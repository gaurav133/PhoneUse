package com.asgj.android.appusage.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.database.PhoneUsageDatabase;
import com.asgj.android.appusage.dialogs.MonthViewFragment;
import com.asgj.android.appusage.dialogs.MonthViewFragment.DateInterface;
import com.asgj.android.appusage.service.UsageTrackingService;
import com.asgj.android.appusage.service.UsageTrackingService.LocalBinder;
import com.asgj.android.appusage.service.UsageTrackingService.provideData;
import com.asgj.android.appusage.ui.widgets.SlidingTabLayout;

public class UsageListMainActivity extends Activity implements View.OnClickListener, DateInterface, UsageListFragment.OnUsageItemClickListener, UsageDetailListFragment.OnDetachFromActivity, Comparator<Map.Entry<Long, UsageInfo>>{
    private Context mContext;
    private MonthViewFragment startDateFragment;
    private Calendar cal1, cal2;
    private UsageTrackingService mMainService;
    private UsageStatsManager mUsageStatsManager;
    private long mTimeStamp;
    private Handler mHandler;
    private HashMap<String, Long> mDataMap;
    private ArrayList<UsageInfo> mMusicList;
    private List<UsageStats> mQueryUsageStats;
    private boolean mIsCreated = false;
    private boolean mIsBound = false;
    private LocalBinder mBinder;
    private boolean mIsDateInPref = true;
    private PhoneUsageDatabase mDatabase;
    private UsageListFragment<HashMap<String, Long>, ArrayList<UsageInfo>> mUsageListFragment;
    private UsageDetailListFragment mDetailFragment;
    private static final String LOG_TAG = UsageListMainActivity.class.getSimpleName();
    private String[] mShowList = null;
    private TextView mShowByOptionsToday = null;
    private TextView mShowByOptionsWeekly = null;
    private TextView mShowByOptionsMonthly = null;
    private TextView mShowByOptionsYearly = null;
    private TextView mShowByOptionsCustom = null;
    private float mNormalYPosition = -1f;
    private float mSecondFabPos = -1f;
    private float mThirdFabPos = -1f;
    private float mForthFabPos = -1f;
    private float mFifthFabPos = -1f;
    private boolean isFabPositionSet = false;
    private int mFabPosParameter = 20;
    private int mFabMarginsRight = 50;
    private int mFabMarginsBottom = 20;
    HashMap<String, UsageStats> mCurrentMap;

    private void setFabPositions() {
        if (isFabPositionSet) {
            return;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float height = metrics.heightPixels;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mFabPosParameter = 20;
        } else {
            mFabPosParameter = 40;
        }
        mSecondFabPos = mShowByOptionsWeekly.getY() - (height / mFabPosParameter * 3);
        mThirdFabPos = mShowByOptionsWeekly.getY() - (height / mFabPosParameter * 6);
        mForthFabPos = mShowByOptionsWeekly.getY() - (height / mFabPosParameter * 9);
        mFifthFabPos = mShowByOptionsWeekly.getY() - (height / mFabPosParameter * 12);
        mNormalYPosition = mShowByOptionsWeekly.getY();
        isFabPositionSet = true;
    }
    
     class loadDataTask extends AsyncTask<Void, Void, Void> {

        private Context mContext; 
        loadDataTask(Context context) {
            mContext = context;
        }
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
         // Fetch static data for today.
            mDataMap = new HashMap<>();


            switch (UsageSharedPrefernceHelper.getShowByType(mContext)) {
            case "Today":
                mDataMap = UsageSharedPrefernceHelper.getAllKeyValuePairsApp(mContext);
                break;
            case "Weekly":
            case "Monthly":
            case "Yearly":
                mDataMap = mDatabase.getAppDurationForGivenTimes(mContext,
                        UsageSharedPrefernceHelper.getCalendarByShowType(mContext),
                        Calendar.getInstance());
                break;
            case "Custom":
                if (Utils.compareDates(cal2, Calendar.getInstance()) != 0) {
                    mDataMap = mDatabase.getAppDurationForGivenTimes(mContext,
                            cal1, cal2);
                } else {
                    mDataMap = mDatabase.getAppDurationForGivenTimes(mContext,
                            cal1, Calendar.getInstance());
                }

                break;
            default:
                break;
            }

            // Service running part. Broadcast and get data. 
            // In case it's custom and end date is not today, we don't need dynamic data.
            if (!(UsageSharedPrefernceHelper.getShowByType(mContext).equals(getString(R.string.string_Custom)) && Utils.compareDates(cal2, Calendar.getInstance()) != 0)) {
                if (mBinder != null) {
                    // Need data from service, fire off a broadcast.
                    Intent getDataIntent = new Intent();
                    getDataIntent.setAction("com.android.asgj.appusage.action.DATA_PROVIDE");
                    sendBroadcast(getDataIntent);

                    mBinder.setInterface(new provideData() {

                        @Override
                        public void provideMap(HashMap<String, Long> map) {
                            // TODO Auto-generated method stub
                                Log.v ("gaurav", "Interface call");
                                for (Map.Entry<String, Long> dataEntry : map.entrySet()) {
                                    String key = dataEntry.getKey();
                                    Log.v ("gaurav", "Data map before entry: " + mDataMap);
                                    if (mDataMap.containsKey(key)) {
                                        mDataMap.put(key, dataEntry.getValue() + mDataMap.get(key));
                                    } else {
                                        mDataMap.put(key, dataEntry.getValue());
                                    }
                                }

                            }
                        });
                    }
                }
            
           // Music part.

            mMusicList = new ArrayList<>();

            Comparator<UsageInfo> startTimeSortComparator = new Comparator<UsageInfo>() {

                @Override
                public int compare(UsageInfo lhs, UsageInfo rhs) {
                    // TODO Auto-generated method stub
                    return (int) (rhs.getmIntervalStartTime() - lhs.getmIntervalStartTime());
                }
                
            };
            switch (UsageSharedPrefernceHelper.getShowByType(mContext)) {
            case "Today":
                mMusicList = UsageSharedPrefernceHelper.getTotalInfoOfMusic(mContext);
                break;
            case "Weekly":
            case "Monthly":
            case "Yearly":
                mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext,
                        UsageSharedPrefernceHelper.getCalendarByShowType(mContext),
                        Calendar.getInstance());
                break;
            case "Custom":
                if (Utils.compareDates(cal2, Calendar.getInstance()) != 0) {
                    mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext, cal1, cal2);
                } else {
                    mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext, cal1,
                            Calendar.getInstance());
                }
                break;
            default:
                break;
            }

                // Check whether custom and end day not today.

            if (!(UsageSharedPrefernceHelper.getShowByType(mContext).equals(
                    getString(R.string.string_Custom))
                    && Utils.compareDates(cal2, Calendar.getInstance()) != 0)) {
                
                if (mMainService != null) {
                    mMusicList.addAll(mMainService.getCurrentDataForMusic());
                }
            }
            Collections.sort(mMusicList, startTimeSortComparator);
            return null;
        }        
        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mUsageListFragment.setmUsageAppData(mDataMap);
            mUsageListFragment.setmMusicData(mMusicList);
        }
        
    }

    // UI elements.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage_list_main_layout);
        getActionBar().setDisplayShowHomeEnabled(false);
        mContext = this;
        mShowList = new String[]{getString(R.string.string_Today),
        		getString(R.string.string_Weekly),getString(R.string.string_Monthly)
        		,getString(R.string.string_Yearly),getString(R.string.string_Custom)};
        mDatabase = new PhoneUsageDatabase(mContext);
        initListFragment();
        if(Utils.isTabletDevice(mContext)){
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View view =  findViewById(R.id.usage_tab_height_layout);
            view.setBackgroundColor(getResources().getColor(R.color.color_action_bar_background));
            final float scale = mContext.getResources().getDisplayMetrics().density;
            int pixels = (int) (2*(SlidingTabLayout.TAB_VIEW_PADDING_DIPS + SlidingTabLayout.TAB_VIEW_TEXT_SIZE_SP) * scale + 1.0f);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
           layoutParams.height=pixels;
           view.setLayoutParams(layoutParams);
            }
        	initDetailFragment(null, "abc");
        }
        mUsageListFragment.setOnUsageItemClickListener(this);

        initFabTextView();

        // Check whether binding is needed.
        if (UsageSharedPrefernceHelper.isServiceRunning(mContext)) {
            Log.v(LOG_TAG, "Rebinding service to activity.");
            Intent startServiceIntent = new Intent();
            startServiceIntent.setClass(mContext, UsageTrackingService.class);
            startServiceIntent
                    .setComponent(new ComponentName(mContext, UsageTrackingService.class));
            bindService(startServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
        
        this.cal1 = Calendar.getInstance();
        long startTime = UsageSharedPrefernceHelper.getCalendar(mContext, "startCalendar");
        cal1.setTimeInMillis(startTime);
        
        this.cal2 = Calendar.getInstance();
        long endTime = UsageSharedPrefernceHelper.getCalendar(mContext, "endCalendar");
        cal2.setTimeInMillis(endTime);
    }
    
    public float getListItemHeight() {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();

        getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, value, true);
        ((WindowManager) (getSystemService(Context.WINDOW_SERVICE)))
                .getDefaultDisplay().getMetrics(metrics);

        return TypedValue.complexToDimension(value.data, metrics);
    }
    
    private void initFabTextView(){
    	mShowByOptionsToday = (TextView)findViewById(R.id.showByOptionsToday);
    	mShowByOptionsWeekly = (TextView)findViewById(R.id.showByOptionsWeekly);
    	mShowByOptionsMonthly = (TextView)findViewById(R.id.showByOptionsMonthly);
    	mShowByOptionsYearly = (TextView)findViewById(R.id.showByOptionsYearly);
    	mShowByOptionsCustom = (TextView)findViewById(R.id.showByOptionsCustom);
        
    	mShowByOptionsToday.setOnClickListener(this);
    	mShowByOptionsWeekly.setOnClickListener(this);
    	mShowByOptionsMonthly.setOnClickListener(this);
    	mShowByOptionsYearly.setOnClickListener(this);
    	mShowByOptionsCustom.setOnClickListener(this);
    	if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
    		mShowByOptionsToday.setVisibility(View.GONE);
    	}else{
    		mShowByOptionsToday.setText(UsageSharedPrefernceHelper.getShowByType(mContext));
    		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mShowByOptionsToday.getLayoutParams();
    		DisplayMetrics metrics = getResources().getDisplayMetrics();
        	float height = metrics.heightPixels;
        	mFabMarginsBottom = (int)getListItemHeight();
        	params.bottomMargin = (int)mFabMarginsBottom * 3/4;
        	params.rightMargin = (int)(height / mFabMarginsRight);
        	mShowByOptionsToday.setLayoutParams(params);
        	mShowByOptionsWeekly.setLayoutParams(params);
        	mShowByOptionsMonthly.setLayoutParams(params);
        	mShowByOptionsYearly.setLayoutParams(params);
        	mShowByOptionsCustom.setLayoutParams(params);
    		mShowByOptionsToday.setElevation(20f);
    		mShowByOptionsWeekly.setElevation(20f);
    		mShowByOptionsMonthly.setElevation(20f);
    		mShowByOptionsYearly.setElevation(20f);
    		mShowByOptionsCustom.setElevation(20f);
        }
    }

    private void initListFragment() {
    	Fragment fragment = getFragmentManager().findFragmentById(R.id.usage_list_main_fragment); 
    	if(fragment != null){
    		getFragmentManager().popBackStackImmediate();
    	}
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mUsageListFragment = new UsageListFragment<HashMap<String, Long>, ArrayList<UsageInfo>>();
        transaction.replace(R.id.usage_list_main_fragment, mUsageListFragment);
        transaction.commit();
    }
    
    private void setFabButtonsVisibility(boolean visible){
    	int visibility = visible ? View.VISIBLE : View.INVISIBLE;
    	mShowByOptionsToday.setVisibility(visibility);		
    }

    
    @Override
    public void onAttachFragment(Fragment fragment) {
    	if(mContext!= null && !Utils.isTabletDevice(mContext) && fragment instanceof UsageDetailListFragment){
    		setFabButtonsVisibility(false);
    	}
    	super.onAttachFragment(fragment);
    }
    
    private void initDetailFragment(HashMap<Long,UsageInfo> intervalMap, String applicationName) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        
        LinkedHashMap<Long, UsageInfo> linkedMap = null;
        // First sort map by key (start duration).
        if (intervalMap != null && !intervalMap.isEmpty()) {
             linkedMap = sortMapByKey(intervalMap);
        }
        
        mDetailFragment = new UsageDetailListFragment(linkedMap);
        mDetailFragment.setOnDetachListener(this);
        if (!Utils.isTabletDevice(mContext)) {
        	transaction.setCustomAnimations(R.anim.enter_from_right,
        			R.anim.exit_to_right,R.anim.exit_to_left,R.anim.enter_from_left);
            transaction.replace(R.id.usage_list_main_fragment, mDetailFragment);
            transaction.addToBackStack(null);
        }
        else {
        transaction.replace(R.id.usage_detail_main_fragment, mDetailFragment);
        }
        transaction.commit();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.v(LOG_TAG, "Service disconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMainService = ((LocalBinder) service).getInstance();
            mBinder = (LocalBinder) service;
            mIsBound = true;

/*            if (mIsCreated) {
                
                displayDataForApps();
                 
                displayDataForMusic();
            }*/
            Log.v(LOG_TAG, "Service connected, mMainService is: " + mMainService);
        }
    };
    
    /**
     * Displays data as per different scenarios and preferences.
     */
    

   /**
    * Display data for music as per user preferences.
    */
    public void displayDataForMusic() {
        mMusicList = new ArrayList<>();

        Comparator<UsageInfo> startTimeSortComparator = new Comparator<UsageInfo>() {

            @Override
            public int compare(UsageInfo lhs, UsageInfo rhs) {
                // TODO Auto-generated method stub
                return (int) (rhs.getmIntervalStartTime() - lhs.getmIntervalStartTime());
            }
            
        };
        switch (UsageSharedPrefernceHelper.getShowByType(mContext)) {
        case "Today":
            mMusicList = UsageSharedPrefernceHelper.getTotalInfoOfMusic(mContext);
            break;
        case "Weekly":
        case "Monthly":
        case "Yearly":
            mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext,
                    UsageSharedPrefernceHelper.getCalendarByShowType(mContext),
                    Calendar.getInstance());
            break;
        case "Custom":
            if (Utils.compareDates(cal2, Calendar.getInstance()) != 0) {
                mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext, cal1, cal2);
            } else {
                mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext, cal1,
                        Calendar.getInstance());
            }
            break;
        default:
            break;
        }

            // Check whether custom and end day not today.

        if (UsageSharedPrefernceHelper.getShowByType(mContext).equals(
                getString(R.string.string_Custom))
                && Utils.compareDates(cal2, Calendar.getInstance()) != 0) {
            return;
        } else {
            if (mMainService != null) {
                mMusicList.addAll(mMainService.getCurrentDataForMusic());
            }
        }
        Collections.sort(mMusicList, startTimeSortComparator);
        mUsageListFragment.setmMusicData(mMusicList);
    }

    /**
     * onResume method to dynamically show data as tracking progresses.
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Log.v(LOG_TAG, "mainService onResume is: " + mMainService);

        // IF service not running, show data from xml.
        if (!UsageSharedPrefernceHelper.isServiceRunning(mContext)) {
           
            if (mIsDateInPref == true) {
                
                // Fetch date from preferences and compare.
                Date presentDate = new Date(System.currentTimeMillis());
                Date prefsDate = new Date(UsageSharedPrefernceHelper.getDateStoredInPref(mContext));

                Calendar c1 = Calendar.getInstance();
                c1.setTime(presentDate);
                
                Calendar c2 = Calendar.getInstance();
                c2.setTime(prefsDate);

                // Compare with present time.
                if (Utils.compareDates(c1, c2) == 1) {
                    // Clear preference.
                    UsageSharedPrefernceHelper.clearPreference(mContext);
                }
                mIsDateInPref = false;

                // Store timestamp to check if day has changed.
                mTimeStamp = System.currentTimeMillis();
            } else {
                Date presentDate = new Date(System.currentTimeMillis());
                Date prevDate = new Date(mTimeStamp);

                Calendar c1 = Calendar.getInstance();
                c1.setTime(presentDate);

                Calendar c2 = Calendar.getInstance();
                c2.setTime(prevDate);

                if (Utils.compareDates(c1, c2) == 1) {
                    // Clear prefs.
                    UsageSharedPrefernceHelper.clearPreference(mContext);
                }
                
                mTimeStamp = System.currentTimeMillis();
            }
        }
        

        loadDataTask dataTask = new loadDataTask(mContext);
        dataTask.execute();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_activity_menu, menu);

        if (UsageSharedPrefernceHelper.isServiceRunning(mContext)) {
            MenuItem menuItem = (MenuItem) menu.findItem(R.id.action_start);
            menuItem.setTitle(getString(R.string.string_stop));
        }
        
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        	 MenuItem menuItem = (MenuItem) menu.findItem(R.id.action_showBy);
        	 menuItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if(mShowByOptionsWeekly.getVisibility() == View.VISIBLE)
        	hideFabOption();
    	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.v(LOG_TAG, "onDestroy activity"+mIsBound);

        // Unbind from service to prevent service connection leak.
        if (mIsBound) {
            unbindService(mConnection);
            mConnection = null;
        }
        
        UsageSharedPrefernceHelper.setCalendar(mContext, cal1.getTimeInMillis(), "startCalendar");
        UsageSharedPrefernceHelper.setCalendar(mContext, cal2.getTimeInMillis(), "endCalendar");
        
        UsageSharedPrefernceHelper.setCurrentDate(mContext);
        mDataMap = null;
        mMusicList = null;
        mContext = null;
        mBinder = null;
        mDatabase = null;
        super.onDestroy();
    }

    private void startTrackingService() {
            UsageSharedPrefernceHelper.setServiceRunning(mContext, true);

            // Here you bind to the service.
            Intent startServiceIntent = new Intent();
            startServiceIntent.setClass(this, UsageTrackingService.class);
            startServiceIntent.setComponent(new ComponentName(this, UsageTrackingService.class));
            startService(startServiceIntent);
            bindService(startServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

            // Home Screen intent.
            Intent intentGoToHomeScreen = new Intent();
            intentGoToHomeScreen.setAction("android.intent.action.MAIN");
            intentGoToHomeScreen.addCategory("android.intent.category.HOME");

        // Test toast.
        Toast.makeText(mContext, "Your phone usage is being calculated now!", Toast.LENGTH_LONG)
                .show();
        startActivity(intentGoToHomeScreen);

        // Service started, fetch updated data.
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mCurrentMap = Utils.getCurrentUsageMapForL(mContext);

            HashMap<String, Long> map = new HashMap<>();

            for (Map.Entry<String, UsageStats> entry : mCurrentMap.entrySet()) {
                map.put(entry.getKey(), entry.getValue().getTotalTimeInForeground());
            }

            // Store this data in preferences for future calculations.
            UsageSharedPrefernceHelper.updatePreviousDataForAppsForL(mContext, map);
        }

        finish();

    }

    @SuppressLint("InlinedApi")
    private void stopTrackingService() {
        // Destroy the service, as no longer needed.
        Intent stopServiceIntent = new Intent();
        stopServiceIntent.setClass(this, UsageTrackingService.class);
        stopServiceIntent.setComponent(new ComponentName(this, UsageTrackingService.class));
        stopService(stopServiceIntent);

       UsageSharedPrefernceHelper.setServiceRunning(mContext, false);

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");

            SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd,yyyy HH:mm");

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);

            Log.v(LOG_TAG, "CALENDAR OBJECT: " + c);
            TimeZone currentTimeZone = TimeZone.getDefault();
            int offset = currentTimeZone.getRawOffset();
            Date resultdate2 = new Date(offset);
            Log.v(LOG_TAG,
                    "Current timezone: " + currentTimeZone + " and offset : "
                            + sdf1.format(resultdate2));

            long time2 = System.currentTimeMillis();

            // Need to query usage stats for present day according to timezone.
            mQueryUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    c.getTimeInMillis(), time2);

            Date resultdate1 = new Date(c.getTimeInMillis());

            Log.v(LOG_TAG, "Inside stop" + sdf1.format(resultdate1));

            SimpleDateFormat sdf11 = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date resultdate11 = new Date(time2);
            Log.v(LOG_TAG, sdf11.format(resultdate11));
        }

        Log.v(LOG_TAG, "Query stats:" + mQueryUsageStats);
        // Query stats:
        if (mQueryUsageStats != null) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : mQueryUsageStats) {
                mySortedMap.put(usageStats.getTotalTimeInForeground(), usageStats);
            }
            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                for (Map.Entry<Long, UsageStats> entry : mySortedMap.entrySet()) {
                    Log.v(LOG_TAG, "Key : " + entry.getKey());
                    Log.v(LOG_TAG, "Value : " + entry.getValue());
                    String topPackageName = entry.getValue().getPackageName();
                    Log.v(LOG_TAG, "Package name: " + topPackageName);
                    Log.v(LOG_TAG, "Time spent in foreground: "
                            + entry.getValue().getTotalTimeInForeground() / 1000);
                    // Log.v (LOG_TAG, "First timestamp")

                    SimpleDateFormat sdf11 = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                    Date resultdate11 = new Date(entry.getValue().getFirstTimeStamp());
                    Log.v(LOG_TAG, "First timestamp: " + sdf11.format(resultdate11));

                    Date resultdate111 = new Date(entry.getValue().getLastTimeStamp());
                    Log.v(LOG_TAG, "Last timestamp: " + sdf11.format(resultdate111));
                }
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        // TODO Auto-generated method stub
        Log.v(LOG_TAG, "onTrim memory callback activity, level is: " + level);
        if (level >= TRIM_MEMORY_COMPLETE) {
            if (mContext != null && UsageSharedPrefernceHelper.isServiceRunning(mContext)) {
                // If service is bound to activity.
                if (mMainService != null) {
                    mMainService.saveDataOnKill();
                }
            }
        }
        super.onTrimMemory(level);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(mShowByOptionsWeekly.getVisibility() == View.VISIBLE)
        	hideFabOption();
        switch (item.getItemId()) {
        case R.id.action_start:
            if (!UsageSharedPrefernceHelper.isServiceRunning(this)) {
                if (Utils.isSufficientRAMAvailable(mContext, true)
                        && Utils.isSufficientBatteryAvailable(mContext, true)) {
                    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        if (!Utils.isPermissionGranted(this)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                    .setTitle(R.string.string_error_title)
                                    .setMessage(R.string.string_error_msg)
                                    .setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    Intent intent = new Intent(
                                                            Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                                    startActivity(intent);
                                                }
                                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            startTrackingService();
                            item.setTitle(getString(R.string.string_stop));
                        }
                } else {
                        startTrackingService();
                        item.setTitle(getString(R.string.string_stop));
                    }
                }
            } else {
                stopTrackingService();
                item.setTitle(getString(R.string.string_start));
                finish();
            }

            break;
        case R.id.action_showBy:
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(
                    getString(R.string.string_showBy)).setSingleChoiceItems(
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                            mShowList),
                    Utils.getIndexFromArray(mShowList,
                            UsageSharedPrefernceHelper.getShowByType(mContext)),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
						    if (!mShowList[which].equals(mContext.getString(R.string.string_Custom))) {
							    UsageSharedPrefernceHelper.setShowByUsage(getBaseContext(),mShowList[which]);
						        loadDataTask dataTask = new loadDataTask(mContext);
						        dataTask.execute();
						    } else {
         					    startDateFragment = new MonthViewFragment();
								startDateFragment.show(getFragmentManager(),"startMonthViewPicker");
							}
						    dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            break;
        case R.id.action_setting:
        	Intent intent = new Intent(this, SettingActivity.class);
        	startActivity(intent);
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showFabOptions(){
		mShowByOptionsWeekly.animate().y(mSecondFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();
		mShowByOptionsMonthly.animate().y(mThirdFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();;
		mShowByOptionsYearly.animate().y(mForthFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();
		mShowByOptionsCustom.animate().y(mFifthFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();
    }
    
    private void hideFabOption(){
		mShowByOptionsWeekly.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
		mShowByOptionsMonthly.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
		mShowByOptionsYearly.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
		mShowByOptionsCustom.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
    	
    }
    
 class ShowAnimationListner implements AnimatorListener {
		
		@Override
		public void onAnimationStart(Animator animation) {
			mShowByOptionsToday.setClickable(false);
			mShowByOptionsWeekly.setClickable(false);
			mShowByOptionsMonthly.setClickable(false);
			mShowByOptionsYearly.setClickable(false);
			mShowByOptionsCustom.setClickable(false);
			mShowByOptionsWeekly.setVisibility(View.VISIBLE);
			mShowByOptionsMonthly.setVisibility(View.VISIBLE);
			mShowByOptionsYearly.setVisibility(View.VISIBLE);
			mShowByOptionsCustom.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			mShowByOptionsToday.setClickable(true);
			mShowByOptionsWeekly.setClickable(true);
			mShowByOptionsMonthly.setClickable(true);
			mShowByOptionsYearly.setClickable(true);
			mShowByOptionsCustom.setClickable(true);
			
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}
	}
    
    class HideAnimationListner implements AnimatorListener {
		
		@Override
		public void onAnimationStart(Animator animation) {
			mShowByOptionsToday.setClickable(false);
			mShowByOptionsWeekly.setClickable(false);
			mShowByOptionsMonthly.setClickable(false);
			mShowByOptionsYearly.setClickable(false);
			mShowByOptionsCustom.setClickable(false);
			
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			
			mShowByOptionsWeekly.setVisibility(View.INVISIBLE);
			mShowByOptionsMonthly.setVisibility(View.INVISIBLE);
			mShowByOptionsYearly.setVisibility(View.INVISIBLE);
			mShowByOptionsCustom.setVisibility(View.INVISIBLE);
			mShowByOptionsToday.setClickable(true);
			mShowByOptionsWeekly.setClickable(true);
			mShowByOptionsMonthly.setClickable(true);
			mShowByOptionsYearly.setClickable(true);
			mShowByOptionsCustom.setClickable(true);
			
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.showByOptionsToday:
			setFabPositions();
			if (mShowByOptionsWeekly.getVisibility() == View.INVISIBLE) {
				showFabOptions();
				mShowByOptionsToday.setText(mShowList[0]);
			} else {
				UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[0]);
				hideFabOption();
				mShowByOptionsToday.setText(mShowList[0]);
		        loadDataTask dataTaskToday = new loadDataTask(mContext);
		        dataTaskToday.execute();
				if(Utils.isTabletDevice(mContext))
				updateDetailFragment(null);
			}
           
			break;
		case R.id.showByOptionsWeekly:
			UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[1]);
			hideFabOption();
			mShowByOptionsToday.setText(mShowList[1]);
            loadDataTask dataTaskWeekly = new loadDataTask(mContext);
            dataTaskWeekly.execute();
            if(Utils.isTabletDevice(mContext))
            updateDetailFragment(null);
			break;
		case R.id.showByOptionsMonthly:
			UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[2]);
			hideFabOption();
			mShowByOptionsToday.setText(mShowList[2]);
            loadDataTask dataTaskMonthly = new loadDataTask(mContext);
            dataTaskMonthly.execute();
            if(Utils.isTabletDevice(mContext))
            updateDetailFragment(null);
			break;
		case R.id.showByOptionsYearly:
			UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[3]);
			hideFabOption();
			mShowByOptionsToday.setText(mShowList[3]);
            loadDataTask dataTaskYearly = new loadDataTask(mContext);
            dataTaskYearly.execute();
            if(Utils.isTabletDevice(mContext))
            updateDetailFragment(null);
			break;
		case R.id.showByOptionsCustom:
			startDateFragment = new MonthViewFragment();
            startDateFragment.show(getFragmentManager(), "startMonthViewPicker");
			hideFabOption();
			break;
		}
		
	}

    @Override
    public void onDateSetComplete(Calendar startCalendar, Calendar endCalendar) {
        // TODO Auto-generated method stub

        this.cal1 = startCalendar;
        this.cal2 = endCalendar;
        // Set in preference only after date from both pickers have been validated.
        UsageSharedPrefernceHelper.setShowByUsage(mContext,
                mContext.getString(R.string.string_Custom));
        mShowByOptionsToday.setText(mShowList[4]);
        loadDataTask dataTask = new loadDataTask(mContext);
        dataTask.execute();
            if(Utils.isTabletDevice(mContext))
            updateDetailFragment(null);
    }
    
    @Override
    public void onMusicItemClick(String pkg, int groupPosition,
    		int childPosition) {
    	if(mShowByOptionsWeekly.getVisibility() == View.VISIBLE)
        	hideFabOption();    	
    }
    
    private void updateDetailFragment(String pkg) {
        // Check current preference first.
    	HashMap<Long,UsageInfo> infoMap = null;
        // Check whether custom and end day not today.
        if (UsageSharedPrefernceHelper.getShowByType(mContext).equals(
                mContext.getString(R.string.string_Custom))
                && Utils.compareDates(cal2, Calendar.getInstance()) != 0) {

            infoMap = mDatabase.getAppIntervalsBetweenDates(pkg, cal1, cal2);
        } else {

            switch (UsageSharedPrefernceHelper.getShowByType(mContext)) {
            case "Today":
                infoMap = mDatabase.getAppIntervalsBetweenDates(pkg, Calendar.getInstance(),
                        Calendar.getInstance());
                break;
            case "Weekly":
            case "Monthly":
            case "Yearly":
                infoMap = mDatabase.getAppIntervalsBetweenDates(pkg,
                        UsageSharedPrefernceHelper.getCalendarByShowType(mContext),
                        Calendar.getInstance());
        	
                break;
            case "Custom":
                infoMap = mDatabase.getAppIntervalsBetweenDates(pkg,
                        cal1, Calendar.getInstance());
                break;
            default:
                break;
            }
        }
        
        LinkedHashMap<Long, UsageInfo> linkedMap = sortMapByKey(infoMap);
        initDetailFragment(linkedMap, Utils.getApplicationLabelName(mContext, pkg));
    }

    private LinkedHashMap<Long, UsageInfo> sortMapByKey(HashMap<Long,UsageInfo> infoMap) {

        // Sort intervals before sending to detail fragment.
        LinkedList<Map.Entry<Long, UsageInfo>> list = new LinkedList<>();
        for (Map.Entry<Long, UsageInfo> entry : infoMap.entrySet()) {
            list.add(entry);
        }
        Collections.sort(list, this);
        
        LinkedHashMap<Long, UsageInfo> linkedMap = new LinkedHashMap<>();
        ListIterator<Map.Entry<Long, UsageInfo>> iterator = list.listIterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Long, UsageInfo> entry = iterator.next();
            linkedMap.put(entry.getKey(), entry.getValue());
        }
        return linkedMap;
    }
    @Override
    public void onUsageItemClick(String pkg, int position) {
    	if(mShowByOptionsWeekly.getVisibility() == View.VISIBLE)
    	hideFabOption();
    	if(pkg.equals("totalTime")){
    		return;
    	}
       
        updateDetailFragment(pkg);
        
		
	}

	@Override
	public void onDetach() {
		if(!Utils.isTabletDevice(mContext)){
			setFabButtonsVisibility(true);
		}
		
	}

    @Override
    public int compare(Entry<Long, UsageInfo> lhs, Entry<Long, UsageInfo> rhs) {
        // TODO Auto-generated method stub
        return (int) (rhs.getKey() - lhs.getKey());
    }
	@Override
	public void onUsageItemSwiped(String pkg, int position) {
		Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.setType("text/plain");
    	intent.putExtra(Intent.EXTRA_TEXT, "I used "+ Utils.getApplicationLabelName(mContext, pkg) + "for duration :"+
    	Utils.getTimeFromSeconds(mDataMap.get(pkg).longValue())+ "\n" + "Sent from PhoneUse App");
    	startActivity(Intent.createChooser(intent, "Share with"));
		
	}
}

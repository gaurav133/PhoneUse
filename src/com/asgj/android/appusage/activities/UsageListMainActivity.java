package com.asgj.android.appusage.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
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

public class UsageListMainActivity extends Activity implements View.OnClickListener, DateInterface, UsageListFragment.OnUsageItemClickListener, UsageDetailListFragment.OnDetachFromActivity{
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
    private TextView mShowByOptionsMain = null;
    private TextView mShowByOptions2 = null;
    private TextView mShowByOptions3 = null;
    private TextView mShowByOptions4 = null;
    private TextView mShowByOptions5 = null;
    private float mNormalYPosition = -1f;
    private float mSecondFabPos = -1f;
    private float mThirdFabPos = -1f;
    private float mForthFabPos = -1f;
    private float mFifthFabPos = -1f;
    private boolean isFabPositionSet = false;
    private int mFabPosParameter = 20;
    private int mFabMargins = 50;
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
        mSecondFabPos = mShowByOptions2.getY() - (height / mFabPosParameter * 3);
        mThirdFabPos = mShowByOptions2.getY() - (height / mFabPosParameter * 6);
        mForthFabPos = mShowByOptions2.getY() - (height / mFabPosParameter * 9);
        mFifthFabPos = mShowByOptions2.getY() - (height / mFabPosParameter * 12);
        mNormalYPosition = mShowByOptions2.getY();
        isFabPositionSet = true;
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
        	initDetailFragment(null, "abc");
        }
        mUsageListFragment.setOnUsageItemClickListener(this);
        mIsCreated = true;

        mHandler = new Handler();

        initFabTextView();

        // Check whether binding is needed.
        if (UsageSharedPrefernceHelper.isServiceRunning(mContext)) {
            Log.v(LOG_TAG, "Rebinding service to activity.");
            Intent startServiceIntent = new Intent();
            startServiceIntent.setClass(mContext, UsageTrackingService.class);
            startServiceIntent
                    .setComponent(new ComponentName(mContext, UsageTrackingService.class));
            bindService(startServiceIntent, mConnection, 0);
        }
        
        this.cal1 = Calendar.getInstance();
        long startTime = UsageSharedPrefernceHelper.getCalendar(mContext, "startCalendar");
        cal1.setTimeInMillis(startTime);
        
        this.cal2 = Calendar.getInstance();
        long endTime = UsageSharedPrefernceHelper.getCalendar(mContext, "endCalendar");
        cal2.setTimeInMillis(endTime);
    }
    
    private void initFabTextView(){
    	mShowByOptionsMain = (TextView)findViewById(R.id.showByOptions1);
    	mShowByOptions2 = (TextView)findViewById(R.id.showByOptions2);
    	mShowByOptions3 = (TextView)findViewById(R.id.showByOptions3);
    	mShowByOptions4 = (TextView)findViewById(R.id.showByOptions4);
    	mShowByOptions5 = (TextView)findViewById(R.id.showByOptions5);
        
    	mShowByOptionsMain.setOnClickListener(this);
    	mShowByOptions2.setOnClickListener(this);
    	mShowByOptions3.setOnClickListener(this);
    	mShowByOptions4.setOnClickListener(this);
    	mShowByOptions5.setOnClickListener(this);
    	if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
    		mShowByOptionsMain.setVisibility(View.GONE);
    	}else{
    		mShowByOptionsMain.setText(UsageSharedPrefernceHelper.getShowByType(mContext));
    		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mShowByOptionsMain.getLayoutParams();
    		DisplayMetrics metrics = getResources().getDisplayMetrics();
        	float height = metrics.heightPixels;
        	params.bottomMargin = (int)(height / mFabMargins);
        	params.rightMargin = (int)(height / mFabMargins);
        	mShowByOptionsMain.setLayoutParams(params);
        	mShowByOptions2.setLayoutParams(params);
        	mShowByOptions3.setLayoutParams(params);
        	mShowByOptions4.setLayoutParams(params);
        	mShowByOptions5.setLayoutParams(params);
    		mShowByOptionsMain.setElevation(20f);
    		mShowByOptions2.setElevation(20f);
    		mShowByOptions3.setElevation(20f);
    		mShowByOptions4.setElevation(20f);
    		mShowByOptions5.setElevation(20f);
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
    	mShowByOptionsMain.setVisibility(visibility);		
    }

    
    @Override
    public void onAttachFragment(Fragment fragment) {
    	if(mContext!= null && !Utils.isTabletDevice(mContext) && fragment instanceof UsageDetailListFragment){
    		setFabButtonsVisibility(false);
    	}
    	super.onAttachFragment(fragment);
    }
    
    private void initDetailFragment(HashMap<Long,UsageInfo> intervalList, String applicationName) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mDetailFragment = new UsageDetailListFragment(intervalList);
        mDetailFragment.setOnDetachListener(this);
        if (!Utils.isTabletDevice(mContext)) {
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

            if (mIsCreated) {
                
                displayDataForApps();
                 
                displayDataForMusic();
            }
            Log.v(LOG_TAG, "Service connected, mMainService is: " + mMainService);
        }
    };
    
    /**
     * Displays data as per different scenarios and preferences.
     */
    public void displayDataForApps() {

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

        mUsageListFragment.setmUsageAppData(mDataMap);

        // Service running part. Broadcast and get data. 
        // In case it's custom and end date is not today, we don't need dynamic data.
        if (UsageSharedPrefernceHelper.getShowByType(mContext).equals(getString(R.string.string_Custom)) && Utils.compareDates(cal2, Calendar.getInstance()) != 0) {
            return;
        } else {
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
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    mUsageListFragment.setmUsageAppData(mDataMap);
                                }
                            });
                        }
                    });
                }
            }
        }

   /**
    * Display data for music as per user preferences.
    */
    public void displayDataForMusic() {
        mMusicList = new ArrayList<>();

                switch (UsageSharedPrefernceHelper.getShowByType(mContext)) {
                case "Today":
                    mMusicList = UsageSharedPrefernceHelper.getTotalInfoOfMusic(mContext);
                    mUsageListFragment.setmMusicData(mMusicList);
                    break;
                case "Weekly":
                case "Monthly":
                case "Yearly":
                    mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext,
                            UsageSharedPrefernceHelper.getCalendarByShowType(mContext),
                            Calendar.getInstance());
                    mUsageListFragment.setmMusicData(mMusicList);
                    break;
                case "Custom":
                    if (Utils.compareDates(cal2, Calendar.getInstance()) != 0) {
                        mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext,
                                cal1, cal2);
                    } else {
                        mMusicList = mDatabase.getMusicIntervalsBetweenDates(mContext,
                                cal1, Calendar.getInstance());
                    }
                    mUsageListFragment.setmMusicData(mMusicList);
                    break;
                default:
                    break;
                }

            // Check whether custom and end day not today.

                if (UsageSharedPrefernceHelper.getShowByType(mContext).equals(getString(R.string.string_Custom)) && Utils.compareDates(cal2, Calendar.getInstance()) != 0) {
                    return;
                } else {
                if (mMainService != null) {
                        mMusicList.addAll( mMainService.getCurrentDataForMusic());
                }
            }

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
        

            displayDataForApps();
        displayDataForMusic();
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
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.v(LOG_TAG, "onDestroy activity");

        // Unbind from service to prevent service connection leak.
        if (mIsBound) {
            unbindService(mConnection);
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
            bindService(startServiceIntent, mConnection, 0);

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
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case R.id.action_start:
            if (!UsageSharedPrefernceHelper.isServiceRunning(this)) {
                if (Utils.isSufficientRAMAvailable(mContext)
                        && Utils.isSufficientBatteryAvailable(mContext)) {
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
								if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
								    displayDataForApps();
								}
								displayDataForMusic();
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
		mShowByOptions2.animate().y(mSecondFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();
		mShowByOptions3.animate().y(mThirdFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();;
		mShowByOptions4.animate().y(mForthFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();
		mShowByOptions5.animate().y(mFifthFabPos).setDuration(400).setListener(new ShowAnimationListner()).start();
    }
    
    private void hideFabOption(){
		mShowByOptions2.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
		mShowByOptions3.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
		mShowByOptions4.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
		mShowByOptions5.animate().y(mNormalYPosition).setDuration(400).setListener(new HideAnimationListner()).start();
    	
    }
    
 class ShowAnimationListner implements AnimatorListener {
		
		@Override
		public void onAnimationStart(Animator animation) {
			mShowByOptionsMain.setClickable(false);
			mShowByOptions2.setClickable(false);
			mShowByOptions3.setClickable(false);
			mShowByOptions4.setClickable(false);
			mShowByOptions5.setClickable(false);
			mShowByOptions2.setVisibility(View.VISIBLE);
			mShowByOptions3.setVisibility(View.VISIBLE);
			mShowByOptions4.setVisibility(View.VISIBLE);
			mShowByOptions5.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			mShowByOptionsMain.setClickable(true);
			mShowByOptions2.setClickable(true);
			mShowByOptions3.setClickable(true);
			mShowByOptions4.setClickable(true);
			mShowByOptions5.setClickable(true);
			
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}
	}
    
    class HideAnimationListner implements AnimatorListener {
		
		@Override
		public void onAnimationStart(Animator animation) {
			mShowByOptionsMain.setClickable(false);
			mShowByOptions2.setClickable(false);
			mShowByOptions3.setClickable(false);
			mShowByOptions4.setClickable(false);
			mShowByOptions5.setClickable(false);
			
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			
			mShowByOptions2.setVisibility(View.INVISIBLE);
			mShowByOptions3.setVisibility(View.INVISIBLE);
			mShowByOptions4.setVisibility(View.INVISIBLE);
			mShowByOptions5.setVisibility(View.INVISIBLE);
			mShowByOptionsMain.setClickable(true);
			mShowByOptions2.setClickable(true);
			mShowByOptions3.setClickable(true);
			mShowByOptions4.setClickable(true);
			mShowByOptions5.setClickable(true);
			
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.showByOptions1:
			setFabPositions();
			if(mShowByOptions2.getVisibility() == View.INVISIBLE){
			    showFabOptions();
			    mShowByOptionsMain.setText(mShowList[0]);
			}else{
				UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[0]);
				hideFabOption();
				mShowByOptionsMain.setText(mShowList[0]);
							}
            displayDataForApps();
            displayDataForMusic();
			break;
		case R.id.showByOptions2:
			UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[1]);
			hideFabOption();
			mShowByOptionsMain.setText(mShowList[1]);
			             displayDataForApps();
            displayDataForMusic();
			break;
		case R.id.showByOptions3:
			UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[2]);
			hideFabOption();
			mShowByOptionsMain.setText(mShowList[2]);
			 			displayDataForApps();
            displayDataForMusic();
			break;
		case R.id.showByOptions4:
			UsageSharedPrefernceHelper.setShowByUsage(this, mShowList[3]);
			hideFabOption();
			mShowByOptionsMain.setText(mShowList[3]);
			            displayDataForApps();
            displayDataForMusic();
			break;
		case R.id.showByOptions5:
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
            displayDataForApps();
            displayDataForMusic();
    }
    
    @Override
    public void onMusicItemClick(String pkg, int groupPosition,
    		int childPosition) {
    	// TODO Auto-generated method stub
    	
    }

    @Override
    public void onUsageItemClick(String pkg, int position) {
    	if(mShowByOptions2.getVisibility() == View.VISIBLE)
    	hideFabOption();
        // Check current preference first.
    	HashMap<Long,UsageInfo> infoList = null;
        // Check whether custom and end day not today.
        if (UsageSharedPrefernceHelper.getShowByType(mContext).equals(
                mContext.getString(R.string.string_Custom))
                && Utils.compareDates(cal2, Calendar.getInstance()) != 0) {

        	infoList = mDatabase.getAppIntervalsBetweenDates(pkg, cal1, cal2);
        } else {

            switch (UsageSharedPrefernceHelper.getShowByType(mContext)) {
            case "Today":
            	infoList = mDatabase.getAppIntervalsBetweenDates(pkg, Calendar.getInstance(),
                        Calendar.getInstance());
                break;
            case "Weekly":
            case "Monthly":
            case "Yearly":
            	infoList = mDatabase.getAppIntervalsBetweenDates(pkg,
                        UsageSharedPrefernceHelper.getCalendarByShowType(mContext),
                        Calendar.getInstance());
        	
                break;
            case "Custom":
            	infoList = mDatabase.getAppIntervalsBetweenDates(pkg,
                        cal1, Calendar.getInstance());
                break;
            default:
                break;
            }
        }

        if (Utils.isTabletDevice(mContext)) {
            mDetailFragment.updateDetailList(infoList);
        } else {
            initDetailFragment(infoList, Utils.getApplicationLabelName(mContext, pkg));
		}
		
	}

	@Override
	public void onDetach() {
		if(!Utils.isTabletDevice(mContext)){
			setFabButtonsVisibility(true);
		}
		
	}
}

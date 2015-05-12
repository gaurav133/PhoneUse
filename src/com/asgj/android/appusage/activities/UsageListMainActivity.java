package com.asgj.android.appusage.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.database.PhoneUsageDatabase;
import com.asgj.android.appusage.service.UsageTrackingService;
import com.asgj.android.appusage.service.UsageTrackingService.LocalBinder;

public class UsageListMainActivity extends Activity implements OnClickListener {
    private Context mContext;
    private static UsageTrackingService mMainService;
    private UsageStatsManager mUsageStatsManager;
    private List<UsageStats> mQueryUsageStats;
    private long mStartServiceTime;
    private PhoneUsageDatabase mDatabase;
    private boolean isServiceStarted = false;
    private static final String LOG_TAG = UsageListMainActivity.class.getSimpleName();
    

    // UI elements.
    Button mServiceStartButton, mServiceStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mDatabase = new PhoneUsageDatabase(mContext);
        init();
        
        // Check whether binding is needed.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("ServiceStart", false) == true) {
            Log.v (LOG_TAG, "Service restrart from activity");
            Intent startServiceIntent = new Intent();
            startServiceIntent.setClass(this, UsageTrackingService.class);
            startServiceIntent.setComponent(new ComponentName(this, UsageTrackingService.class));
            startService(startServiceIntent);
            bindService(startServiceIntent, mConnection, 0);
        }
    }

    /**
     * Initialize UI controls and event listeners.
     * This method is called every-time the activity is created.
     */
    public void init() {

        // Initialize buttons.
        Button mServiceStartButton = (Button) findViewById(R.id.startButton);
        Button mServiceStopButton = (Button) findViewById(R.id.stopButton);

        // Set listeners.
        mServiceStartButton.setOnClickListener(this);
        mServiceStopButton.setOnClickListener(this);
        
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.v (LOG_TAG, "Service disconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMainService = ((LocalBinder) service).getInstance();
            Log.v (LOG_TAG, "Service connected, mMainService is: " + mMainService);
        }
    };

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.startButton:
            
            isServiceStarted = true;
            
            // Commit this to shared-preference.
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("ServiceStart",true);
            editor.commit();
            
             mStartServiceTime = System.currentTimeMillis();
             Log.v (LOG_TAG, "Time: " + mStartServiceTime);
             SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
             Date resultdate = new Date(mStartServiceTime);
             Log.v (LOG_TAG,sdf.format(resultdate));
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) { 
               
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
            Toast.makeText(mContext, "Your phone usage is being calculated now!", Toast.LENGTH_SHORT)
                    .show();
            startActivity(intentGoToHomeScreen);
                }
            break;

        case R.id.stopButton:
            /**
             * When user presses stop button, service should be stopped, and data inserted to database.
             */

            // Destroy the service, as no longer needed.
            Intent stopServiceIntent = new Intent();
            stopServiceIntent.setClass(this, UsageTrackingService.class);
            stopServiceIntent.setComponent(new ComponentName(this, UsageTrackingService.class));
            stopService(stopServiceIntent);
            
            isServiceStarted = false;
            SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor1 = preferences1.edit();
            editor1.putBoolean("ServiceStart",false);
            editor1.commit();
            
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { 
                mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
                
                SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd,yyyy HH:mm");   
                
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                
                Log.v (LOG_TAG, "CALENDAR OBJECT: " + c);
                TimeZone currentTimeZone = TimeZone.getDefault();
                int offset = currentTimeZone.getRawOffset();
                Date resultdate2 = new Date(offset);
                Log.v (LOG_TAG, "Current timezone: " + currentTimeZone + " and offset : " + sdf1.format(resultdate2));
                
                long time2 = System.currentTimeMillis();
                
                // Need to query usage stats for present day according to timezone.
                mQueryUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, c.getTimeInMillis(), time2);
                 
                Date resultdate1 = new Date(c.getTimeInMillis());
                
                Log.v (LOG_TAG,"Inside stop" + sdf1.format(resultdate1));
                
                SimpleDateFormat sdf11 = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
                Date resultdate11 = new Date(time2);
                Log.v (LOG_TAG,sdf11.format(resultdate11));
               }
            
            Log.v (LOG_TAG, "Query stats:" + mQueryUsageStats);
            // Query stats:
            if(mQueryUsageStats != null) {
                SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                for (UsageStats usageStats : mQueryUsageStats) {
                    mySortedMap.put(usageStats.getTotalTimeInForeground(),usageStats);
                }                    
                if(mySortedMap != null && !mySortedMap.isEmpty()) {
                    for (Map.Entry<Long, UsageStats> entry : mySortedMap.entrySet()) {
                        Log.v (LOG_TAG, "Key : " + entry.getKey());
                        Log.v (LOG_TAG, "Value : " + entry.getValue());
                        String topPackageName =  entry.getValue().getPackageName();
                        Log.v (LOG_TAG, "Package name: " + topPackageName);
                        Log.v (LOG_TAG, "Time spent in foreground: " + entry.getValue().getTotalTimeInForeground()/1000);
                       // Log.v (LOG_TAG, "First timestamp")
                        
                        SimpleDateFormat sdf11 = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
                        Date resultdate11 = new Date(entry.getValue().getFirstTimeStamp());
                        Log.v (LOG_TAG,"First timestamp: " + sdf11.format(resultdate11));
                        
                        Date resultdate111 = new Date(entry.getValue().getLastTimeStamp());
                        Log.v (LOG_TAG,"Last timestamp: " + sdf11.format(resultdate111));
                    }
                    
                }                                       
            }
        
            // TODO Show results in a listview instead of finishing activity.
            // Launch app usage list activity.
           /* Intent usageIntent = new Intent();
            usageIntent.setAction("com.example.phoneuse.USAGE_LIST");
            startActivity(usageIntent);*/
            
            finish();
            break;
        default:
            break;
        }
    }
    
    /**
     * onResume method to dynamically show data as tracking progresses.
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        Log.v (LOG_TAG, "mainService is: " + mMainService);
        // Show data dynamically.
        if (mMainService != null) {
            for (Map.Entry<String, Long> entry : mMainService.mForegroundActivityMap.entrySet()) {
                Log.v(LOG_TAG, " App name : " + entry.getKey() + " Time used: " + entry.getValue()
                        / 1000000000);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        
        Log.v (LOG_TAG, "onDestroy activity");
        if (mMainService != null) {
            
            Toast.makeText(mContext, "Phone used for: " + mMainService.phoneUsedTime() + "seconds",
                    Toast.LENGTH_LONG).show();
        }
        
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

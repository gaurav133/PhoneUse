package com.asgj.android.appusage.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.asgj.android.appusage.Utility.UsageInfo;

/**
 * Service to track application usage time for different apps being used by the user.
 * It'll track usage for foreground apps as well as background apps (Music, Call).
 * @author jain.g
 */
public class UsageTrackingService extends Service {
    
    private final IBinder mBinder = new LocalBinder();
    private TelephonyManager telephonyManager;
    private static final String LOG_TAG = UsageTrackingService.class.getSimpleName();
    
    // Hashmap to hold time values for foreground and background activity time values.
    public HashMap<String, Long> foregroundActivityMap, backgroundActivityMap;
    public HashMap<String, Integer> callDetailsMap;
    public ArrayList<UsageInfo> listMusicPlayTimes;
    
    // Interval tree to store time durations music was being played.
    
    
    int index = 0;
    newThreadForForeground newThread;
    
    private boolean isRunningForegroundAppsThread = false,
            isRunningBackgroundApps = false,
            isFirstTimeStartForgroundAppService = false, isScreenOn = false,
            isMusicPlaying = false,
            isMusicStarted = false;
    
    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private ApplicationInfo mApplicationInfo;
    private ComponentName mComponentName;
    
    private long mPreviousStartTime;
    private String mPackageName;
    private String mCurrentAppName, mPreviousAppName;
    
    private Context mContext = null;
    private long startTime, usedTime, startTimestamp, endTimestamp;
    private long musicListenTime;
    private long musicStartTime, musicStopTime;
    private long musicStartTimeStamp, musicStopTimeStamp;
    
    // Broadcast receiver to receive screen wake up events.
    private BroadcastReceiver screenWakeUp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                isRunningForegroundAppsThread = true;
                isScreenOn = true;
                startThread();
                Log.v(LOG_TAG, "Screen is on");
            }
        }
    };
    
    /*// Broadcast Receiver for Music play.
    private BroadcastReceiver musicPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v(LOG_TAG, "Music play started");
            Log.v (LOG_TAG, "Intent received: " + intent.getExtras().toString());
            Log.v(LOG_TAG, "intent action:" + intent.getAction());
            
            // Check if this is first time music has started after app start.
            if (isFirstTimeStartBackgroundAppService) {
                isFirstTimeStartBackgroundAppService = false;
                startTimeBackground = System.nanoTime();
            }
            if (isMusicStopped) {
                startTimeBackground = System.nanoTime();
            }
            isMusicStopped = false;
            // isMusicPlaying = true;
            // isRunningBackgroundAppsThread = true;
        }
    };
    
    // Broadcast Receiver for Music pause.
    private BroadcastReceiver musicPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v(LOG_TAG, "Music paused");
            musicListenTime += (System.nanoTime() - startTimeBackground);
            isMusicStopped = true;
        }
    };*/
    
    // Broadcast receiver to catch screen dim event (Means user not using phone other than attending a call or listening music.)
    private BroadcastReceiver screenDim = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.v(LOG_TAG, "SCREEN IS OFF");
                isFirstTimeStartForgroundAppService = false;
                isScreenOn = false;
                isRunningForegroundAppsThread = false;
                
                // If screen dim, and user isn't listening to songs or talking, then update boolean variables.
                if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE && !isMusicPlaying()) {
                    isRunningBackgroundApps = false;
                    isMusicStarted = false;
                } else if (!isMusicPlaying()) {
                    isMusicStarted = false;
                }
                // updateEndTime();
            }
        }
    };
    
    /**
     * Method to check whether music is playing.
     * @return true, if music is playing, false otherwise.
     */
    public boolean isMusicPlaying() {
        // Have to check time when screen off but music playing / call being taken.
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        isMusicPlaying = audioManager.isMusicActive();
        return isMusicPlaying;
    }

    /**
     * Calculate total time for which phone is used.
     */
    public double phoneUsedTime() {
        for (Map.Entry<String, Long> entry : foregroundActivityMap.entrySet()) {
            usedTime += entry.getValue() / 1000000000;
        }
        return usedTime;
    }

    /**
     * Local binder class to return an instance of this service for interaction with activity.
     */
    public class LocalBinder extends Binder {
        // Return service instance from this class.
        public UsageTrackingService getInstance() {
            return UsageTrackingService.this;
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mContext = this;
        
        // Set up broadcast receivers that this service uses.
        setUpReceivers(true);
        
        // Initialize hash-maps to hold time values.
        foregroundActivityMap = new HashMap<String, Long>();
        backgroundActivityMap = new HashMap<String, Long>();
        callDetailsMap = new HashMap<String, Integer>();
        listMusicPlayTimes = new ArrayList<UsageInfo>();
        
        // Starting time from which calculation needs to be done.
        startTime = System.nanoTime();
        
        Log.v(LOG_TAG, "Service 1 created");
    }

    private void setUpReceivers(boolean register) {
        if (register) {
            IntentFilter wakeUpFilter = new IntentFilter("android.intent.action.SCREEN_ON");
            IntentFilter dimFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
            // Register receivers.
            registerReceiver(screenWakeUp, wakeUpFilter);
            registerReceiver(screenDim, dimFilter);
        } else {
            unregisterReceiver(screenWakeUp);
            unregisterReceiver(screenDim);
        }
    }

    private final class newThreadForForeground implements Runnable {
        /*public HashMap<String, Double> getList() {
            return foregroundMap;
        }*/

        HashMap<String, Long> foregroundMap = new HashMap<String, Long>();
        /**
         * Initialize foreground map to 0 values.
         */
        private void initializeMap() {
            for (Map.Entry<String, Long> entry : foregroundMap.entrySet()) {
                entry.setValue(0L);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            if (isFirstTimeStartForgroundAppService) {
                initializeMap();
                mPreviousStartTime = startTime;

                // Initially, when service is started, application name would be Phone Use.
                mPreviousAppName = mContext.getString(R.string.app_name);
                foregroundMap.put(mPreviousAppName, 0L);
                isFirstTimeStartForgroundAppService = false;

                foregroundMap.put(mPreviousAppName, 0.0);
            }
            
            // Next time when screen becomes ON again, update foreground map to hold previous values. 
            if (isScreenOn) {
                
                // Update start time.
                mPreviousStartTime = System.nanoTime();
                foregroundMap = foregroundActivityMap;
            }
            
            // If any foreground app is running or background app (music is playing).
            while (isRunningForegroundAppsThread || isRunningBackgroundApps) {
                
                mActivityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
                mPackageManager = mContext.getPackageManager();
                List<ActivityManager.RunningTaskInfo> taskInfo = mActivityManager.getRunningTasks(1);
                
                // String activity = taskInfo.get(0).topActivity.getClassName();
                mComponentName = taskInfo.get(0).topActivity;
                mPackageName = mComponentName.getPackageName();
                try {
                    mApplicationInfo = mPackageManager.getApplicationInfo(mPackageName, 0);
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mCurrentAppName = (String) mPackageManager.getApplicationLabel(mApplicationInfo);
                
                // If the present application is different from the previous application, update the previous app time.
                if (mCurrentAppName != mPreviousAppName) {
                    long time = System.nanoTime();
                    if (foregroundMap.containsKey(mPreviousAppName)) {
                        foregroundMap.put(mPreviousAppName, foregroundMap.get(mPreviousAppName) + (time - mPreviousStartTime));
                    } else {
                        foregroundMap.put(mPreviousAppName, time - mPreviousStartTime);
                    }
                    
                    // Start time for current app.
                    Log.v(LOG_TAG, "I AM CALLED APP NAME CHANGED");
                    mPreviousStartTime = time;
                }
                mPreviousAppName = mCurrentAppName;
                
                // If music is not playing but it was started after tracking started, then update music time.
                if (!isMusicPlaying() && isMusicStarted) {
                    musicStopTime = System.nanoTime();
                    musicStopTimeStamp = System.currentTimeMillis();
                    musicListenTime += (musicStopTime - musicStartTime);
                    isMusicStarted = false;
                    
                    // As music has been stopped add resulting interval to list.
                    UsageInfo intervalNode = new UsageInfo();
                    intervalNode.setmIntervalStartTime(musicStartTimeStamp);
                    intervalNode.setmIntervalEndTime(musicStopTimeStamp);
                    intervalNode.setmIntervalDuration((long) ((intervalNode.getmIntervalEndTime() - intervalNode.getmIntervalStartTime())/1000));
                    listMusicPlayTimes.add(intervalNode);
                    
                } else if (isMusicPlaying() && !isMusicStarted) {
                    // If music has been started after tracking started.
                    isMusicStarted = true;
                    musicStartTimeStamp = System.currentTimeMillis();
                    musicStartTime = System.nanoTime();
                }
                
                long time = System.nanoTime();
                if (foregroundMap.containsKey(mPreviousAppName)) {
                    foregroundMap.put(mPreviousAppName, foregroundMap.get(mPreviousAppName) + (time - mPreviousStartTime));
                } else {
                    foregroundMap.put(mPreviousAppName, time - mPreviousStartTime);
                }
                storeMap(foregroundMap);
             
            }               
        }
    }

    private void storeMap(HashMap<String, Long> h) {
        foregroundActivityMap.putAll(h);
        
        for (Map.Entry<String, Long> entry : foregroundActivityMap.entrySet()) {
            Log.v (LOG_TAG, "APP NAME: " + entry.getKey() + "TIME: " + entry.getValue()/1000000000);
        }
        
    }        
        
    
    
    /**
     * Get call logs for a particular duration.
     * @param startTime Starting time from which call logs are desired (Inclusive).
     * @param endTime End time upto which call logs are desired (Exclusive).
     * @return HashMap containing filtered call log entries for given time interval.
     */
    private HashMap<String, Integer> getCallDetails(long startTime, long endTime) {

        Cursor managedCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, CallLog.Calls.DATE + " DESC");
        
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        
        while (managedCursor.moveToNext()) {
            
            String callDate = managedCursor.getString(date);
            
              // Only add if the call times overlap with tracking times.
            if ((Double.parseDouble(callDate) <= startTime && (Double.parseDouble(callDate) + duration) >= startTime)
                    || (Double.parseDouble(callDate) > startTime && Double
                            .parseDouble(callDate) < endTime)) {
                
                // Add the details in hashmap.
                Log.v (LOG_TAG, "callDate: " + callDate);
                Log.v (LOG_TAG, "Duration: " + duration);
                
                callDetailsMap.put(callDate, duration);
            }
            
        }
        return callDetailsMap;
    }

    /**
     * Starts a new thread to track foreground apps time.
     */
    public void startThread() {
        newThread = new newThreadForForeground();
         (new Thread(newThread)).start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        
        Log.v (LOG_TAG, "Service unbind");
        endTimestamp = System.currentTimeMillis();
        
        // Check whether music playing in background while we are stopping
        // tracking.
        if (isMusicPlaying()) {
            Log.v (LOG_TAG, "Music is playing");
            musicStopTimeStamp = System.currentTimeMillis();
            musicListenTime += (System.nanoTime() - musicStartTime);
            
         // As music has been stopped add resulting interval to list.
            UsageInfo intervalNode = new UsageInfo();
            intervalNode.setmIntervalStartTime(musicStartTimeStamp);
            intervalNode.setmIntervalEndTime(musicStopTimeStamp);
            intervalNode.setmIntervalDuration((long) ((intervalNode.getmIntervalEndTime() - intervalNode.getmIntervalStartTime())/1000));
            listMusicPlayTimes.add(intervalNode);
        }
        
        // Get call details for given timestamps.
        callDetailsMap = getCallDetails(startTimestamp, endTimestamp);
        
        // Unregister receivers.
        setUpReceivers(false);
        
        isRunningForegroundAppsThread = false;
        isRunningBackgroundApps = false;
        isMusicStarted = false;
        
        // Display list. 
        ListIterator<TimeIntervalNode> iterator = listMusicPlayTimes.listIterator();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    

        while (iterator.hasNext()) {
        	UsageInfo node = iterator.next();
            
            
            Log.v (LOG_TAG, "startTime : " + new Date((long) node.getmIntervalStartTime()));
            Log.v (LOG_TAG, "endTime : " + new Date((long) node.getmIntervalEndTime()));
            Log.v (LOG_TAG, "duration : " + node.getmIntervalDuration());
        }
        
        Log.v (LOG_TAG, "list : " + listMusicPlayTimes);
        Log.v(LOG_TAG, "You listened to music for: " + musicListenTime / 1000000000 + "seconds");
        
        // If music was played, then only add data to map.
        if (musicListenTime != 0) {
            backgroundActivityMap.put("music", musicListenTime);
        }
        
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        
                
        Log.v(LOG_TAG, "onBind Call");
        isRunningForegroundAppsThread = true;
        isFirstTimeStartForgroundAppService = true;
        
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        startTimestamp = System.currentTimeMillis();
        
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { 
        isPermissionGranted();
        } else {
        startThread();
        
        // If music is already playing when tracking started.
        if (isMusicPlaying()) {
            isMusicStarted = true;
            isRunningBackgroundApps = true;
            musicStartTimeStamp = System.currentTimeMillis();
            musicStartTime = System.nanoTime();
            }
        }
        return mBinder;
    }
    
    /**
     * Check whether permission has been granted for accessing Usage stats manager.
     * @return true if permission has been granted, false otherwise.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean isPermissionGranted() {
        
        final UsageStatsManager usageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0,  System.currentTimeMillis());

       // Log.v (LOG_TAG, "Query stats: " + queryUsageStats);
        return !queryUsageStats.isEmpty();
    }
    
}

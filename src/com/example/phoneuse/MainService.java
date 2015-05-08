package com.example.phoneuse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

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

/**
 * Service to track application usage time for different apps being used by the user.
 * It'll track usage for foreground apps as well as background apps (Music, Call).
 * @author jain.g
 */
public class MainService extends Service {
    
    private final IBinder mBinder = new LocalBinder();
    private TelephonyManager telephonyManager;
    
    // Hashmap to hold time values for foreground and background activity time values.
    HashMap<String, Double> foregroundActivityMap, backgroundActivityMap;
    HashMap<String, Integer> callDetailsMap;
    ArrayList<TimeIntervalNode> listMusicPlayTimes;
    
    // Interval tree to store time durations music was being played.
    
    
    int index = 0;
    newThreadForForeground newThread;
    
    private boolean isRunningForegroundAppsThread = false,
            isRunningBackgroundApps = false,
            isFirstTimeStartForgroundAppService = false,
            isFirstTimeStartBackgroundAppService = false, isScreenOn = false,
            isMusicPlaying = false,
            isMusicStarted = false;
    
    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private ApplicationInfo mApplicationInfo;
    private ComponentName mComponentName;
    
    private double mPreviousStartTime;
    private String mPackageName;
    private String mCurrentAppName, mPreviousAppName;
    
    private Context mContext = null;
    private double startTime, usedTime, startTimestamp, endTimestamp;
    private double musicListenTime;
    private double musicStartTime, musicStopTime;
    private double musicStartTimeStamp, musicStopTimeStamp;
    
    // Broadcast receiver to receive screen wake up events.
    private BroadcastReceiver screenWakeUp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                isRunningForegroundAppsThread = true;
                isScreenOn = true;
                startThread();
                Log.v("gaurav", "Screen is on");
            }
        }
    };
    
    /*// Broadcast Receiver for Music play.
    private BroadcastReceiver musicPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v("gaurav", "Music play started");
            Log.v ("gaurav", "Intent received: " + intent.getExtras().toString());
            Log.v("gaurav", "intent action:" + intent.getAction());
            
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
            Log.v("gaurav", "Music paused");
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
                Log.v("gaurav", "SCREEN IS OFF");
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
        for (Map.Entry<String, Double> entry : foregroundActivityMap.entrySet()) {
            usedTime += entry.getValue() / 1000000000;
        }
        return usedTime;
    }

    /**
     * Local binder class to return an instance of this service for interaction with activity.
     */
    public class LocalBinder extends Binder {
        // Return service instance from this class.
        MainService getInstance() {
            return MainService.this;
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
        foregroundActivityMap = new HashMap<String, Double>();
        backgroundActivityMap = new HashMap<String, Double>();
        callDetailsMap = new HashMap<String, Integer>();
        listMusicPlayTimes = new ArrayList<TimeIntervalNode>();
        
        // Starting time from which calculation needs to be done.
        startTime = System.nanoTime();
        
        Log.v("gaurav", "Service 1 created");
    }

    private void setUpReceivers(boolean register) {
        if (register) {
            IntentFilter wakeUpFilter = new IntentFilter("android.intent.action.SCREEN_ON");
            IntentFilter dimFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
           /* IntentFilter musicPauseFilter = new IntentFilter(
                    "android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
            IntentFilter musicPlayFilter = new IntentFilter(
                    "android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
            
              musicPlayFilter.addAction("com.android.music.metachanged");
              
              musicPlayFilter.addAction("com.htc.music.metachanged");
              
              musicPlayFilter.addAction("fm.last.android.metachanged");
              musicPlayFilter
              .addAction("com.sec.android.app.music.metachanged");
              musicPlayFilter.addAction("com.nullsoft.winamp.metachanged");
              musicPlayFilter.addAction("com.amazon.mp3.metachanged");
              musicPlayFilter.addAction("com.miui.player.metachanged");
              musicPlayFilter.addAction("com.real.IMP.metachanged");
              musicPlayFilter.addAction("com.sonyericsson.music.metachanged");
              musicPlayFilter.addAction("com.rdio.android.metachanged");
              musicPlayFilter
              .addAction("com.samsung.sec.android.MusicPlayer.metachanged");
              musicPlayFilter.addAction("com.andrew.apollo.metachanged");
              
              musicPlayFilter.addAction("com.android.music.playstatechanged");
              musicPlayFilter.addAction("com.android.music.playbackcomplete");
              musicPlayFilter.addAction("com.android.music.queuechanged");*/
             
            
            // Register receivers.
            registerReceiver(screenWakeUp, wakeUpFilter);
            registerReceiver(screenDim, dimFilter);
         //   registerReceiver(musicPlay, musicPlayFilter);
         //   registerReceiver(musicPause, musicPauseFilter);
        } else {
            unregisterReceiver(screenWakeUp);
            unregisterReceiver(screenDim);
         //   unregisterReceiver(musicPlay);
         //   unregisterReceiver(musicPause);
        }
    }

    private final class newThreadForForeground implements Runnable {
        /*public HashMap<String, Double> getList() {
            return foregroundMap;
        }*/

        HashMap<String, Double> foregroundMap = new HashMap<String, Double>();
        /**
         * Initialize foreground map to 0 values.
         */
        private void initializeMap() {
            for (Map.Entry<String, Double> entry : foregroundMap.entrySet()) {
                entry.setValue(0.0);
            }
        }

        @Override
        public void run() {
            if (isFirstTimeStartForgroundAppService) {
                initializeMap();
                mPreviousStartTime = startTime;
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
                
                if (mCurrentAppName != mPreviousAppName) {
                    double time = System.nanoTime();
                    if (foregroundMap.containsKey(mPreviousAppName)) {
                        foregroundMap.put(mPreviousAppName, foregroundMap.get(mPreviousAppName) + (time - mPreviousStartTime));
                    } else {
                        foregroundMap.put(mPreviousAppName, time - mPreviousStartTime);
                    }
                    
                    // Start time for current app.
                    Log.v("gaurav", "I AM CALLED APP NAME CHANGED");
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
                    TimeIntervalNode intervalNode = new TimeIntervalNode();
                    intervalNode.startTime = musicStartTimeStamp;
                    intervalNode.endTime = musicStopTimeStamp;
                    intervalNode.duration = (int) ((intervalNode.endTime - intervalNode.startTime)/1000);
                    listMusicPlayTimes.add(intervalNode);
                    
                } else if (isMusicPlaying() && !isMusicStarted) {
                    // If music has been started after tracking started.
                    isMusicStarted = true;
                    musicStartTimeStamp = System.currentTimeMillis();
                    musicStartTime = System.nanoTime();
                }
            }
            double time = System.nanoTime();
            if (foregroundMap.containsKey(mPreviousAppName)) {
                foregroundMap.put(mPreviousAppName, foregroundMap.get(mPreviousAppName) + (time - mPreviousStartTime));
            } else {
                foregroundMap.put(mPreviousAppName, time - mPreviousStartTime);
            }
            storeMap(foregroundMap);
            
        }
    }

    private void storeMap(HashMap<String, Double> h) {
        foregroundActivityMap.putAll(h);
        
        for (Map.Entry<String, Double> entry : foregroundActivityMap.entrySet()) {
            Log.v ("gaurav", "APP NAME: " + entry.getKey() + "TIME: " + entry.getValue()/1000000000);
        }
        
    }
    
    /**
     * Get call logs for a particular duration.
     */
    private void getCallDetails() {

        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, CallLog.Calls.DATE + " DESC");
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :");
        while (managedCursor.moveToNext()) {
            
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String dateStr = new SimpleDateFormat("dd:MM:yyyy", Locale.ENGLISH).format(callDayTime);
        
              // Only add if the call times overlap with tracking times.
            if ((Double.parseDouble(callDate) <= startTimestamp && (Double.parseDouble(callDate) + duration) >= startTimestamp)
                    || (Double.parseDouble(callDate) > startTimestamp && Double
                            .parseDouble(callDate) <= endTimestamp)) {
                
                // Add the details in hashmap.
                Log.v ("gaurav", "number: " + phNumber);
                Log.v ("gaurav", "callType: " + callType);
                Log.v ("gaurav", "callDate: " + callDate);
                Log.v ("gaurav", "callDayTime: " + callDayTime);
                Log.v ("gaurav", "Duration: " + duration);
                Log.v ("gaurav", "dateStr: " + dateStr);
                
                callDetailsMap.put(callDate, duration);
            }
            
        }

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
        
        Log.v ("gaurav", "Service unbind");
        endTimestamp = System.currentTimeMillis();
        
        // Check whether music playing in background while we are stopping
        // tracking.
        if (isMusicPlaying()) {
            Log.v ("gaurav", "Music is playing");
            musicStopTimeStamp = System.currentTimeMillis();
            musicListenTime += (System.nanoTime() - musicStartTime);
            
         // As music has been stopped add resulting interval to list.
            TimeIntervalNode intervalNode = new TimeIntervalNode();
            intervalNode.startTime = musicStartTimeStamp;
            intervalNode.endTime = musicStopTimeStamp;
            intervalNode.duration = (int) ((intervalNode.endTime - intervalNode.startTime)/1000);
            listMusicPlayTimes.add(intervalNode);
        }
        
        getCallDetails();
        // Unregister receivers.
        setUpReceivers(false);
        
        isRunningForegroundAppsThread = false;
        isRunningBackgroundApps = false;
        isMusicStarted = false;
        
        // Display list. 
        ListIterator<TimeIntervalNode> iterator = listMusicPlayTimes.listIterator();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
        
        while (iterator.hasNext()) {
            TimeIntervalNode node = iterator.next();
            
            
            Log.v ("gaurav", "startTime : " + new Date((long) node.startTime));
            Log.v ("gaurav", "endTime : " + new Date((long) node.endTime));
            Log.v ("gaurav", "duration : " + node.duration);
        }
        
        Log.v ("gaurav", "list : " + listMusicPlayTimes);
        Log.v("gaurav", "You listened to music for: " + musicListenTime / 1000000000 + "seconds");
        
        // If music was played, then only add data to map.
        if (musicListenTime != 0) {
            backgroundActivityMap.put("music", musicListenTime);
        }
        
        // At this point we need to check call logs, and display the calls made during tracking period.
        
        
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        
                
        Log.v("gaurav", "onBind Call");
        isRunningForegroundAppsThread = true;
        isFirstTimeStartForgroundAppService = true;
        isFirstTimeStartBackgroundAppService = true;
        
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        startTimestamp = System.currentTimeMillis();
        
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { 
        isPermissionGranted();
        } else {
        startThread();
        
        // If music is already playing when tracking started.
        if (isMusicPlaying()) {
            // musicPlay = true;
            isMusicStarted = true;
            isRunningBackgroundApps = true;
            musicStartTimeStamp = System.currentTimeMillis();
            musicStartTime = System.nanoTime();
            //isMusicStopped = false;
        }
        // TODO Auto-generated method stub
        
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

       // Log.v ("gaurav", "Query stats: " + queryUsageStats);
        return !queryUsageStats.isEmpty();
    }
    
}

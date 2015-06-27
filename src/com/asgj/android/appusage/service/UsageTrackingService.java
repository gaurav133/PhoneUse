package com.asgj.android.appusage.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.database.PhoneUsageDatabase;

/**
 * Service to track application usage time for different apps being used by the user.
 * It'll track usage for foreground apps as well as background apps (Music, Call).
 * @author jain.g
 */
@SuppressLint("InlinedApi")
public class UsageTrackingService extends Service implements Comparator<UsageStats> {

    public interface provideData {
        public void provideMap(HashMap<String, Long> map);
    }

    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    private static final long MUSIC_THRESHOLD_TIME = 60;
    private final LocalBinder mBinder = new LocalBinder();
    private static final String LOG_TAG = UsageTrackingService.class.getSimpleName();
    private PhoneUsageDatabase mDatabase;
    private Timer mTimer;

    // Hash-map to hold time values for foreground and background activity time values.
    public HashMap<String, Integer> mCallDetailsMap;
    public ArrayList<UsageInfo> mListMusicPlayTimes;

    int mIndex = 0;
    BackgroundTrackingTask mBgTrackingTask;
    BackgroundTrackingTask.TimerTs mTimerTask;
    Calendar mStartTrackingCalendar, mEndTrackingCalendar;

    private boolean mIsRunningForegroundAppsThread = false,
            mIsFirstTimeStartForgroundAppService = false, mIsMusicPlaying = false,
            mIsMusicStarted = false, mIsContinueTracking = false, mIsMusicPlayingAtStart = false, mIsTrackingOn = false,
            mIsScreenOff = false;

    private KeyguardManager mKeyguardManager;
    private ActivityManager mActivityManager;
    private UsageStatsManager mUsageStatsManager;
    private List<UsageStats> mUsageList;
    private ListIterator<UsageStats> mUsageListIterator;

    private long mPreviousStartTime;
    private String mPackageName;
    private String mCurrentAppName, mPreviousAppName;

    private Context mContext = null;
    private long mStartTime;
    private long mMusicStartTime, mMusicStopTime;
    private long mPreviousAppStartTimeStamp, mPreviousAppExitTimeStamp, mMusicStartTimeStamp, mMusicStopTimeStamp;

    // Broadcast receiver to receive screen wake up events.
    public BroadcastReceiver dataProvideReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("com.android.asgj.appusage.action.DATA_PROVIDE")) {
                Log.v(LOG_TAG, "Provide data to activity.");

                if (mBgTrackingTask != null && mBgTrackingTask.foregroundMap != null
                        && mIsScreenOff == false) {
                    long duration = Utils.getTimeInSecFromNano(System.nanoTime()
                            - mPreviousStartTime);
                    if (duration > 0L) {
                        if (mBgTrackingTask.foregroundMap.containsKey(mPreviousAppName)) {
                            mBgTrackingTask.foregroundMap
                                    .put(mPreviousAppName,
                                            mBgTrackingTask.foregroundMap.get(mPreviousAppName)
                                                    + duration);
                        } else {
                            mBgTrackingTask.foregroundMap.put(mPreviousAppName,
                                    duration);
                        }
                    }
                    if (mBinder.interfaceMap != null) {
                        mBinder.interfaceMap.provideMap(mBgTrackingTask.foregroundMap);
                    doHandlingOnApplicationClose();
                    
                    doHandlingForApplicationStarted();
                    }
                }
            }
        }
    };

    // Broadcast receiver to receive screen wake up events.
    private BroadcastReceiver screenWakeUpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                Log.v(LOG_TAG, "Screen is on");

                // Check whether key-guard is locked or not.
                if (mKeyguardManager.isKeyguardLocked()) {
                    // Bypass to screenUserPresent receiver.
                    mIsScreenOff = false;
                    mPreviousStartTime = System.nanoTime();
                    mPreviousAppStartTimeStamp = System.currentTimeMillis();
                } else {
                    
                    mIsRunningForegroundAppsThread = true;
                    
                    // Update mPreviousStartTime and start timestamp.
                    mPreviousStartTime = System.nanoTime();
                    mPreviousAppStartTimeStamp = System.currentTimeMillis();
                    
                    // If thread isn't already running. Start it again.
                    mIsScreenOff = false;
                    mTimerTask.cancel();

                    mTimerTask = mBgTrackingTask.new TimerTs();
                    mTimer.schedule(mTimerTask, 0, 1000);
                }

            }
        }
    };

    // Broadcast receiver to receive screen wake up events.
    private BroadcastReceiver timeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.TIME_TICK")) {
                
                // Check whether present time is 00:00, dump data and clear preference at that time.
                java.text.DateFormat dateFormat = SimpleDateFormat.getTimeInstance();
                String time = dateFormat.format(new Date(System.currentTimeMillis()));
                Log.v(LOG_TAG, "Time is: " + time);

                Calendar previousCalendar, presentCalendar;
                presentCalendar = Calendar.getInstance();
                previousCalendar = Calendar.getInstance();

                previousCalendar.setTimeInMillis(presentCalendar.getTimeInMillis() - 60 * 1000);

                // If present date is greater than previous date.
                if (Utils.compareDates(presentCalendar, previousCalendar) == 1) {

                    Log.v (LOG_TAG, "It's midnight, dump data to DB");
                    UsageSharedPrefernceHelper.clearPreference(mContext);

                    if (mIsScreenOff == false) {
                        long currentTime = System.nanoTime();

                        UsageInfo usageInfo = new UsageInfo();
                        usageInfo.setmIntervalStartTime(mPreviousAppStartTimeStamp);
                        usageInfo.setmIntervalEndTime(mPreviousAppExitTimeStamp);
                        usageInfo.setmIntervalDuration(Utils.getTimeInSecFromNano(currentTime - mPreviousStartTime));
                        mDatabase.insertApplicationEntry(mPreviousAppName, usageInfo);

                        mPreviousStartTime = currentTime;
                        mPreviousAppStartTimeStamp = System.currentTimeMillis();
                    }

                    initializeMap(mBgTrackingTask.foregroundMap);

                    // MUSIC DATA.
                    if (isMusicPlaying()) {
                        doHandleForMusicClose();
                        
                        mIsMusicStarted = true;
                        mMusicStartTime = System.nanoTime();
                        mMusicStartTimeStamp = System.currentTimeMillis();
                        
                        mListMusicPlayTimes.clear();
                    }
                }
            }
        }
    };
    
    // Broadcast receiver to receive screen wake up events.
    private BroadcastReceiver screenUserPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.USER_PRESENT")) {
                Log.v(LOG_TAG, "Screen user present");
                mIsRunningForegroundAppsThread = true;
                
                // Update mPrevioustime and start time-stamp.
                mPreviousStartTime = System.nanoTime();
                mPreviousAppStartTimeStamp = System.currentTimeMillis();

                mTimerTask.cancel();

                mTimerTask = mBgTrackingTask.new TimerTs();
                mTimer.schedule(mTimerTask, 0, 1000);

                mIsScreenOff = false;
            }
        }
    };

    // Broadcast receiver to catch screen dim event (Means user not using phone other than attending a call or listening music.)
    private BroadcastReceiver screenDimReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.v(LOG_TAG, "SCREEN IS OFF");
                
                mIsScreenOff = true;
                // Update data, only if we're getting screen dim state from foreground apps running state.
                // Corner case - Screen on and locked, again screen turns dim. Avoid data update for this.
                if (mIsRunningForegroundAppsThread == true) {

                    
                    
                    Log.v (LOG_TAG, "screen Dim -- mForegroundMap after reinitializtion is : " + mBgTrackingTask.foregroundMap);
                    
                    long time = System.nanoTime();
                    long duration = Utils.getTimeInSecFromNano(time - mPreviousStartTime);

                    if (duration > 0L) {
                        if (mBgTrackingTask.foregroundMap.containsKey(mPreviousAppName)) {
                            mBgTrackingTask.foregroundMap.put(mPreviousAppName, mBgTrackingTask.foregroundMap.get(mPreviousAppName) + duration);
                        } else {
                            mBgTrackingTask.foregroundMap.put(mPreviousAppName, duration);
                        }
                    }
                    
                    doHandlingOnApplicationClose();
                }
                
                // If screen dim, and user isn't listening to songs or talking, then update boolean variables.
                if (!isMusicPlaying()) {
                    mIsMusicStarted = false;

                    mTimerTask.cancel();
                    mTimerTask = mBgTrackingTask.new TimerTs();
                        mTimer.schedule(mTimerTask, 0, 120000);
                }
                mIsRunningForegroundAppsThread = false;
                
            }
        }
    };

    /**
     * Perform clean up tasks in this method, as activity will restart after this.
     */
    public void onTaskRemoved(Intent rootIntent) {
        saveDataOnKill();
    };

    @Override
    public void onTrimMemory(int level) {
        // TODO Auto-generated method stub
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_COMPLETE && UsageSharedPrefernceHelper.isServiceRunning(mContext)) {
            saveDataOnKill();
        }
    }


    /**
     * Method to return current data for music (for today) for displaying in Music tab.
     * @return ArrayList containing objects of {@code UsageInfo} types.
     */
    public ArrayList<UsageInfo> getCurrentDataForMusic() {
        
        ArrayList<UsageInfo> currentDataForMusic = new ArrayList<UsageInfo>();

        if (UsageSharedPrefernceHelper.isServiceRunning(mContext) && isMusicPlaying()) {
            
            // Add an entry from start time of music to present time.
            long time = System.nanoTime();
            
            // Add this interval to list.
            UsageInfo info = new UsageInfo();
            info.setmIntervalStartTime(mMusicStartTimeStamp);
            info.setmIntervalEndTime(System.currentTimeMillis());
            info.setmIntervalDuration(Utils.getTimeInSecFromNano(time - mMusicStartTime));

            // Check whether this time-stamp already in list.
            ListIterator<UsageInfo> iterator = currentDataForMusic.listIterator();

            while (iterator.hasNext()) {
                UsageInfo musicInfo = iterator.next();

                if (musicInfo.getmIntervalStartTime() == mMusicStartTimeStamp) {
                    iterator.remove();
                    break;
                }
            }
            currentDataForMusic.add(info);
        }
        

        return currentDataForMusic;
    }
    
    /**
     * Method to check whether music is playing.
     * @return true, if music is playing, false otherwise.
     */
    public boolean isMusicPlaying() {
        // Have to check time when screen off but music playing / call being taken.
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mIsMusicPlaying = audioManager.isMusicActive();
        return mIsMusicPlaying;
    }
    /**
     * Local binder class to return an instance of this service for interaction with activity.
     */
    public class LocalBinder extends Binder {
         public provideData interfaceMap;

        // Return service instance from this class.
        public UsageTrackingService getInstance() {
            return UsageTrackingService.this;
        }

        public void setInterface (provideData data) {
            interfaceMap = data;
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mContext = this;

        // Set up broadcast receivers that this service uses.
        setUpReceivers(true);

        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        
        // Initialize hash-maps to hold time values.

        mCallDetailsMap = new HashMap<String, Integer>();
        mListMusicPlayTimes = new ArrayList<UsageInfo>();

        // Starting time from which calculation needs to be done.
        mStartTime = System.nanoTime();
        mPreviousAppStartTimeStamp = System.currentTimeMillis();

        mIsTrackingOn = true;
        // Initialize thread to set up default values.
        initThread(true);
        UsageSharedPrefernceHelper.setServiceRunning(mContext, true);
        Log.v(LOG_TAG, "Service 1 created");
    }

    private void setUpReceivers(boolean register) {
        if (register) {
            IntentFilter wakeUpFilter = new IntentFilter("android.intent.action.SCREEN_ON");
            IntentFilter dimFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
            IntentFilter userPresentFilter = new IntentFilter("android.intent.action.USER_PRESENT");
            IntentFilter timeTickFilter = new IntentFilter("android.intent.action.TIME_TICK");
            
            IntentFilter dataProvideFilter = new IntentFilter("com.android.asgj.appusage.action.DATA_PROVIDE");
            // Register receivers.
            registerReceiver(screenWakeUpReceiver, wakeUpFilter);
            registerReceiver(screenDimReceiver, dimFilter);
            registerReceiver(screenUserPresentReceiver, userPresentFilter);
            registerReceiver(timeTickReceiver, timeTickFilter);
            registerReceiver(dataProvideReceiver, dataProvideFilter);
        } else {
            unregisterReceiver(screenUserPresentReceiver);
            unregisterReceiver(screenWakeUpReceiver);
            unregisterReceiver(screenDimReceiver);
            unregisterReceiver(timeTickReceiver);
            unregisterReceiver(dataProvideReceiver);
        }
    }

    private void doHandlingOnApplicationClose(){
    	 mPreviousAppExitTimeStamp = System.currentTimeMillis();
         long time = System.nanoTime();
         long duration = Utils.getTimeInSecFromNano(time - mPreviousStartTime);

         // In case application usage duration is 0 seconds, just return.
         if (duration == 0) {
             return;
         }

         // As application has changed, we need to dump data to DB.
         UsageInfo usageInfoApp = new UsageInfo();
         usageInfoApp.setmIntervalStartTime(mPreviousAppStartTimeStamp);
         usageInfoApp.setmIntervalEndTime(mPreviousAppExitTimeStamp);
         usageInfoApp.setmIntervalDuration(duration);

         // Insert data to database for previous application.
         mDatabase.insertApplicationEntry(mPreviousAppName, usageInfoApp);
         
         // Insert data to preference too.
         UsageSharedPrefernceHelper.updateTodayDataForApps(mContext, mBgTrackingTask.foregroundMap);
         initializeMap(mBgTrackingTask.foregroundMap);
    }

    private void doHandlingForApplicationStarted() {
        long time = System.nanoTime();
        mPreviousAppStartTimeStamp = System.currentTimeMillis();
        Log.v(LOG_TAG, "I AM CALLED APP NAME CHANGED");
        mPreviousStartTime = time;
    }
    
    @SuppressWarnings("deprecation")
    private boolean isTopApplicationchange() {
        
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
          mActivityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
          mPackageName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();

          mCurrentAppName = mPackageName;
        } else {
           mUsageList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    0, System.currentTimeMillis());
            Collections.sort(mUsageList, this);

            if (mUsageList != null) {
                mUsageListIterator = mUsageList.listIterator();

                UsageStats stats = null;
                while (mUsageListIterator.hasNext()) {
                    stats = mUsageListIterator.next();
                }
                if (stats != null) {

                    mPackageName = stats.getPackageName();

                    mCurrentAppName = mPackageName;
                }
               
            }
        }
          return !mCurrentAppName.equals(mPreviousAppName);

    }
    
    private boolean isNeedToHandleMusicClose(){
    	return !isMusicPlaying() && mIsMusicStarted;
    }
    
    private void doHandleForMusicClose(){
          mMusicStopTime = System.nanoTime();
          mMusicStopTimeStamp = System.currentTimeMillis();

          mIsMusicStarted = false;
          mIsMusicPlayingAtStart = false;

          long duration = Utils.getTimeInSecFromNano(mMusicStopTime - mMusicStartTime);

          if (duration > 0) {
              // As music has been stopped add resulting interval to list.
              UsageInfo usageInfoMusic = new UsageInfo();
              usageInfoMusic.setmIntervalStartTime(mMusicStartTimeStamp);
              usageInfoMusic.setmIntervalEndTime(mMusicStopTimeStamp);
              usageInfoMusic.setmIntervalDuration(duration);

              ListIterator<UsageInfo> iterator = mListMusicPlayTimes.listIterator();

              while (iterator.hasNext()) {
                  UsageInfo info = iterator.next();

                  if (info.getmIntervalStartTime() == mMusicStartTimeStamp) {
                      iterator.remove();
                      break;
                  }
              }
              mListMusicPlayTimes.add(usageInfoMusic);
          }
          
          if (mIsScreenOff == true) {
              mTimerTask.cancel();
              mTimerTask = mBgTrackingTask.new TimerTs();
                  mTimer.schedule(mTimerTask, 0, 120000);
          }
    }

    private void initializeMap( HashMap<String, Long> foregroundMap) {
        foregroundMap.clear();
    }

    private void initLocalMapForThread( HashMap<String, Long> foregroundMap){
    	     initializeMap(foregroundMap);
             mPreviousStartTime = mStartTime;
             Log.v (LOG_TAG, "mPreviousStartTime: " + mPreviousStartTime);

             // Initially, when service is started, application name would be Phone Use.
             mPreviousAppName = mContext.getPackageName();
           //  foregroundMap.put(mPreviousAppName, 0L);
             mIsFirstTimeStartForgroundAppService = false;
    }
    
    private void stopTimer() {
        if (mTimerTask != null && mTimer != null) {
            mTimerTask.cancel();
            mTimer.cancel();
            mTimer.purge();
        }
        mIsTrackingOn = false;
    }

    private final class BackgroundTrackingTask {

        HashMap<String, Long> foregroundMap = new HashMap<String, Long>();
        final class TimerTs extends TimerTask {
            private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread thread, Throwable ex) {
                    if (UsageSharedPrefernceHelper.isServiceRunning(getApplicationContext())) {
                        Intent stopServiceIntent = new Intent(getApplicationContext(), UsageTrackingService.class);
                        stopService(stopServiceIntent);
                        
                        Intent startServiceIntent = new Intent(getApplicationContext(), UsageTrackingService.class);
                        startService(startServiceIntent);
                        
                    }
                    androidDefaultUEH.uncaughtException(thread, ex);
                }
            };

            @Override
            public void run() {
                // TODO Auto-generated method stub

                // In case battery or RAM is low, stop timer.
                if (!Utils.isSufficientBatteryAvailable(mContext)
                        || !Utils.isSufficientRAMAvailable(mContext)) {
                    Log.v(LOG_TAG, "Inside run battery ram low");
                    if (!mIsContinueTracking) {
                        saveDataOnKill();
                    }
                    mIsContinueTracking = true;
                    return;
                }
                if (mIsFirstTimeStartForgroundAppService) {
                    androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
                    Thread.setDefaultUncaughtExceptionHandler(handler);
                    initLocalMapForThread(foregroundMap);
                    return;
                }

            	if (isTopApplicationchange() && !mKeyguardManager.isKeyguardLocked()
                        && mIsRunningForegroundAppsThread == true) {
                    long time = System.nanoTime();
                    long duration = Utils.getTimeInSecFromNano(time - mPreviousStartTime);
                

                    if (duration > 0) {
                        if (foregroundMap.containsKey(mPreviousAppName)) {
                            foregroundMap.put(mPreviousAppName, foregroundMap.get(mPreviousAppName) + Utils.getTimeInSecFromNano(time - mPreviousStartTime));
                        } else {
                            foregroundMap.put(mPreviousAppName, Utils.getTimeInSecFromNano(time - mPreviousStartTime));
                        }
                    }
                    doHandlingOnApplicationClose();
                    // Update mPreviousAppStartTimeStamp.
                    doHandlingForApplicationStarted();
                }
                mPreviousAppName = mCurrentAppName;
                if (mMusicStopTime != 0) {
                    if (Utils.getTimeInSecFromNano(System.nanoTime() - mMusicStopTime) > MUSIC_THRESHOLD_TIME) {
                        if (mListMusicPlayTimes != null && !mListMusicPlayTimes.isEmpty()) {
                            UsageSharedPrefernceHelper.updateTodayDataForMusic(mContext, mListMusicPlayTimes);
                            mListMusicPlayTimes.clear();
                        }
                    }
                }

                // If music is not playing but it was started after tracking started, then update music time.
                if (isNeedToHandleMusicClose()) {
                  doHandleForMusicClose();
                } else if (isMusicPlaying() && mIsMusicPlayingAtStart == false && mIsMusicStarted == false) {
                    // If music has been started after tracking started.
                    mIsMusicStarted = true;

                    // If screen is off and music is playing, then update timer again to 1 sec.
                    if (mIsScreenOff == true) {
                        mTimerTask.cancel();
                        mTimerTask = mBgTrackingTask.new TimerTs();
                        mTimer.schedule(mTimerTask, 0, 1000);
                    }

                        // If it's not the first interval after service start,
                        // check for threshold gap.
                        if (mMusicStopTime > 0) {
                            if (Utils.getTimeInSecFromNano(System.nanoTime() - mMusicStopTime) > MUSIC_THRESHOLD_TIME) {
                                mMusicStartTimeStamp = System.currentTimeMillis();
                                mMusicStartTime = System.nanoTime();
                            }
                        } else {
                            mMusicStartTime = System.nanoTime();
                            mMusicStartTimeStamp = System.currentTimeMillis();
                        }
                    }
                }
            }
        }

       /**
    /**
     * Starts a new thread to track foreground and background application time.
     */
    public void startThread() {
         mBgTrackingTask = new BackgroundTrackingTask();

         // Start timer.
         mTimer = new Timer();
         mTimerTask = mBgTrackingTask.new TimerTs();
         mTimer.schedule(mTimerTask, 0, 1000);
    }

    /**
     * onUnbind is called only when activity is destroyed (either back-press or kill through task manager).
     */
    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub

        Log.v (LOG_TAG, "Service unbind");
        return super.onUnbind(intent);
    }

    public void saveDataOnKill() {
        Log.v(LOG_TAG, "Save data on kill method call");

        mIsRunningForegroundAppsThread = false;
        mIsMusicStarted = false;

        long time = System.nanoTime();
        long duration = Utils.getTimeInSecFromNano(time - mPreviousStartTime);

        Log.v (LOG_TAG, "onDestroy -- mForegroundMap in onDestroy is : " + mBgTrackingTask.foregroundMap);
        

        if (duration > 0) {
        if (mBgTrackingTask.foregroundMap.containsKey(mPreviousAppName)) {
            mBgTrackingTask.foregroundMap.put(mPreviousAppName, mBgTrackingTask.foregroundMap.get(mPreviousAppName) + Utils.getTimeInSecFromNano(time - mPreviousStartTime));
        } else {
            mBgTrackingTask.foregroundMap.put(mPreviousAppName, Utils.getTimeInSecFromNano(time - mPreviousStartTime));
            }
        }

        // Check whether music playing in background while we are stopping
        // tracking.
        if (isMusicPlaying()) {
            Log.v (LOG_TAG, "Music is playing");
          doHandleForMusicClose();
        }

        // As application has changed, we need to dump data to DB.
        doHandlingOnApplicationClose();

        // Dump data to xml shared preference.
        
        if (!mListMusicPlayTimes.isEmpty()) {
            UsageSharedPrefernceHelper.updateTodayDataForMusic(mContext, mListMusicPlayTimes);
            
            // Dump data to DB for music.
            ListIterator<UsageInfo> iterator = mListMusicPlayTimes.listIterator();
            while (iterator.hasNext()) {
                mDatabase.insertMusicEntry(iterator.next());
            }
        }
        
        // Store current date to preferences.
        UsageSharedPrefernceHelper.setCurrentDate(mContext);

        mPreviousAppStartTimeStamp = System.currentTimeMillis();
        mPreviousAppExitTimeStamp = mPreviousAppStartTimeStamp;
        mPreviousStartTime = System.nanoTime();
        mListMusicPlayTimes.clear();
        initializeMap(mBgTrackingTask.foregroundMap);

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.v(LOG_TAG, "onDestroy Service");
        saveDataOnKill();
        UsageSharedPrefernceHelper.setServiceRunning(mContext, false);
        setUpReceivers(false);

        mBgTrackingTask.foregroundMap = null;
        mBgTrackingTask = null;
        mDatabase = null;
        mCallDetailsMap = null;
        mListMusicPlayTimes = null;
        mIsContinueTracking = false;
        mIsRunningForegroundAppsThread = false;
        mIsMusicStarted = false;

        stopTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("isStartingAfterReboot")) {
            boolean isStartFromReboot = intent.getBooleanExtra("isStartingAfterReboot", false);
            if (isStartFromReboot) {
                initThread(false);
            }
        }
        // TODO Auto-generated method stub
        return START_STICKY;
    }
    
    private void initThread(boolean isFirstTime){
    	 mIsRunningForegroundAppsThread = true;
    	 if(isFirstTime)
         mIsFirstTimeStartForgroundAppService = true;
    	 if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
             mUsageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
             mUsageList = new ArrayList<UsageStats>();
         }

         mDatabase = new PhoneUsageDatabase(mContext);
         startThread();
         // If music is already playing when tracking started.
         if (isMusicPlaying()) {
             mIsMusicPlayingAtStart = true;
             mIsMusicStarted = true;
             mMusicStartTimeStamp = System.currentTimeMillis();
             mMusicStartTime = System.nanoTime();
             }
         }
    
    
    @Override
    public LocalBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "onBind Call");
               return mBinder;
    }
    @Override
    public int compare(UsageStats usageStats1, UsageStats usageStats2) {
        // TODO Auto-generated method stub
        return (int) (usageStats1.getLastTimeUsed() - usageStats2.getLastTimeUsed());
    }
  }


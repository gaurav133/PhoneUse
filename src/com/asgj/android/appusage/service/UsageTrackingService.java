package com.asgj.android.appusage.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.database.PhoneUsageDatabase;
import com.asgj.android.appusage.database.PhoneUsageDbHelper;

/**
 * Service to track application usage time for different apps being used by the user.
 * It'll track usage for foreground apps as well as background apps (Music, Call).
 * @author jain.g
 */
public class UsageTrackingService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private TelephonyManager mTelephonyManager;
    private static final String LOG_TAG = UsageTrackingService.class.getSimpleName();
    private PhoneUsageDatabase mDatabase;

    // Hash-map to hold time values for foreground and background activity time values.
    public HashMap<String, Long> mForegroundActivityMap;
    public HashMap<String, Integer> mCallDetailsMap;
    public ArrayList<UsageInfo> mListMusicPlayTimes;

    int mIndex = 0;
    BackgroundTrackingTask mBgTrackingTask;

    private boolean mIsRunningForegroundAppsThread = false,
            mIsRunningBackgroundApps = false,
            mIsFirstTimeStartForgroundAppService = false, isScreenOn = false,
            mIsMusicPlaying = false,
            mIsMusicStarted = false,
            mIsKeyguardLocked = false,
            mIsEndTracking = false;

    private KeyguardManager mKeyguardManager;
    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private ApplicationInfo mApplicationInfo;
    private ComponentName mComponentName;

    private long mPreviousStartTime;
    private String mPackageName;
    private String mCurrentAppName, mPreviousAppName;

    private Context mContext = null;
    private long mStartTime, mUsedTime, mStartTimestamp, mEndTimestamp;
    private long mMusicListenTime;
    private long mMusicStartTime, mMusicStopTime;
    private long mPreviousAppStartTimeStamp, mPreviousAppExitTimeStamp, mMusicStartTimeStamp, mMusicStopTimeStamp;

    // Broadcast receiver to receive screen wake up events.
    private BroadcastReceiver screenWakeUp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                
                // Check whether keyguard is locked or not.
                if (mKeyguardManager.isKeyguardLocked()) {
                 // Bypass to screenUserPresent receiver.
                    mIsKeyguardLocked = true;
                } else {
                    
                    mIsRunningForegroundAppsThread = true;
                    isScreenOn = true;
                    
                    // Update mPreviousStartTime and start timestamp.
                    mPreviousStartTime = System.nanoTime();
                    mPreviousAppStartTimeStamp = System.currentTimeMillis();
                    
                    // If thread isn't already running. Start it again.
                    if (mIsRunningBackgroundApps == false) {
                        startThread();
                    }
                }
                
                Log.v(LOG_TAG, "Screen is on");
            }
        }
    };
    
 // Broadcast receiver to receive screen wake up events.
    private BroadcastReceiver screenUserPresent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.USER_PRESENT")) {
                mIsRunningForegroundAppsThread = true;
                
                // Update mPrevioustime and start time-stamp.
                mPreviousStartTime = System.nanoTime();
                mPreviousAppStartTimeStamp = System.currentTimeMillis();
                
                // If thread isn't already running. Start it again.
                if (mIsRunningBackgroundApps == false) {
                    startThread();
                }
                Log.v(LOG_TAG, "Screen user present");
            }
        }
    };

    // Broadcast receiver to catch screen dim event (Means user not using phone other than attending a call or listening music.)
    private BroadcastReceiver screenDim = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.v(LOG_TAG, "SCREEN IS OFF");
                isScreenOn = false;
                
                // Update data, only if we're getting screen dim state from foreground apps running state.
                // Corner case - Screen on and locked, again screen turns dim. Avoid data update for this.
                if (mIsRunningForegroundAppsThread == true) {
                    doHandlingOnApplicationClose();
                    storeMap(mBgTrackingTask.foregroundMap);
                    
                    // Reinitialize map.
                    initializeMap(mBgTrackingTask.foregroundMap);
                    
                    Log.v (LOG_TAG, "screen Dim -- mForegroundMap after reinitializtion is : " + mBgTrackingTask.foregroundMap);
                    
                    long time = System.nanoTime();
                    if (mForegroundActivityMap.containsKey(mPreviousAppName)) {
                        mForegroundActivityMap.put(mPreviousAppName, mForegroundActivityMap.get(mPreviousAppName) + Utils.getTimeInSecFromNano(time - mPreviousStartTime));
                    } else {
                        mForegroundActivityMap.put(mPreviousAppName, Utils.getTimeInSecFromNano(time - mPreviousStartTime));
                    }
                    
                    Log.v (LOG_TAG, "screen Dim -- mForeground activity Map after updation is : " + mForegroundActivityMap);
                }
                
                // If screen dim, and user isn't listening to songs or talking, then update boolean variables.
                if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE && !isMusicPlaying()) {
                    mIsRunningBackgroundApps = false;
                    mIsMusicStarted = false;
                } else if (!isMusicPlaying()) {
                    mIsMusicStarted = false;
                }
                mIsRunningForegroundAppsThread = false;
                
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
        mIsMusicPlaying = audioManager.isMusicActive();
        return mIsMusicPlaying;
    }

    /**
     * Calculate total time for which phone is used.
     */
    public long phoneUsedTime() {
        Log.v ("gaurav", "Map is: " + mForegroundActivityMap);
        mUsedTime = 0;
        for (Map.Entry<String, Long> entry : mForegroundActivityMap.entrySet()) {
            mUsedTime += entry.getValue();
        }
        return mUsedTime;
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

        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        
        // Initialize hash-maps to hold time values.
        mForegroundActivityMap = new HashMap<String, Long>();
        mCallDetailsMap = new HashMap<String, Integer>();
        mListMusicPlayTimes = new ArrayList<UsageInfo>();

        // Starting time from which calculation needs to be done.
        mStartTime = System.nanoTime();
        mPreviousAppStartTimeStamp = System.currentTimeMillis();
        
        // Here you bind to the service.
        Notification noti = new Notification.Builder(mContext)
        .setContentTitle("App Usage")
        .setContentText("Tracking in progress")
        .setSmallIcon(R.drawable.ic_launcher)
        .build();
        Intent notificationIntent = new Intent(this, UsageTrackingService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        noti.setLatestEventInfo(this, getText(R.string.action_settings),
                getText(R.string.hello_world), pendingIntent);
        startForeground(1, noti);

        UsageSharedPrefernceHelper.setServiceRunning(mContext, true);
        Log.v(LOG_TAG, "Service 1 created");
    }

    private void setUpReceivers(boolean register) {
        if (register) {
            IntentFilter wakeUpFilter = new IntentFilter("android.intent.action.SCREEN_ON");
            IntentFilter dimFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
            IntentFilter userPresentFilter = new IntentFilter("android.intent.action.USER_PRESENT");
            
            // Register receivers.
            registerReceiver(screenWakeUp, wakeUpFilter);
            registerReceiver(screenDim, dimFilter);
            registerReceiver(screenUserPresent, userPresentFilter);
        } else {
            unregisterReceiver(screenUserPresent);
            unregisterReceiver(screenWakeUp);
            unregisterReceiver(screenDim);
        }
    }

    private void doHandlingOnApplicationClose(){
    	 mPreviousAppExitTimeStamp = System.currentTimeMillis();
         long time = System.nanoTime();

         // As application has changed, we need to dump data to DB.
         UsageInfo usageInfoApp = new UsageInfo();
         usageInfoApp.setmIntervalStartTime(mPreviousAppStartTimeStamp);
         usageInfoApp.setmIntervalEndTime(mPreviousAppExitTimeStamp);
         usageInfoApp.setmIntervalDuration(TimeUnit.SECONDS.convert((time - mPreviousStartTime), TimeUnit.NANOSECONDS));

         Log.v (LOG_TAG, "Previous App name not equal:" + mPreviousAppName);
         Log.v (LOG_TAG, "UsageInfo duration: " + usageInfoApp.getmIntervalDuration());
         // Insert data to database for previous application.
         mDatabase.insertApplicationEntry(mPreviousAppName, usageInfoApp);
    }

    private void doHandlingForApplicationStarted() {
        long time = System.nanoTime();
        mPreviousAppStartTimeStamp = System.currentTimeMillis();
        Log.v(LOG_TAG, "I AM CALLED APP NAME CHANGED");
        mPreviousStartTime = time;
    }
    
    private boolean isTopApplicationchange(){
    	  mActivityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
          mPackageManager = mContext.getPackageManager();
          List<ActivityManager.RunningTaskInfo> taskInfo = mActivityManager.getRunningTasks(1);

          mComponentName = taskInfo.get(0).topActivity;
          mPackageName = mComponentName.getPackageName();
          try {
              mApplicationInfo = mPackageManager.getApplicationInfo(mPackageName, 0);
          } catch (NameNotFoundException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
          mCurrentAppName = (String) mPackageManager.getApplicationLabel(mApplicationInfo);
          return mCurrentAppName != mPreviousAppName;

    }
    
    private boolean isNeedToHandleMusicClose(){
    	return !isMusicPlaying() && mIsMusicStarted;
    }
    
    private void doHandleForMusicClose(){
    	  mMusicStopTime = System.nanoTime();
          mMusicStopTimeStamp = System.currentTimeMillis();
          mMusicListenTime += (TimeUnit.SECONDS.convert(mMusicStopTime - mMusicStartTime, TimeUnit.NANOSECONDS));
          mIsMusicStarted = false;

          // As music has been stopped add resulting interval to list.
          UsageInfo usageInfoMusic = new UsageInfo();
          usageInfoMusic.setmIntervalStartTime(mMusicStartTimeStamp);
          usageInfoMusic.setmIntervalEndTime(mMusicStopTimeStamp);
          usageInfoMusic.setmIntervalDuration(TimeUnit.SECONDS.convert(mMusicStopTime - mMusicStartTime, TimeUnit.NANOSECONDS));
          mListMusicPlayTimes.add(usageInfoMusic);
          
          // Insert data to database for previous application.
          mDatabase.insertMusicEntry(usageInfoMusic);

    }
    
    private void initializeMap( HashMap<String, Long> foregroundMap) {
        for (Map.Entry<String, Long> entry : foregroundMap.entrySet()) {
            entry.setValue(0L);
        }
    }


    
    private void initLocalMapForThread( HashMap<String, Long> foregroundMap){
    	     initializeMap(foregroundMap);
             mPreviousStartTime = mStartTime;
             Log.v ("gaurav", "mPreviousStartTime: " + mPreviousStartTime);

             // Initially, when service is started, application name would be Phone Use.
             mPreviousAppName = mContext.getString(R.string.app_name);
           //  foregroundMap.put(mPreviousAppName, 0L);
             mIsFirstTimeStartForgroundAppService = false;
    }
    

    private final class BackgroundTrackingTask implements Runnable {

        HashMap<String, Long> foregroundMap = new HashMap<String, Long>();
        @Override
        public void run() {
            
            if (mIsFirstTimeStartForgroundAppService) {
                initLocalMapForThread(foregroundMap);
            }
            /*if (isScreenOn) {

                // Update start time.
                mPreviousStartTime = System.nanoTime();
                foregroundMap = mForegroundActivityMap;
            }
*/
            // If any foreground application is running or background application (music is playing).
            while (mIsRunningForegroundAppsThread || mIsRunningBackgroundApps) {
                
                              // If the present application is different from the previous application, update the previous app time.
                if (isTopApplicationchange()) {
                    long time = System.nanoTime();
                
                	doHandlingOnApplicationClose();
                    if (foregroundMap.containsKey(mPreviousAppName)) {
                        foregroundMap.put(mPreviousAppName, foregroundMap.get(mPreviousAppName) + TimeUnit.SECONDS.convert((time - mPreviousStartTime), TimeUnit.NANOSECONDS));
                    } else {
                        foregroundMap.put(mPreviousAppName, TimeUnit.SECONDS.convert((time - mPreviousStartTime), TimeUnit.NANOSECONDS));
                    }

                    // Update mPreviousAppStartTimeStamp.
                	doHandlingForApplicationStarted();
                	
                }
                mPreviousAppName = mCurrentAppName;

                // If music is not playing but it was started after tracking started, then update music time.
                if (isNeedToHandleMusicClose()) {
                  doHandleForMusicClose();

                } else if (isMusicPlaying() && !mIsMusicStarted) {
                    // If music has been started after tracking started.
                    mIsMusicStarted = true;
                    mMusicStartTimeStamp = System.currentTimeMillis();
                    mMusicStartTime = System.nanoTime();
                }
            }
            // If tracking has ended, store last application.
            //doHandlingOnEndthread(foregroundMap);
        }
    }

    private void storeMap(HashMap<String, Long> foregroundMap) {
        
        // For each entry in foreground map, update the entry in mForegroundMap
        for (Map.Entry<String, Long> entry : foregroundMap.entrySet()) {
            String key = entry.getKey();
            if (mForegroundActivityMap.containsKey(key)) {
                mForegroundActivityMap.put(key, mForegroundActivityMap.get(key) + entry.getValue());
            } else {
                mForegroundActivityMap.put(key, entry.getValue());
            }
        }
        Log.v (LOG_TAG, "mForegroundActivityMap is : " + mForegroundActivityMap);
    }
    
    private void doHandlingOnEndthread(HashMap<String, Long> foregroundMap){
    	if (mIsEndTracking == true) {
            long time = System.nanoTime();
            if (foregroundMap.containsKey(mPreviousAppName)) {
                foregroundMap.put(mPreviousAppName, foregroundMap.get(mPreviousAppName) + TimeUnit.SECONDS.convert((time - mPreviousStartTime), TimeUnit.NANOSECONDS));
            } else {
                foregroundMap.put(mPreviousAppName, TimeUnit.SECONDS.convert((time - mPreviousStartTime), TimeUnit.NANOSECONDS));
            }
        }
    }

       /**
    /**
     * Starts a new thread to track foreground and background application time.
     */
    public void startThread() {
         mBgTrackingTask = new BackgroundTrackingTask();
         (new Thread(mBgTrackingTask)).start();
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

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.v (LOG_TAG, "onDestroy Service");
        
        mIsRunningForegroundAppsThread = false;
        mIsRunningBackgroundApps = false;
        mIsMusicStarted = false;
        mIsEndTracking = true;
        
        long time = System.nanoTime();
        Log.v (LOG_TAG, "onDestroy -- mForegroundMap in onDestroy is : " + mBgTrackingTask.foregroundMap);
        
        storeMap(mBgTrackingTask.foregroundMap);
        if (mForegroundActivityMap.containsKey(mPreviousAppName)) {
            mForegroundActivityMap.put(mPreviousAppName, mForegroundActivityMap.get(mPreviousAppName) + Utils.getTimeInSecFromNano(time - mPreviousStartTime));
        } else {
            mForegroundActivityMap.put(mPreviousAppName, Utils.getTimeInSecFromNano(time - mPreviousStartTime));
        }
        
        Log.v (LOG_TAG, "Destroy Service -- mForeground Map after updation is : " + mForegroundActivityMap);
        
        mEndTimestamp = System.currentTimeMillis();
        
        // Check whether music playing in background while we are stopping
        // tracking.
        if (isMusicPlaying()) {
            Log.v (LOG_TAG, "Music is playing");
          doHandleForMusicClose();
        }

        // As application has changed, we need to dump data to DB.
        doHandlingOnApplicationClose();
        // Get call details for given time-stamps.
        mCallDetailsMap = Utils.getCallDetails(mContext, mStartTimestamp, mEndTimestamp,mCallDetailsMap);
        
        // Unregister receivers.
        //setUpReceivers(false);
        
        // Display list. 
        ListIterator<UsageInfo> iterator = mListMusicPlayTimes.listIterator();
        mDatabase.exportDatabse(PhoneUsageDbHelper.getInstance(mContext).getDatabaseName());
        UsageSharedPrefernceHelper.setServiceRunning(mContext, false);
        super.onDestroy();
        
        Toast.makeText(mContext, "Phone used for: " + phoneUsedTime() + "seconds",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(intent != null && intent.hasExtra("isStartingAfterReboot")){
    	    boolean isStartFromReboot = intent.getBooleanExtra("isStartingAfterReboot", false);
    		if(isStartFromReboot){
    			initThread(false);
    		}
    	}
        // TODO Auto-generated method stub
        return START_NOT_STICKY;
    }
    
    private void initThread(boolean isFirstTime){
    	 mIsRunningForegroundAppsThread = true;
    	 if(isFirstTime)
         mIsFirstTimeStartForgroundAppService = true;
         
         mDatabase = new PhoneUsageDatabase(mContext);
         mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
         mStartTimestamp = System.currentTimeMillis();

         if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { 
            isPermissionGranted();
         } else {

         startThread();
         // If music is already playing when tracking started.
         if (isMusicPlaying()) {
             mIsMusicStarted = true;
             mIsRunningBackgroundApps = true;
             mMusicStartTimeStamp = System.currentTimeMillis();
             mMusicStartTime = System.nanoTime();
             }
         }

    }
    
    @Override
    public IBinder onBind(Intent intent) {
    	initThread(true);
        Log.v(LOG_TAG, "onBind Call");
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

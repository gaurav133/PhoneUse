package com.example.phoneuse;

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
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.phoneuse.MainService.LocalBinder;

public class MainActivity extends Activity implements OnClickListener {
    private Context mContext;
    private MainService mMainService;
    private UsageStatsManager usageStatsManager;
    private List<UsageStats> queryUsageStats;

    private long time;
    // UI elements.
    Button mServiceStartButton, mServiceStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        init();
    }
    
 // Broadcast Receiver for Music play.
    private BroadcastReceiver musicPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v("gaurav", "Music play started");
            Log.v("gaurav", "intent action:" + intent.getAction());
            
            
            
            

            // Check if this is first time music has started after app start.
           /* if (isFirstTimeStartBackgroundAppService) {
                isFirstTimeStartBackgroundAppService = false;
                startTimeBackground = System.nanoTime();
            }
            if (isMusicStopped) {
                startTimeBackground = System.nanoTime();
            }
            isMusicStopped = false;*/
            // isMusicPlaying = true;
            // isRunningBackgroundAppsThread = true;
        }
    };

    /**
     * Initialize UI controls and event listeners.
     * This method is called everytime the activity is created.
     */
    public void init() {

        // Initialise buttons.
        Button mServiceStartButton = (Button) findViewById(R.id.startButton);
        Button mServiceStopButton = (Button) findViewById(R.id.stopButton);

        // Set listeners.
        mServiceStartButton.setOnClickListener(this);
        mServiceStopButton.setOnClickListener(this);
        
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
        musicPlayFilter.addAction("com.android.music.queuechanged");
        registerReceiver(musicPlay, musicPlayFilter);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMainService = ((LocalBinder) service).getInstance();
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.startButton:
            
             time = System.currentTimeMillis();
             Log.v ("gaurav", "Time: " + time);
             SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
             Date resultdate = new Date(time);
             Log.v ("gaurav",sdf.format(resultdate));
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) { 
               
            // Here you bind to the service.
            Intent startServiceIntent = new Intent();
            startServiceIntent.setClass(this, MainService.class);
            startServiceIntent.setComponent(new ComponentName(this, MainService.class));
            bindService(startServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

            // Home Screen intent.
            Intent intentGoToHomeScreen = new Intent();
            intentGoToHomeScreen.setAction("android.intent.action.MAIN");
            intentGoToHomeScreen.addCategory("android.intent.category.HOME");

            // Test toast.
            Toast.makeText(mContext, "Your phone usage is being calculated now!", Toast.LENGTH_LONG)
                    .show();
            startActivity(intentGoToHomeScreen);
                }
            break;

        case R.id.stopButton:
            /**
             * When user presses stop button, service should be stopped, and data inserted to database.
             */
            // Insert into DB.
           // insertIntoDB();

            // Unbind the service, as no longer needed.
           // unbindService(mConnection);
            
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { 
                usageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
                
                SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd,yyyy HH:mm");   
             // Get current time zone and it's offset from epoch.
                
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                
                Log.v ("gaurav", "CALENDAR OBJECT: " + c);
                TimeZone currentTimeZone = TimeZone.getDefault();
                int offset = currentTimeZone.getRawOffset();
                Date resultdate2 = new Date(offset);
                Log.v ("gaurav", "Current timezone: " + currentTimeZone + " and offset : " + sdf1.format(resultdate2));
                
                long time2 = System.currentTimeMillis();
                
                // Need to query usage stats for present day according to timezone.
                queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, c.getTimeInMillis(), time2);
                 
                Date resultdate1 = new Date(c.getTimeInMillis());
                
                Log.v ("gaurav","Inside stop" + sdf1.format(resultdate1));
                
                SimpleDateFormat sdf11 = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
                Date resultdate11 = new Date(time2);
                Log.v ("gaurav",sdf11.format(resultdate11));
               }
            
            Log.v ("gaurav", "Query stats:" + queryUsageStats);
            // Query stats:
            if(queryUsageStats != null) {
                SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                for (UsageStats usageStats : queryUsageStats) {
                    mySortedMap.put(usageStats.getTotalTimeInForeground(),usageStats);
                }                    
                if(mySortedMap != null && !mySortedMap.isEmpty()) {
                    for (Map.Entry<Long, UsageStats> entry : mySortedMap.entrySet()) {
                        Log.v ("gaurav", "Key : " + entry.getKey());
                        Log.v ("gaurav", "Value : " + entry.getValue());
                        String topPackageName =  entry.getValue().getPackageName();
                        Log.v ("gaurav", "Package name: " + topPackageName);
                        Log.v ("gaurav", "Time spent in foreground: " + entry.getValue().getTotalTimeInForeground()/1000);
                       // Log.v ("gaurav", "First timestamp")
                        
                        SimpleDateFormat sdf11 = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
                        Date resultdate11 = new Date(entry.getValue().getFirstTimeStamp());
                        Log.v ("gaurav","First timestamp: " + sdf11.format(resultdate11));
                        
                        Date resultdate111 = new Date(entry.getValue().getLastTimeStamp());
                        Log.v ("gaurav","Last timestamp: " + sdf11.format(resultdate111));
                    }
                    
                }                                       
            }
        
          /*  String p = queryUsageStats.toString();
            Log.v ("gaurav", "String: " + p);*/
            // Log.v ("gaurav", "Query stats: " + usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0,  System.currentTimeMillis()));
            // TODO Show results in a listview instead of finishing activity.
            finish();
            break;
        default:
            break;
        }
    }

    public void insertIntoDB() {
        // Get SQL Helper.
        MySQLiteHelper helper = MySQLiteHelper.getInstance(mContext);

        SQLiteDatabase db = helper.getWritableDatabase();
        for (Map.Entry<String, Double> map : mMainService.foregroundActivityMap.entrySet()) {
            Log.v("gaurav", "Key : " + map.getKey() + "Value : " + map.getValue());
        }
        MySQLiteHelper.insert(mMainService.foregroundActivityMap, db);
        MySQLiteHelper.retrieve(db);
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
        
        if (mMainService != null) {
            for (Map.Entry<String, Double> entry : mMainService.foregroundActivityMap.entrySet()) {
                Log.v("gaurav", " App name : " + entry.getKey() + " Time used: " + entry.getValue()
                        / 1000000000);
                // i++;
            }
            mMainService.phoneUsedTime();
            Toast.makeText(mContext, "Phone used for: " + mMainService.phoneUsedTime() + "seconds",
                    Toast.LENGTH_LONG).show();
          //  Log.v("gaurav", "Phone screen on for: " + mMainService.phoneUsedTime() + "seconds");
        }
        
        //queryUsageStats = null;
        super.onDestroy();
        // Unregister receivers.
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

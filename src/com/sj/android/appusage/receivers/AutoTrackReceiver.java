package com.sj.android.appusage.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.sj.android.appusage.service.UsageTrackingService;
import com.sj.android.appusage.service.WakeLocker;

public class AutoTrackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        Log.v ("gaurav", "onReceive call");
        Log.v ("gaurav", "Intent start: " + intent.getBooleanExtra("startService", false));
        Log.v ("gaurav", "Intent stop: " + intent.getBooleanExtra("stopService", false));

        // Start service if not running at 2 AM.
        if (intent.getBooleanExtra("startService", false) == true) {
            if (!UsageSharedPrefernceHelper.isServiceRunning(context)) {
                Log.v("gaurav", "Start service");
                WakeLocker.acquire(context);
                Intent startServiceIntent = new Intent();
                startServiceIntent
                        .setClass(context, UsageTrackingService.class);
                startServiceIntent.setComponent(new ComponentName(context,
                        UsageTrackingService.class));
                context.startService(startServiceIntent);
                WakeLocker.release();
            }
        }

        if (intent.getBooleanExtra("stopService", false) == true) {
            if (UsageSharedPrefernceHelper.isServiceRunning(context)) {
                Log.v("gaurav", "Stop service");
                WakeLocker.acquire(context);
                Intent startServiceIntent = new Intent();
                startServiceIntent
                        .setClass(context, UsageTrackingService.class);
                startServiceIntent.setComponent(new ComponentName(context,
                        UsageTrackingService.class));
                context.stopService(startServiceIntent);
                WakeLocker.release();
            }
        }
    }
}

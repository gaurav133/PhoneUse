package com.asgj.android.appusage.receivers;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.service.UsageTrackingService;

public class DeviceRebootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action == null)
			return;
		if (action.equals(Intent.ACTION_SHUTDOWN)) {
			if (UsageSharedPrefernceHelper.isServiceRunning(context)) {
			    Intent stopServiceIntent = new Intent();
			    stopServiceIntent
                        .setClass(context, UsageTrackingService.class);
			    context.stopService(stopServiceIntent);
				UsageSharedPrefernceHelper.setServiceRunningWhileShutDown(
						context, true);
			}
		}
		//else case is for reboot
		else {
			if (UsageSharedPrefernceHelper.isNeedToServiceOnReboot(context)) {
			    
			    // Clear preference if necessary.
			    Calendar datePref = Calendar.getInstance();
			    datePref.setTimeInMillis(UsageSharedPrefernceHelper.getDateStoredInPref(context));
			    
				Intent startServiceIntent = new Intent();
				startServiceIntent
						.setClass(context, UsageTrackingService.class);
				startServiceIntent.setComponent(new ComponentName(context,
						UsageTrackingService.class));
				startServiceIntent.putExtra("isStartingAfterReboot", true);
				context.startService(startServiceIntent);
			}
		}

	}

}
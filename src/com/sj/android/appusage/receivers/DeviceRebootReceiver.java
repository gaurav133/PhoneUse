package com.sj.android.appusage.receivers;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.sj.android.appusage.Utility.Utils;
import com.sj.android.appusage.service.UsageTrackingService;

public class DeviceRebootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.v ("gaurav", "action: " + action);
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
		    Log.v ("gaurav", "Device reboot");
			if (UsageSharedPrefernceHelper.isNeedToServiceOnReboot(context)) {
			    
				Intent startServiceIntent = new Intent();
				startServiceIntent
						.setClass(context, UsageTrackingService.class);
				startServiceIntent.setComponent(new ComponentName(context,
						UsageTrackingService.class));
				startServiceIntent.putExtra("isStartingAfterReboot", true);
				context.startService(startServiceIntent);
			}
			AlarmManager startAlarmManager, stopAlarmManager;
            Intent startTimeIntent, stopTimeIntent;
            PendingIntent startPendingIntent, stopPendingIntent;
            
			Log.v ("gaurav", "is auto mode: " + UsageSharedPrefernceHelper.getTrackingMode(context));
			if (UsageSharedPrefernceHelper.getTrackingMode(context)) {
			Calendar startTrackCalendar = Calendar.getInstance();
	        Calendar endTrackCalendar = Calendar.getInstance();

	        long startSeconds, endSeconds;
	        
	        startAlarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
	        stopAlarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
	        
	        startSeconds = UsageSharedPrefernceHelper.getTrackingStartTime(context);
	        endSeconds = UsageSharedPrefernceHelper.getTrackingEndTime(context);
	        
	        Log.v ("gaurav", "startSeconds : " + startSeconds);
	        Log.v ("gaurav", "endSeconds : " + endSeconds);
            
	        int result = Utils.getStartAndEndTrackDays(startSeconds,  endSeconds);

	        Log.v ("gaurav", "result returned reboot : " + result);
            
	        switch (result) {
	        case 1 : // Both on present day. Do nothing.
	                break;
	        case 2 : // Start alarm today, end alarm tomorrow.
	            endTrackCalendar.add(Calendar.DATE, 1);
	            break;
	        case 3 : // Both on next day.
	                startTrackCalendar.add(Calendar.DATE, 1);
	                endTrackCalendar.add(Calendar.DATE, 1);
	                break;
	        }
	        
	        int startHr, endHr, startMin, endMin;
	        
	        startHr = (int) (startSeconds/3600);
	        startSeconds %= 3600;
	        
	        endHr = (int) (endSeconds/3600);
	        endSeconds %= 3600;
	        
	        startMin = (int) (startSeconds/60);
	        startSeconds %= 60;
	        
	        endMin = (int) (endSeconds/60);
	        endSeconds %= 60;
	        
	        startTrackCalendar.set(Calendar.HOUR_OF_DAY, startHr);
	        startTrackCalendar.set(Calendar.MINUTE, startMin);
	        startTrackCalendar.set(Calendar.SECOND, 0);
	        
	        endTrackCalendar.set(Calendar.HOUR_OF_DAY, endHr);
	        endTrackCalendar.set(Calendar.MINUTE, endMin);
	        endTrackCalendar.set(Calendar.SECOND, 0);

	        startTimeIntent = new Intent(context, AutoTrackReceiver.class);
	        startTimeIntent.putExtra("startService", true);
	        startPendingIntent = PendingIntent.getBroadcast(context, 1, startTimeIntent,
	                PendingIntent.FLAG_CANCEL_CURRENT);
	        startAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
	                startTrackCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
	                startPendingIntent);

	        stopTimeIntent = new Intent(context, AutoTrackReceiver.class);
	        stopTimeIntent.putExtra("stopService", true);
	        stopPendingIntent = PendingIntent.getBroadcast(context, 2, stopTimeIntent,
	                PendingIntent.FLAG_CANCEL_CURRENT);
	        stopAlarmManager
	                .setRepeating(AlarmManager.RTC_WAKEUP, endTrackCalendar.getTimeInMillis(),
	                        AlarmManager.INTERVAL_DAY, stopPendingIntent);

			}
		}

	}

}
package com.asgj.android.appusage.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UsageSharedPrefernceHelper {
	
	private static String PREFERNCE_NAME = "phone.usage";
	
	public static void insertTotalDurationInPref(Context context,String pkgName, long time){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong(pkgName, time);
		editor.commit();
	}

	public static long getTotalDurationInPref(Context context,String pkgName){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		return prefs.getLong(pkgName, 0);
	}
	public static void setServiceRunning(Context context,boolean isServiceRunning){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean("isServiceRunning", isServiceRunning);
		editor.commit();
	}

	public static boolean isServiceRunning(Context context){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean("isServiceRunning", false);
	}
	public static void setServiceRunningWhileShutDown(Context context,boolean isServiceRunning){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean("isServiceRunningWhileShutDown", isServiceRunning);
		editor.commit();
	}

	public static boolean isNeedToServiceOnReboot(Context context){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean("isServiceRunningWhileShutDown", false);
	}
}

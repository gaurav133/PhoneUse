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
}

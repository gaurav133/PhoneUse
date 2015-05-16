package com.asgj.android.appusage.Utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
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
	public static ArrayList<UsageInfo> getTotalInfoOfMusic(Context context){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		Set<String> mInfoList = prefs.getStringSet("music", null);
		ArrayList<UsageInfo> mInfo = new ArrayList<>();
		for (String s : mInfoList){
			mInfo.add(getMusicInfo(s));
		}
		return mInfo;
		
	}
	
	public static void setTotalIntervalsOfMusic(Context context,UsageInfo info){
		SharedPreferences prefs = context.getSharedPreferences(
			      PREFERNCE_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		Set<String> mInfoList = new HashSet<>();
		if(prefs.contains("music")){
			mInfoList.addAll(prefs.getStringSet("music", null));
		}
		mInfoList.add(getFormattedStringFromMusicInfo(info));
		editor.putStringSet("musicinfo", mInfoList);
		editor.commit();
	}
	
	private static String getFormattedStringFromMusicInfo(UsageInfo info){
		return info.getmIntervalStartTime() + "_" + info.getmIntervalEndTime() + "_"
				+ info.getmIntervalDuration();
	}
	
	private static UsageInfo getMusicInfo(String info){
		StringTokenizer tokenizer = new StringTokenizer(info, "_");
		UsageInfo infoIns = new UsageInfo();
		infoIns.setmIntervalStartTime(Long.parseLong((String)tokenizer.nextElement()));
		infoIns.setmIntervalEndTime(Long.parseLong((String)tokenizer.nextElement()));
		infoIns.setmIntervalDuration(Long.parseLong((String)tokenizer.nextElement()));
		return infoIns;
	}
}

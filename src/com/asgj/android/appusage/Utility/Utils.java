package com.asgj.android.appusage.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
public class Utils {
	
	public static String TIME_FORMAT_HHMMSS = "hh:mm:ss";
	public static long getTimeInSecFromNano(long nanoSec){
		return TimeUnit.SECONDS.convert(nanoSec, TimeUnit.NANOSECONDS);
	}
	
	public static String getTimeFromNanoSeconds(long nanoSec, String format)
			throws Exception {
		if (!format.equals(TIME_FORMAT_HHMMSS)) {
			throw new Exception("given time format not supported");
		}
		nanoSec = nanoSec / 1000;
		int hour = (int) nanoSec / 3600;
		nanoSec = nanoSec % 3600;
		int min = (int) nanoSec / 60;
		int sec = (int) nanoSec % 60;
		return hour + ":" + min + ":" + sec;

	}
	
	public static long getMiliSecFromDate(String date){
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		Date dateIns = null;
		try {
			dateIns = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateIns.getTime();
	}
	
	public static ArrayList<ApplicationInfo> getAllApplicationsInDevice(Context context){
		final PackageManager pm = context.getPackageManager();
		ArrayList<ApplicationInfo> packages = (ArrayList<ApplicationInfo>) pm.getInstalledApplications(PackageManager.GET_META_DATA);
		return packages;
	}

	public static String getDateFromMiliSeconds(long miliSec) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		Date resultdate = new Date(miliSec);
		return sdf.format(resultdate);
	}
}

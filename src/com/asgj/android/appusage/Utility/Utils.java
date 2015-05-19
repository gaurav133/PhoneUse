package com.asgj.android.appusage.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.CallLog;
import android.text.format.DateFormat;
import android.util.Log;

public class Utils {

    public static String TIME_FORMAT_HHMMSS = "hh:mm:ss";
    public static String TIME_FORMAT_HH_HR_MM_MIN_SS_SEC = "hh hr mm min ss sec";

    public static long getTimeInSecFromNano(long nanoSec) {
        return TimeUnit.SECONDS.convert(nanoSec, TimeUnit.NANOSECONDS);
    }
    
     
    public static boolean isPermissionGranted(Context context) {
        final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0,  System.currentTimeMillis());
        return !queryUsageStats.isEmpty();
    }

    
    public static HashMap<String,Long> getAppUsageFromLAndroidDb(Context context){
    	 UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
    	 List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,0,System.currentTimeMillis());
    	 HashMap<String,Long> map = new HashMap<String, Long>();
    	 for(UsageStats stat : usageStatsList){
    		 map.put(stat.getPackageName(), Utils.getTimeInSecFromNano(stat.getTotalTimeInForeground()));
    	 }
    	 return map;
    }
    public static String getTimeFromNanoSeconds(long nanoSec, String format) throws Exception {
        if (!format.equals(TIME_FORMAT_HHMMSS) || (!format.equals(TIME_FORMAT_HH_HR_MM_MIN_SS_SEC))) {
            throw new Exception("given time format not supported");
        }
        nanoSec = nanoSec / 1000;
        int hour = (int) nanoSec / 3600;
        nanoSec = nanoSec % 3600;
        int min = (int) nanoSec / 60;
		int sec = (int) nanoSec % 60;
		String time = "";
		if (format.equals(TIME_FORMAT_HHMMSS)) {
			if (hour > 0) {
				time = time + hour + ":";
			}
			if (min > 0) {
				time = time + min + ":";
			}
			return time + sec;
		} else {
			if (hour > 0) {
				time = time + hour + " hr ";
			}
			if (min > 0) {
				time = time + min + " min ";
			}
			return time + sec + " sec";
		}

    }

    public static long getMiliSecFromDate(String date) {
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
    
    /**
     * Calculate the time from given seconds
     * @param seconds Input seconds to be converted.
     * @return Time in given format.
     * @throws 
     */
    public static String getTimeFromSeconds(long seconds) {
        String time = "";
        int hour = (int) seconds / 3600;
        seconds = seconds % 3600;
        int min = (int) seconds / 60;
        int sec = (int) seconds % 60;
        
        if (seconds < 60) {
            time = sec + " sec ";
        } else if (seconds >= 60 && seconds < 3600) {
            time = min + " min " + sec + " sec ";
        } else if (seconds >= 3600) {
            time = hour + " hr " + min + " min " + sec + " sec ";
        }
        return time;
    }

    public static String getTimeFromTimeStamp(Context context, long timeStamp) {
        
        java.text.DateFormat dateFormat = SimpleDateFormat.getTimeInstance();
        return dateFormat.format(timeStamp);
    }

    public static ArrayList<ApplicationInfo> getAllApplicationsInDevice(Context context) {
        final PackageManager pm = context.getPackageManager();
        ArrayList<ApplicationInfo> packages = (ArrayList<ApplicationInfo>) pm
                .getInstalledApplications(PackageManager.GET_META_DATA);
        return packages;
    }

    public static String getDateFromMiliSeconds(long miliSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        Date resultdate = new Date(miliSec);
        return sdf.format(resultdate);
    }
    
    /**
     * Returns application icon corresponding to given package.
     * @param pkgName Package name for which icon is needed.
     * @param context Context to access application resources.
     * @return Application icon for pkgName, null in case pkgName is empty.
     */
    public static Bitmap getApplicationIcon(Context context, String pkgName) {

        Bitmap resizedBitmap = null;
        Drawable appIcon = null;
            try {
                appIcon = context.getPackageManager().getApplicationIcon(pkgName);
                Bitmap bmp = ((BitmapDrawable) appIcon).getBitmap();
                
                resizedBitmap = Bitmap.createScaledBitmap(bmp, 100, 100, false);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return resizedBitmap;
    }
    
	public static String getApplicationLabelName(Context context,
			String packageName) {
		ApplicationInfo mApplicationInfo = null;
		try {
			mApplicationInfo = context.getPackageManager().getApplicationInfo(
					packageName, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (String) context.getPackageManager().getApplicationLabel(
				mApplicationInfo);
	}
    /**
    * Get call logs for a particular duration.
    * @param startTime Starting time from which call logs are desired (Inclusive).
    * @param endTime End time upto which call logs are desired (Exclusive).
    * @return HashMap containing filtered call log entries for given time interval.
    */
    public static HashMap<String, Integer> getCallDetails(Context context, long startTime,
            long endTime, HashMap<String, Integer> mCallDetailsMap) {

        Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, CallLog.Calls.DATE + " DESC");

        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        while (managedCursor.moveToNext()) {

            String callDate = managedCursor.getString(date);

            // Only add if the call times overlap with tracking times.
            if ((Long.parseLong(callDate) <= startTime && (Long.parseLong(callDate) + duration) >= startTime)
                    || (Long.parseLong(callDate) > startTime && Long.parseLong(callDate) < endTime)) {

                // Add the details in hash-map.

                mCallDetailsMap.put(callDate, duration);
            }
        }
        return mCallDetailsMap;
    }
    public static HashMap<String,String> getDataFromSystemL(List<UsageStats> queryUsageStats){
		HashMap<String,String> map = new HashMap<String, String>();
		for(UsageStats stat : queryUsageStats){
			try {
				map.put(stat.getPackageName(), Utils.getTimeFromNanoSeconds(stat.getTotalTimeInForeground(),Utils.TIME_FORMAT_HH_HR_MM_MIN_SS_SEC));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}

}

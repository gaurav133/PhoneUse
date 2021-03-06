package com.sj.android.appusage.Utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sj.android.appusage.R;
import com.sj.android.appusage.activities.UsageListMainActivity;

public class Utils {

	public static String TIME_FORMAT_HHMMSS = "hh:mm:ss";
	private static final String LOG_TAG = Utils.class.getSimpleName();
	public static String TIME_FORMAT_HH_HR_MM_MIN_SS_SEC = "hh hr mm min ss sec";

	public static boolean isTabletDevice(Context context) {
		return context.getResources().getBoolean(R.bool.isTablet);
	}
	
	public static LinkedHashMap<Long, UsageInfo> sortIntervalMap(HashMap<Long,UsageInfo> infoMap,Comparator<Map.Entry<Long, UsageInfo>> compare) {

        // Sort intervals before sending to detail fragment.
        LinkedList<Map.Entry<Long, UsageInfo>> list = new LinkedList<>();
        for (Map.Entry<Long, UsageInfo> entry : infoMap.entrySet()) {
            list.add(entry);
        }
        Collections.sort(list, compare);
        
        LinkedHashMap<Long, UsageInfo> linkedMap = new LinkedHashMap<>();
        ListIterator<Map.Entry<Long, UsageInfo>> iterator = list.listIterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Long, UsageInfo> entry = iterator.next();
            linkedMap.put(entry.getKey(), entry.getValue());
        }
        return linkedMap;
    }
	
	public static LinkedHashMap<String, Long> sortYearMap(HashMap<String, Long> infoMap) {

        // Sort intervals before sending to detail fragment.
        LinkedList<Map.Entry<String, Long>> list = new LinkedList<>();
        for (Map.Entry<String, Long> entry : infoMap.entrySet()) {
            list.add(entry);
        }
        Collections.reverse(list);
        
        LinkedHashMap<String, Long> linkedMap = new LinkedHashMap<>();
        ListIterator<Map.Entry<String, Long>> iterator = list.listIterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            linkedMap.put(entry.getKey(), entry.getValue());
        }
        return linkedMap;
    }

    public static void sendNotification(Context context, String pkg, int notificationId) {
        int requestID = (int) System.currentTimeMillis();
        
        Intent notificationIntent = new Intent(context.getApplicationContext(), UsageListMainActivity.class);
        notificationIntent.putExtra("package", pkg);
        notificationIntent.setAction("com.android.sj.appusage.action.NOTIFIICATION");
        
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
        
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.usage_launcher_icon)
                .setContentTitle(context.getResources().getString(R.string.string_usage_alert))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentText(
                        context.getResources().getString(R.string.string_time_duration_exceeded) 
                                + " " + Utils.getApplicationLabelName(context, pkg) + ".");

        
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context
                .getSystemService(Service.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(notificationId, builder.build());
    }

    /**
     * Returns time in seconds from given nanoseconds.
     * @param nanoSec input time in nanoseconds.
     * @return time in seconds.
     */
    public static long getTimeInSecFromNano(long nanoSec) {
        return TimeUnit.SECONDS.convert(nanoSec, TimeUnit.NANOSECONDS);
    }

	public static long getTimeInSecFromMili(long miliSec) {
		return TimeUnit.SECONDS.convert(miliSec, TimeUnit.MILLISECONDS);
	}

	public static <T> int getIndexFromArray(T[] arr, T element) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == element) {
				return i;
			}
		}
		return -1;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static boolean isPermissionGranted(Context context) {
		final UsageStatsManager usageStatsManager = (UsageStatsManager) context
				.getSystemService("usagestats");
		final List<UsageStats> queryUsageStats = usageStatsManager
				.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0,
						System.currentTimeMillis());
		return !queryUsageStats.isEmpty();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static HashMap<String, Long> getAppUsageFromLAndroidDb(
			Context context, String showBy, long startTime, long endTime) {
		UsageStatsManager usm = (UsageStatsManager) context
				.getSystemService("usagestats");
		List<UsageStats> usageStatsList = null;
		int interval = 0;
		switch (showBy) {
		case "Today":
			interval = UsageStatsManager.INTERVAL_DAILY;
			break;
		case "Weekly":
			interval = UsageStatsManager.INTERVAL_WEEKLY;
			break;
		case "Monthly":
			interval = UsageStatsManager.INTERVAL_MONTHLY;
			break;
		case "Yearly":
			interval = UsageStatsManager.INTERVAL_YEARLY;
			break;
		case "Custom":
			interval = UsageStatsManager.INTERVAL_BEST;
			break;
		}
		if (interval != UsageStatsManager.INTERVAL_BEST) {
			usageStatsList = usm.queryUsageStats(interval, 0,
					System.currentTimeMillis());
		} else {
			usageStatsList = usm.queryUsageStats(
					UsageStatsManager.INTERVAL_BEST, startTime,
					endTime == 0 ? System.currentTimeMillis() : endTime);
		}
		HashMap<String, Long> map = new HashMap<String, Long>();
		for (UsageStats stat : usageStatsList) {
			if (Utils.getTimeInSecFromMili(stat.getTotalTimeInForeground()) > 0) {
				map.put(stat.getPackageName(), Utils.getTimeInSecFromMili(stat
						.getTotalTimeInForeground()));
			}
		}
		return map;
	}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static HashMap<String, UsageStats> getCurrentUsageMapForL(Context context) {

        UsageStatsManager statsManager = (UsageStatsManager) context.getSystemService("usagestats");
        ArrayList<UsageStats> appList = new ArrayList<>();
        appList = (ArrayList<UsageStats>) statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        HashMap<String, UsageStats> result = new HashMap<String, UsageStats>();

        ListIterator<UsageStats> iterator = appList.listIterator();

        while (iterator.hasNext()) {
            UsageStats stats = iterator.next();
            result.put(stats.getPackageName(), stats);
        }

        return result;
    }
    
    public static long getStartTimeFromDuration(long endTime, int duration) {
        Calendar result = Calendar.getInstance();
        
        result.setTimeInMillis(endTime);
        result.add(Calendar.SECOND, -duration);
        
        return result.getTimeInMillis();
    }

    /**
     * Returns time in required format from input nanoseconds for displaying.
     * @param nanoSec Input no. of nanoseconds
     * @param format Input format for displaying time
     * @throws IllegalArgumentException in case input format doesn't match the stored format
     * @return Time as per required format
     */
    public static String getTimeFromNanoSeconds(long nanoSec, String format) throws IllegalArgumentException {
        if (!format.equals(TIME_FORMAT_HHMMSS) || (!format.equals(TIME_FORMAT_HH_HR_MM_MIN_SS_SEC))) {
            throw new IllegalArgumentException("given time format not supported");
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
            if (sec > 0) {
                time = time + sec + " sec";
            }
            return time;
        } else {
            if (hour > 0) {
                time = time + hour + " hr ";
            }
            if (min > 0) {
                time = time + min + " min ";
            }
            if (sec > 0) {
                time = time + sec + " sec";
            }
            return time;
        }
    }

    /**
     * Method that returns the days for setting Starting and ending alarm in Auto-track mode.
     * 
     * @param startSeconds Starting time set by user for auto track.
     * @param endSeconds End time set by user for auto track.
     * @return 1 if both should be fired on present day, 2 if start alarm should fire on present
     * day, and end alarm on next day, 3 if both should fire on next day.
     */

    public static int getStartAndEndTrackDays(long startSeconds, long endSeconds) {
        int result = 1;
        long startHour, startMin;
        long endHour, endMin;

        startHour = startSeconds / 3600;
        endHour = endSeconds / 3600;

        startSeconds %= 3600;
        endSeconds %= 3600;

        startMin = startSeconds / 60;
        endMin = endSeconds / 60;

        startSeconds %= 60;
        endSeconds %= 60;

        /**
         * 3 cases : (startSeconds > endSeconds, startSeconds < endSeconds, startSeconds =
         * endSeconds)
         * Result for case 3 is same as case 1.
         */
        // Case 1.
        if ((startHour > endHour) || (startHour == endHour && startMin >= endMin)) {
            /**
             * Example scenarios : 1. S: 10 AM, E: 5 AM 
             *                     2. S: 10 PM, E: 5 AM 
             *                     3. S: 10 PM, E: 3 PM
             */

            result = 2;
        }

        // Case 2.
        if (startHour < endHour || (startHour == endHour && startMin < endMin)) {
            /**
             * Example scenarios : 1. S: 5 AM, E: 10 AM 
             *                     2. S: 1 PM, E: 10 PM 
             *                     3. S: 11 AM, E: 9 PM
             */

            Calendar presentCalendar = Calendar.getInstance();
            int presentHr = presentCalendar.get(Calendar.HOUR_OF_DAY);
            int presentMin = presentCalendar.get(Calendar.MINUTE);

            if ((presentHr > startHour && presentHr > endHour)
                    || (presentHr == endHour && presentMin >= endMin)) {
                // Fire both on next day.
                result = 3;
            } else if ((presentHr > startHour && presentHr < endHour)
                    || ((presentHr == startHour && presentMin >= startMin) && (presentHr < endHour || (presentHr == endHour && presentMin < endMin))) 
                    || ((presentHr < startHour) || (presentHr == startHour && presentMin <= startMin))){
                result = 1;
            }
        }
       Log.v ("gaurav", "Result code: " + result);
        return result;
    }

    /**
     * Check whether device API is Android L or less.
     * @param context Context to access application resources.
     * @return boolean value indicating if it's Android L device. True in case it is, false otherwise.
     */
    public static boolean isAndroidLDevice (Context context) {
        return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) ? true : false;
    }
    public static int getHourFromTime(long timeStamp) {

        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
        String time;
        
        time = format.format(new Date(timeStamp));
        int index = time.indexOf(":");
        String hr = time.substring(0, index);
        
        return Integer.parseInt(hr);
    }

    public static long getMiliSecFromDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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

		if (hour > 0) {
			time += hour + " hr";
		}
		if (min > 0) {
			time += " " + min + " min";
		}
		if (sec > 0) {
			time += " " + sec + " sec";
		}
		return time;
	}

	public static String getTimeForRangeFromSeconds(long seconds,
			boolean is12HourFormat) {
		String time = "";
		int hour = (int) seconds / 3600;
		seconds = seconds % 3600;
		int min = (int) seconds / 60;
		int sec = (int) seconds % 60;

		if (hour >= 12) {
			if (hour - 12 > 9) {
				time += (hour - 12) + " :";
			} else {
				time += "0" + (hour - 12) + " :";
			}
		} else {
			if (hour > 9) {
				time += (hour) + " :";
			} else {
				time += "0" + (hour) + " :";
			}
		}
		if (min > 9) {
			time += " " + min + "";
		} else {
			time += " 0" + min + "";
		}
		if (sec > 0) {
			
		}

		if (hour >= 12) {
			hour = hour - 12;
			time = time + " PM";
		} else {
			time = time + " AM";
		}
		return time;
	}

	public static String getTimeFromTimeStamp(Context context, long timeStamp) {

		java.text.DateFormat dateFormat = SimpleDateFormat.getTimeInstance();
		return dateFormat.format(timeStamp);
	}

	public static ArrayList<ApplicationInfo> getAllApplicationsInDevice(
			Context context) {
		final PackageManager pm = context.getPackageManager();
		ArrayList<ApplicationInfo> packages = (ArrayList<ApplicationInfo>) pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		return packages;
	}
	
	public static String getDateForDisplay(long miliSec) {
        Date date = new Date(miliSec);
        return DateFormat.getDateInstance().format(date);
	}
	
	public static boolean isDateToday(String arg0){
		Calendar c = Calendar.getInstance();
		String arg1 = DateFormat.getDateInstance().format(c.getTime());
		if (arg1.equals(arg0)) {
			return true;
		}
		return false;
	}
	
	public static boolean isCurrentMonth(String arg0){
	    Log.v ("gaurav", " " + Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
	    return (Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + Calendar.getInstance().get(Calendar.YEAR)).equals(arg0);
	}
	
	public static String getDateFromMiliSeconds(long miliSec) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date resultdate = new Date(miliSec);
		return sdf.format(resultdate);
	}
	
	
    public static String getMonth(long miliSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM DD,yyyy");
        Date resultdate = new Date(miliSec);
        return sdf.format(resultdate);
    }

	/**
	 * Returns application icon corresponding to given package.
	 * 
	 * @param pkgName
	 *            Package name for which icon is needed.
	 * @param context
	 *            Context to access application resources.
	 * @return Application icon for pkgName, null in case pkgName is empty.
	 */
	public static void getScaledImageView(Context context, ImageView image) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image
				.getLayoutParams();
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		Configuration conf = context.getResources().getConfiguration();
		int screenLayout = conf.orientation == Configuration.ORIENTATION_LANDSCAPE ? conf.screenHeightDp
				: conf.screenWidthDp;
		int density = displayMetrics.densityDpi;
		int p = (int) ((screenLayout * density) / 2000);
		params.width = p;
		params.height = p;
		image.setLayoutParams(params);

	}

	public static Bitmap getApplicationIcon(Context context, String pkgName) {
		Bitmap resizedBitmap = null;
		Drawable appIcon = null;
		try {
			appIcon = context.getPackageManager().getApplicationIcon(pkgName);
			Bitmap bmp = null;
			if (appIcon instanceof BitmapDrawable)
				bmp = ((BitmapDrawable) appIcon).getBitmap();
			Configuration conf = context.getResources().getConfiguration();
			int screenLayout = conf.orientation == Configuration.ORIENTATION_LANDSCAPE ? conf.screenHeightDp
					: conf.screenWidthDp;
			DisplayMetrics displayMetrics = context.getResources()
					.getDisplayMetrics();
			int density = displayMetrics.densityDpi;
			int p = (int) ((screenLayout * density) / 2000);
			// Scale according to screen size.
			if (bmp != null && p > 0)
				resizedBitmap = Bitmap.createScaledBitmap(bmp, p, p, true);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resizedBitmap;
	}

	public static long calculateMapSum(HashMap<String, Long> inputMap) {

		long sum = 0;

		if (!inputMap.isEmpty()) {
			for (Map.Entry<String, Long> entry : inputMap.entrySet()) {
				sum += entry.getValue();
			}
		}
		return sum;
	}

	public static long calculateListSum(ArrayList<UsageInfo> inputList) {

		long sum = 0;
		if (!inputList.isEmpty()) {
			ListIterator<UsageInfo> listIterator = inputList.listIterator();

			while (listIterator.hasNext()) {
				sum += listIterator.next().getmIntervalDuration();
			}
		}
		return sum;
	}

	/**
	 * Method to compare 2 dates in Java based on input calendar objects.
	 * 
	 * @param cal1
	 *            Calendar object 1.
	 * @param cal2
	 *            Calendar object 2.
	 * @return 1 if cal1 > cal2, 0 if equal, -1 otherwise.
	 */
	public static int compareDates(Calendar cal1, Calendar cal2) {

		int comp1, comp2;

		comp1 = cal1.get(Calendar.YEAR);
		comp2 = cal2.get(Calendar.YEAR);

		if (comp1 != comp2) {
			return (comp1 > comp2) ? 1 : -1;
		}

		comp1 = cal1.get(Calendar.MONTH);
		comp2 = cal2.get(Calendar.MONTH);

		if (comp1 != comp2) {
			return (comp1 > comp2) ? 1 : -1;
		}

		comp1 = cal1.get(Calendar.DAY_OF_MONTH);
		comp2 = cal2.get(Calendar.DAY_OF_MONTH);

		if (comp1 != comp2) {
			return (comp1 > comp2) ? 1 : -1;
		}

		return 0;
	}

	public static int getIndexFromArray(String[] arr, String element) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(element)) {
				return i;
			}
		}
		return -1;
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
			return packageName;
		}
		return (String) context.getPackageManager().getApplicationLabel(
				mApplicationInfo);
	}

	public static HashMap<String, String> getDataFromSystemL(
			List<UsageStats> queryUsageStats) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (UsageStats stat : queryUsageStats) {
			try {
				map.put(stat.getPackageName(), Utils.getTimeFromNanoSeconds(
						stat.getTotalTimeInForeground(),
						Utils.TIME_FORMAT_HH_HR_MM_MIN_SS_SEC));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * Method to check whether sufficient RAM is available to continue/start
	 * service. In case RAM is less than 2%, tracking will be stopped.
	 * 
	 * @param context
	 *            Context to access application resources.
	 * @return boolean flag indicating whether device has sufficient battery
	 *         available.
	 */
	public static boolean isSufficientRAMAvailable(Context context, boolean isShow) {
		MemoryInfo info = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(info);
		long percentAvail = info.availMem * 100 / info.totalMem;
		Log.v(LOG_TAG, "Availabel precentage: " + percentAvail);
		if (percentAvail < 2) {
			// TODO Show some dialog.
		    if (isShow) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(context)
    					.setTitle(R.string.string_title_low_memory_dialog)
    					.setMessage(R.string.string_desc_low_memory_dialog)
    					.setPositiveButton(android.R.string.ok,
    							new DialogInterface.OnClickListener() {
    								@Override
    								public void onClick(DialogInterface dialog,
    										int which) {
    									dialog.dismiss();
    								}
    							});
    			AlertDialog dialog = builder.create();
    			dialog.show();
		    }
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Method to check whether sufficient battery is available to continue/start
	 * service. In case battery is less than 5% and device isn't charging,
	 * tracking will be stopped.
	 * 
	 * @param context
	 *            Context to access application resources.
	 * @return boolean flag indicating whether device has sufficient battery
	 *         available.
	 */
	public static boolean isSufficientBatteryAvailable(Context context, boolean isShow) {
		boolean result = true;

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);

		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float) scale;

		if (batteryPct * 100 <= 5.0f && !isCharging) {
			Log.v(LOG_TAG, "Battery percentage low: " + batteryPct);
			result = false;
			if (isShow) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(context)
    					.setTitle(R.string.string_title_low_battery_dialog)
    					.setMessage(R.string.string_desc_low_battery_dialog)
    					.setPositiveButton(android.R.string.ok,
    							new DialogInterface.OnClickListener() {
    								@Override
    								public void onClick(DialogInterface dialog,
    										int which) {
    									dialog.dismiss();
    								}
    							});
    			AlertDialog dialog = builder.create();
    			dialog.show();
			}
		}
		Log.v(LOG_TAG, "Battery percentage: " + batteryPct);
		return result;
	}
}

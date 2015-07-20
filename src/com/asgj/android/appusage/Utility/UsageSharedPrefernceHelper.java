package com.asgj.android.appusage.Utility;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.asgj.android.appusage.R;

public class UsageSharedPrefernceHelper {
    private static String PREFERNCE_NAME = "phone.usage";
    private static final String LOG_TAG = UsageSharedPrefernceHelper.class
            .getSimpleName();
    private static String PREF_NAME_AUTO_TRACKING_INFO = "phone.usage.app.auto.tracking.info";
    private static String PREF_NAME_ALERT_DURATION_INFO = "phone.usage.app.alert.duration.info";
    private static String PREF_NAME_ALERT_NOTIFIED_INFO = "phone.usage.app.alert.notified.info";

	public static void insertTotalDurationAppInPref(Context context,
			String pkgName, long time, String prefName) {
		SharedPreferences prefs = context.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong(pkgName, time);
		editor.commit();
	}

    public static long getTotalDurationAppInPref(Context context, String pkgName) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME_ALERT_DURATION_INFO, Context.MODE_PRIVATE);
        return prefs.getLong(pkgName, 0);
    }

	public static void setServiceRunning(Context context,
			boolean isServiceRunning) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean("isServiceRunning", isServiceRunning);
		editor.commit();
	}

	public static void setShowByUsage(Context context, String isServiceRunning) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("showBy", isServiceRunning);
		editor.commit();
	}

	public static String getShowByType(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		return prefs.getString("showBy",
				context.getString(R.string.string_Today));
	}

	public static boolean isServiceRunning(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		return prefs.getBoolean("isServiceRunning", false);
	}

	public static void setServiceRunningWhileShutDown(Context context,
			boolean isServiceRunning) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean("isServiceRunningWhileShutDown", isServiceRunning);
		editor.commit();
	}

	public static void setCurrentDate(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong("date", System.currentTimeMillis());
		editor.commit();
	}

	public static void setCalendar(Context context, long time, String key) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong(key, time);
		editor.commit();
	}

	public static long getCalendar(Context context, String key) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		return prefs.getLong(key, 0);
	}

	public static long getDateStoredInPref(Context context) {

		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		long date = prefs.getLong("date", System.currentTimeMillis());
		return date;

	}

    public static boolean isNeedToServiceOnReboot(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
                Context.MODE_PRIVATE);
        return prefs.getBoolean("isServiceRunningWhileShutDown", false);
    }



	public static Set<String> getSelectedApplicationForTracking(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		return prefs.getStringSet("applicationtracking", null);

	}
	
    public static void setApplicationsForTracking(Context context, HashMap<String, Long> alertMap) {

        HashMap<String, Long> previousMapDuration = getApplicationsDurationForTracking(context);
        HashMap<String, Boolean> previousMapAlert = getApplicationsAlertForTracking(context);
		// First remove old entries from previous map which don't require to be notified now.
        if (previousMapDuration != null && alertMap != null && previousMapAlert != null) {
            previousMapDuration.keySet().retainAll(alertMap.keySet());
            previousMapAlert.keySet().retainAll(alertMap.keySet());

            // Now add new entries to previous map and update old entries if duration is changed.
            for (Map.Entry<String, Long> entry : alertMap.entrySet()) {

                String pkg = entry.getKey();
                Long duration = entry.getValue();
                Boolean isNotified = true;

                // In case previous map has an entry with different duration, update duration.
                if (previousMapDuration.containsKey(pkg)) {
                    if (previousMapDuration.get(pkg) != duration) {
                        isNotified = false;
                        previousMapDuration.put(pkg, duration);
                        previousMapAlert.put(pkg, isNotified);
                    }
                } else {
                    isNotified = false;
                    previousMapDuration.put(pkg, duration);
                    previousMapAlert.put(pkg, isNotified);
                }

                // Save the updated package alert to preferences.
                insertTotalDurationAppInPref(context, pkg, duration, PREF_NAME_ALERT_DURATION_INFO);
                setApplicationAlert(context, pkg, isNotified);
            }
            
            if (previousMapDuration.isEmpty()) {
                clearPreference(context, PREF_NAME_ALERT_DURATION_INFO);
                clearPreference(context, PREF_NAME_ALERT_NOTIFIED_INFO);
                return;
            }
        }

    }
    
    public static void clearPreference (Context context, String prefName) {
        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Long> getApplicationsDurationForTracking(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_ALERT_DURATION_INFO,
                Context.MODE_PRIVATE);
        return (HashMap<String, Long>) prefs.getAll();
    }

    public static void setApplicationAlert(Context context, String pkg, boolean isNotified) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_ALERT_NOTIFIED_INFO,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean(pkg, isNotified);
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Boolean> getApplicationsAlertForTracking(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_ALERT_NOTIFIED_INFO,
                Context.MODE_PRIVATE);
        return (HashMap<String, Boolean>) prefs.getAll();
    }

	public static Set<String> getSelectedApplicationForFiltering(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		return prefs.getStringSet("appFilter", null);

	}

	public static void setApplicationForFiltering(Context context, String info,
			boolean isAdded) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		Set<String> mInfoList = new HashSet<>();
		if (prefs.contains("appFilter")) {
			mInfoList.addAll(prefs.getStringSet("appFilter", null));
		}
		if (!mInfoList.contains(info) && isAdded) {
			mInfoList.add(info);
		}
		if (mInfoList.contains(info) && !isAdded) {
			mInfoList.remove(info);
		}
		editor.putStringSet("appFilter", mInfoList);
		editor.commit();

	}
	
	public static void setFilterMode(Context context, boolean isFilterMode) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean("filterMode", isFilterMode);
		editor.commit();
	}

	public static boolean isFilterMode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		return prefs.getBoolean("filterMode", false);
	}

	public static void setTrackingMode(Context context, boolean isCustomMode) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_AUTO_TRACKING_INFO,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean("trackingMode", isCustomMode);
		editor.apply();
	}

	public static boolean getTrackingMode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_AUTO_TRACKING_INFO,
				Context.MODE_PRIVATE);
		return prefs.getBoolean("trackingMode", false);
	}

    public static void setTrackingStartTime(Context context, Long startTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_AUTO_TRACKING_INFO,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong("startTrackingTime", startTime);
        editor.apply();
    }

    public static Long getTrackingStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_AUTO_TRACKING_INFO,
                Context.MODE_PRIVATE);
        return prefs.getLong("startTrackingTime", 0);
    }

    public static void setTrackingEndTime(Context context, Long endTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_AUTO_TRACKING_INFO,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong("endTrackingTime", endTime);
        editor.apply();
    }

    public static Long getTrackingEndTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME_AUTO_TRACKING_INFO,
                Context.MODE_PRIVATE);
        return prefs.getLong("endTrackingTime", 0);
    }

    /**
     * Return a calendar object based on the showType stored in preferences.
     * 
     * @param context Context to access application resources.
     * @return calendar Calendar object with given date.
     */
    public static Calendar getCalendarByShowType(Context context) {

		SharedPreferences prefs = context.getSharedPreferences(PREFERNCE_NAME,
				Context.MODE_PRIVATE);
		String showBy = prefs.getString("showBy", context.getResources()
				.getString(R.string.string_Today));

		Calendar calendar = Calendar.getInstance();

		switch (showBy) {
		case "Today":
			break;
		case "Weekly":
			calendar.add(Calendar.DATE, -6);
			break;
		case "Monthly":
			calendar.add(Calendar.DATE, -29);
			break;
		case "Yearly":
			calendar.add(Calendar.DATE, -364);
			break;
		default:
			break;
		}

		return calendar;
	}
}

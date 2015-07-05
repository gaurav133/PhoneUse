package com.asgj.android.appusage.ui.widgets;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.receivers.AutoTrackReceiver;

public class TimePreference extends DialogPreference implements Preference.OnPreferenceClickListener{
    private int lastHour = 0;
    private int lastMinute = 0;
    protected boolean is24HourFormat;
    private TimePicker picker = null;
    protected TextView timeDisplay;
    boolean mIsClicked = false;

    private Intent mStartTimeIntent, mStopTimeIntent;
    private PendingIntent mStartPendingIntent, mStopPendingIntent;
    private AlarmManager mStartAlarmManager, mStopAlarmManager;

    public static int getHour(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[1]));
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // TODO Auto-generated method stub
        Log.v ("gaurav", "Created");
        return super.onCreateView(parent);
        
    }
    
    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        // TODO Auto-generated method stub
        super.onAttachedToHierarchy(preferenceManager);
        mIsClicked = false;
        Log.v ("gaurav", "onAtachedToHierarchy");
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        is24HourFormat = DateFormat.is24HourFormat(ctxt);
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
        Log.v ("gaurav","Open time preference");
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());

        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setIs24HourView(is24HourFormat);
        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            /*
             * Preference preference = findPreferenceInHierarchy(key)
             * SharedPreferences prefs = getContext().
             */
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);

            if (callChangeListener(time)) {

                // Send broadcast for change in start/stop time.
                if (this.getKey().equals("start_time_pref_key")) {
                    setStartingAlarm(lastHour, lastMinute);
                }

                if (this.getKey().equals("stop_time_pref_key")) {
                    setStoppingAlarm(lastHour, lastMinute);
                }
                persistString(time);
                timeDisplay.setText(toString());
                
                
            }
        }
    }

    
    private void setStartingAlarm(int hour, int min) {
        mStartTimeIntent = new Intent(getContext(), AutoTrackReceiver.class);
        mStartTimeIntent.putExtra("startService", true);
        mStartPendingIntent = PendingIntent.getBroadcast(getContext(), 0, mStartTimeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mStartAlarmManager = (AlarmManager) getContext().getSystemService(Service.ALARM_SERVICE);
        Calendar startTrackCalendar = Calendar.getInstance();
        startTrackCalendar.set(Calendar.HOUR_OF_DAY, lastHour);
        startTrackCalendar.set(Calendar.MINUTE, lastMinute);
        startTrackCalendar.set(Calendar.SECOND, 0);

        mStartAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                startTrackCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                mStartPendingIntent);

        // Store this in shared preference.
        int seconds = hour * 3600 + min * 60;
        UsageSharedPrefernceHelper.setTrackingStartTime(getContext(), seconds);
    }

    private void setStoppingAlarm(int hour, int min) {
        mStopTimeIntent = new Intent(getContext(), AutoTrackReceiver.class);
        mStopTimeIntent.putExtra("stopService", true);
        mStopPendingIntent = PendingIntent.getBroadcast(getContext(), 1, mStopTimeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mStopAlarmManager = (AlarmManager) getContext().getSystemService(Service.ALARM_SERVICE);
        Calendar startTrackCalendar = Calendar.getInstance();
        startTrackCalendar.set(Calendar.HOUR_OF_DAY, lastHour);
        startTrackCalendar.set(Calendar.MINUTE, lastMinute);
        startTrackCalendar.set(Calendar.SECOND, 0);

        mStopAlarmManager
                .setRepeating(AlarmManager.RTC_WAKEUP, startTrackCalendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, mStopPendingIntent);

        // Store this in shared preference.
        int seconds = hour * 3600 + min * 60;
        UsageSharedPrefernceHelper.setTrackingEndTime(getContext(), seconds);
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        Log.v ("gaurav", "onBindView call");
        View widgetLayout;
        int childCounter = 0;
        do {
            widgetLayout = ((ViewGroup) view).getChildAt(childCounter);
            childCounter++;
        } while (widgetLayout.getId() != android.R.id.widget_frame);
        ((ViewGroup) widgetLayout).removeAllViews();
        timeDisplay = new TextView(widgetLayout.getContext());
        timeDisplay.setText(toString());
        ((ViewGroup) view).addView(timeDisplay);
        Preference pref = findPreferenceInHierarchy("preferences_enabled");
        pref.setOnPreferenceClickListener(this);
        if (getSharedPreferences().getBoolean("preferences_enabled", false) == false) {
            timeDisplay.setEnabled(false);
            if (mIsClicked) {
                Log.v("gaurav", "Flag not null, stop alarm");
                if (mStartPendingIntent != null) {
                    mStartPendingIntent.cancel();
                }

                if (mStopPendingIntent != null) {
                    mStopPendingIntent.cancel();
                }

                 UsageSharedPrefernceHelper.setTrackingMode(getContext(), false);
            }
        } else {
            timeDisplay.setEnabled(true);

            if (mIsClicked) {
                Log.v("gaurav", "Flag not null, start alarm");
                // Get time from preferences, and set alarms.
                int startSeconds = UsageSharedPrefernceHelper.getTrackingStartTime(getContext());
                int startHour = startSeconds / 3600;
                startSeconds %= 3600;
                int startMinutes = startSeconds / 60;

                setStartingAlarm(startHour, startMinutes);

                int stopSeconds = UsageSharedPrefernceHelper.getTrackingEndTime(getContext());
                int stopHour = stopSeconds / 3600;
                stopSeconds %= 3600;
                int stopMinutes = stopSeconds / 60;

                setStoppingAlarm(stopHour, stopMinutes);
                UsageSharedPrefernceHelper.setTrackingMode(getContext(), true);
                
            }
        }
        mIsClicked = false;
        
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    public String toString() {
        if (is24HourFormat) {
            return ((lastHour < 10) ? "0" : "") + Integer.toString(lastHour) + ":"
                    + ((lastMinute < 10) ? "0" : "") + Integer.toString(lastMinute);
        } else {
            int myHour = lastHour % 12;
            return ((myHour == 0) ? "12" : ((myHour < 10) ? "0" : "") + Integer.toString(myHour))
                    + ":" + ((lastMinute < 10) ? "0" : "") + Integer.toString(lastMinute)
                    + ((lastHour >= 12) ? " PM" : " AM");
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            if (defaultValue == null) {
                time = "00:00";
            } else {
                time = defaultValue.toString();
            }
            if (shouldPersist()) {
                persistString(time);
            }
        }

        String[] timeParts = time.split(":");
        lastHour = Integer.parseInt(timeParts[0]);
        lastMinute = Integer.parseInt(timeParts[1]);
        ;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // TODO Auto-generated method stub
        Log.v ("gaurav", "Clicked");
        mIsClicked = true;
        return false;
    }

    /*
     * if (restoreValue) { if (defaultValue==null) {
     * time=getPersistedString("00:00"); } else {
     * time=getPersistedString(defaultValue.toString()); } } else {
     * time=defaultValue.toString(); }
     * 
     * lastHour=getHour(time); lastMinute=getMinute(time);
     */

}
// }
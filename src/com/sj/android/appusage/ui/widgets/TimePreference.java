package com.sj.android.appusage.ui.widgets;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.sj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.sj.android.appusage.Utility.Utils;
import com.sj.android.appusage.receivers.AutoTrackReceiver;

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

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        is24HourFormat = DateFormat.is24HourFormat(ctxt);
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
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

            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);

            if (callChangeListener(time)) {

                // Send broadcast for change in start/stop time.
                if (this.getKey().equals("start_time_pref_key")) {
                    Log.v("gaurav", "Last hour start: " + lastHour);
                    Log.v("gaurav", "Last min start: " + lastMinute);
                    setTrackingAlarms(lastHour * 3600 + lastMinute * 60, UsageSharedPrefernceHelper.getTrackingEndTime(getContext()));
                }

                if (this.getKey().equals("stop_time_pref_key")) {
                    Log.v("gaurav", "end hour start: " + lastHour);
                    Log.v("gaurav", "end min start: " + lastMinute);
                    setTrackingAlarms(UsageSharedPrefernceHelper.getTrackingStartTime(getContext()), lastHour * 3600 + lastMinute * 60);
                }
                // persistString(time);
                timeDisplay.setText(toString());
                
                
            }
        }
    }

        
    private void setTrackingAlarms (long startSeconds, long endSeconds) {
        Calendar startTrackCalendar = Calendar.getInstance();
        Calendar endTrackCalendar = Calendar.getInstance();


        mStartAlarmManager = (AlarmManager) getContext().getSystemService(Service.ALARM_SERVICE);
        mStopAlarmManager = (AlarmManager) getContext().getSystemService(Service.ALARM_SERVICE);
        
        int result = Utils.getStartAndEndTrackDays(startSeconds,  endSeconds);
        
        UsageSharedPrefernceHelper.setTrackingEndTime(getContext(), endSeconds);
        UsageSharedPrefernceHelper.setTrackingStartTime(getContext(), startSeconds);

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

        mStartTimeIntent = new Intent(getContext(), AutoTrackReceiver.class);
        mStartTimeIntent.putExtra("startService", true);
        mStartPendingIntent = PendingIntent.getBroadcast(getContext(), 1, mStartTimeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mStartAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                startTrackCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                mStartPendingIntent);


        mStopTimeIntent = new Intent(getContext(), AutoTrackReceiver.class);
        mStopTimeIntent.putExtra("stopService", true);
        mStopPendingIntent = PendingIntent.getBroadcast(getContext(), 2, mStopTimeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mStopAlarmManager
                .setRepeating(AlarmManager.RTC_WAKEUP, endTrackCalendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, mStopPendingIntent);

        // Store this in shared preference.
        UsageSharedPrefernceHelper.setTrackingMode(getContext(), true);
        
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        View widgetLayout;
        int childCounter = 0;
        do {
            widgetLayout = ((ViewGroup) view).getChildAt(childCounter);
            childCounter++;
        } while (widgetLayout.getId() != android.R.id.widget_frame);
        ((ViewGroup) widgetLayout).removeAllViews();
        timeDisplay = new TextView(widgetLayout.getContext());

        if (this.getKey().equals("start_time_pref_key")) {
            long seconds = UsageSharedPrefernceHelper.getTrackingStartTime(getContext());
            lastHour = (int) (seconds / 3600);
            seconds %= 3600;
            lastMinute = (int) (seconds / 60);
            seconds %= 60;
        }

        if (this.getKey().equals("stop_time_pref_key")) {
            long seconds = UsageSharedPrefernceHelper.getTrackingEndTime(getContext());
            lastHour = (int) (seconds / 3600);
            seconds %= 3600;
            lastMinute = (int) (seconds / 60);
            seconds %= 60;
        }
        timeDisplay.setText(toString());
        ((ViewGroup) view).addView(timeDisplay);
        Preference pref = findPreferenceInHierarchy("preferences_enabled");
        pref.setOnPreferenceClickListener(this);
        if (getSharedPreferences().getBoolean("preferences_enabled", false) == false) {
            timeDisplay.setEnabled(false);
            if (mIsClicked) {
                Log.v("gaurav", "Flag not null, stop alarm");

                mStartTimeIntent = new Intent(getContext(), AutoTrackReceiver.class);
                mStartTimeIntent.putExtra("startService", true);
                mStartPendingIntent = PendingIntent.getBroadcast(getContext(), 1, mStartTimeIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                mStartPendingIntent.cancel();
                if (mStartAlarmManager == null) {
                    mStartAlarmManager = (AlarmManager) getContext().getSystemService(
                            Service.ALARM_SERVICE);
                }
                mStartAlarmManager.cancel(mStartPendingIntent);

                mStopTimeIntent = new Intent(getContext(), AutoTrackReceiver.class);
                mStopTimeIntent.putExtra("stopService", true);
                mStopPendingIntent = PendingIntent.getBroadcast(getContext(), 2, mStopTimeIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                mStopPendingIntent.cancel();
                if (mStopAlarmManager == null) {
                    mStopAlarmManager = (AlarmManager) getContext().getSystemService(
                            Service.ALARM_SERVICE);
                }
                mStopAlarmManager.cancel(mStopPendingIntent);

                UsageSharedPrefernceHelper.setTrackingMode(getContext(), false);
            }
        } else {
            timeDisplay.setEnabled(true);

            if (mIsClicked) {
                Log.v("gaurav", "Flag not null, start alarm");
                // Get time from preferences, and set alarms.
                long startSeconds = UsageSharedPrefernceHelper.getTrackingStartTime(getContext());
                long stopSeconds = UsageSharedPrefernceHelper.getTrackingEndTime(getContext());

                setTrackingAlarms(startSeconds, stopSeconds);
            }
        }
        mIsClicked = false;

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

            if (this.getKey().equals("start_time_pref_key")) {
                if (defaultValue == null) {
                    time = "00:00";
                } else {
                    long seconds = UsageSharedPrefernceHelper.getTrackingStartTime(getContext());
                    lastHour = (int) (seconds / 3600);
                    seconds %= 3600;
                    lastMinute = (int) (seconds / 60);
                    seconds %= 60;
                    time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);
                }
            }

            if (this.getKey().equals("stop_time_pref_key")) {
                if (defaultValue == null) {
                    time = "00:00";
                } else {
                    long seconds = UsageSharedPrefernceHelper.getTrackingEndTime(getContext());
                    lastHour = (int) (seconds / 3600);
                    seconds %= 3600;
                    lastMinute = (int) (seconds / 60);
                    seconds %= 60;
                    time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);
                }
            }

        } else {
            if (defaultValue == null) {
                time = "00:00";
            } else {
                time = defaultValue.toString();
            }
            // if (shouldPersist()) {
            // persistString(time);
            // }
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
}

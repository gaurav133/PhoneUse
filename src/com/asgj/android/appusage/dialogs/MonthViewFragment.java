package com.asgj.android.appusage.dialogs;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;

public class MonthViewFragment extends DialogFragment implements CalendarView.OnDateChangeListener {

    public interface DateInterface {
        abstract void onDateSetComplete(Calendar cal1, Calendar cal2);
    }

    private Context mContext;
    DateInterface mInterface;
    private AlertDialog mDialog;
    private AlertDialog.Builder mBuilder;
    Calendar mStartCalendar, mEndCalendar;
    DialogInterface.OnClickListener onClick = null;

      public MonthViewFragment() {

          mStartCalendar = Calendar.getInstance();
          mEndCalendar = Calendar.getInstance();
       // this.mDialogID = dialogID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        try {
            this.mInterface = (DateInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DateInterface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.dialog_layout, null);

        // Define your month view pickers
        final CalendarView calStartDate = (CalendarView) customView
                .findViewById(R.id.monthViewStartDate);
        final CalendarView calEndDate = (CalendarView) customView
                .findViewById(R.id.monthViewEndDate);

        long startTime = UsageSharedPrefernceHelper.getCalendar(mContext, "startCalendar");
        long endTime = UsageSharedPrefernceHelper.getCalendar(mContext, "endCalendar");

        if (startTime != 0 && endTime != 0) {
            calStartDate.setDate(startTime, true, true);
            calEndDate.setDate(endTime, true, true);
        }

        calStartDate.setOnDateChangeListener(this);
        calEndDate.setOnDateChangeListener(this);

        // Build the dialog
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setView(customView); // Set the view of the dialog to your custom layout
        mBuilder.setTitle("Select start and end date");

        // Create and show the dialog
        mDialog = mBuilder.create();
        mDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.string_Ok), onClick);

        return mDialog;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        AlertDialog alertDialog = (AlertDialog) getDialog();
        Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                positiveBtnProcess();
            }
        });
    }

    private void positiveBtnProcess() {
        // Do your stuff here

        if (validateStartDate() && validateEndDate()) {
            mInterface.onDateSetComplete(mStartCalendar, mEndCalendar);
            mDialog.dismiss();
        }
    }

    private boolean validateStartDate() {

        boolean result = true;

        // Get a calendar for present date.
        Calendar presentCalendar = Calendar.getInstance();

        // Validate both dates with present date. (Cannot be greater than present date.)
        if (Utils.compareDates(mStartCalendar, presentCalendar) == 1) {
            Toast.makeText(mContext, mContext.getString(R.string.string_error_start_date_greater_than_today), Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;

    }

    private boolean validateEndDate() {

        boolean result = true;

        // Get a calendar for present date.
        Calendar presentCalendar = Calendar.getInstance();

        // Validate both dates with present date. (Cannot be greater than present date.)
        if (Utils.compareDates(mEndCalendar, presentCalendar) == 1) {
            Toast.makeText(mContext, mContext.getString(R.string.string_error_end_date_greater_than_today), Toast.LENGTH_SHORT).show();
            result = false;
        } else if (Utils.compareDates(mStartCalendar, mEndCalendar) == 1) {
            Toast.makeText(mContext, mContext.getString(R.string.string_error_end_date_lesser_than_start), Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        // TODO Auto-generated method stub

        switch (view.getId()) {
        case R.id.monthViewStartDate: // Validate start date here.
            mStartCalendar.set(Calendar.YEAR, year);
            mStartCalendar.set(Calendar.MONTH, month);
            mStartCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            break;
        case R.id.monthViewEndDate: // Validate end date here.
            mEndCalendar.set(Calendar.YEAR, year);
            mEndCalendar.set(Calendar.MONTH, month);
            mEndCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            break;
        }
    }
}

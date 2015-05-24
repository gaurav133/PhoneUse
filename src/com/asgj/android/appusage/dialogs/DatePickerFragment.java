package com.asgj.android.appusage.dialogs;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.asgj.android.appusage.R;


public class DatePickerFragment extends DialogFragment implements OnDateSetListener {

    public interface DateInterface {
        abstract void onDateSetComplete(int dialogID);
    }

    private int mDialogID;
    DatePickerDialog mDatePickerDialog;
    DateInterface mInterface;
    int mDayOfMonth;
    int mMonth;
    int mYear;
    Calendar mCalendar;
    String mDate;
    
    public int getYear() {
        return this.mYear;
    }
    
    public int getMonth() {
        return this.mMonth;
    }
    
    public int getDayOfMonth() {
        return this.mDayOfMonth;
    }
    
    public String getDate() {
        return this.mDate;
    }
    
    public Calendar getCalendar() {
        return this.mCalendar;
    }
    
    public DatePickerDialog getDialog() {
        return this.mDatePickerDialog;
    }
  
    public DatePickerFragment(int dialogID) {
        
        this.mDialogID = dialogID;
    }
    
    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        try {
            this.mInterface = (DateInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        // TODO Auto-generated constructor stub
        final Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        
        mDatePickerDialog = new DatePickerDialog(getActivity(), this, mYear, mMonth, mDayOfMonth);
        
        switch (mDialogID) {
        case 0 : mDatePickerDialog.setTitle(R.string.string_start_date);
                break;
        case 1 :  mDatePickerDialog.setTitle(R.string.string_end_date);
                break;
        default : break;
        }
        return mDatePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // TODO Auto-generated method stub
        mYear = year;
        mMonth = monthOfYear;
        mDayOfMonth = dayOfMonth;
        
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDayOfMonth);
        
        mInterface.onDateSetComplete(mDialogID);
    }
}

package com.asgj.android.appusage.dialogs;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.asgj.android.appusage.R;
//import android.view.View;

public class DatePickerFragment extends DialogFragment {

    private int dialogID;
    DatePickerDialog mDatePickerDialog;
    OnDateSetListener listener;
    int mDayOfMonth;
    int mMonth;
    int mYear;
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
    
    public DatePickerDialog getDialog() {
        return this.mDatePickerDialog;
    }
    public void setOnDateSetListener(OnDateSetListener listener) {
        this.listener = listener;
    }
    
    public DatePickerFragment(int dialogID) {
        
        this.dialogID = dialogID;
        //return mDatePickerDialog;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        // TODO Auto-generated constructor stub
        final Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        
        mDatePickerDialog = new DatePickerDialog(getActivity(), listener, mYear, mMonth, mDayOfMonth);
        
        switch (dialogID) {
        case 0 : mDatePickerDialog.setTitle(R.string.string_start_date);
                break;
        case 1 :  mDatePickerDialog.setTitle(R.string.string_end_date);
                break;
        default : break;
        }
        return mDatePickerDialog;
    }


}

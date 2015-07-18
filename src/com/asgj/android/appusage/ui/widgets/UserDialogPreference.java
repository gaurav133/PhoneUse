package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.ResolveInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;

public class UserDialogPreference extends DialogPreference implements View.OnClickListener {

    Context mContext = null;
    ArrayList<ResolveInfo> mResolveInfo;
    boolean mIsChecked[];
    
    PreferenceListAdapter mAdapter = null;
    private static final int NOTIFICATION_PREF = 1;
    private static final int PACKAGES_FILTER_PREF = 2;
    private int mCurrentPref = 0;
    private HashMap<String, Long> mSelectedAlertMap;
    private HashMap<String, Long> mAlertMap;

    @Override
    protected void showDialog(Bundle state) {
        // TODO Auto-generated method stub
    	
    	if(mAdapter==null || mResolveInfo == null || mResolveInfo.size() == 0)
    		return;
        Set<String> mAlreadySelectedSet = null;
        mAlertMap = mAdapter.getMonitorMap();
        int selectedCount = 0;
        if (mAdapter.getCurrentPref() == PreferenceListAdapter.NOTIFICATION_PREF) {
            mSelectedAlertMap = UsageSharedPrefernceHelper.getApplicationsDurationForTracking(mContext);
        } else {
            mAlreadySelectedSet = UsageSharedPrefernceHelper
                    .getSelectedApplicationForFiltering(mContext);
        }
        ArrayList<ResolveInfo> list = mAdapter.getSelectedPackages();
        mIsChecked = new boolean[list.size()];
        // TODO : need to get time also from prefernce to show on seekbar.
        if (mCurrentPref == PACKAGES_FILTER_PREF) {
        if (mAlreadySelectedSet != null && mAlreadySelectedSet.size() > 0) {

                for (int i = 0; i < list.size(); i++) {
                    ResolveInfo info = list.get(i);
                    if (mAlreadySelectedSet.contains(info.getmPackageName())) {
                        mIsChecked[i] = true;
                        // info.setChecked(true);
                        selectedCount++;
                    } else {
                        mIsChecked[i] = false;
                    }
                }
            }
        } else {
            if (mSelectedAlertMap != null && !mSelectedAlertMap.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    ResolveInfo info = list.get(i);
                    if (mSelectedAlertMap.containsKey(info.getmPackageName())) {
                        mIsChecked[i] = true;
                        // info.setChecked(true);
                        selectedCount++;
                    } else {
                        // info.setChecked(false);
                        mIsChecked[i] = false;
                    }
                }
            }
        }

        // mAdapter.setPackageList(list);
        mAdapter.setSelectedCount(selectedCount);
        mAdapter.setCheckedArray(mIsChecked, mSelectedAlertMap);

        super.showDialog(state);
        Button positiveBtn = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeBtn = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);
        Button uncheckAllBtn = ((AlertDialog) getDialog())
                .getButton(DialogInterface.BUTTON_NEUTRAL);

        positiveBtn.setOnClickListener(this);
        negativeBtn.setOnClickListener(this);
        uncheckAllBtn.setOnClickListener(this);
    }
    public void setPackageList(ArrayList<ResolveInfo> pkgList){
    	mResolveInfo = pkgList;
    	mAdapter = new PreferenceListAdapter(mResolveInfo, mContext,mCurrentPref);
    }

	public UserDialogPreference(Context context, AttributeSet attrs)
			throws Exception {
		super(context, attrs);
		mContext = context;
		if (this.getKey().equals("user_packges_pref")) {
            mCurrentPref = NOTIFICATION_PREF;
            this.setTitle(R.string.string_select_packages_pref_title);
            this.setSummary(R.string.string_select_packages_dialog_title);
        } else if (this.getKey().equals("filter_pkages")) {
            mCurrentPref = PACKAGES_FILTER_PREF;
            this.setTitle(R.string.string_filter_packages_pref_title);
            this.setSummary(R.string.string_filter_packages_dialog_title);
        }
	}

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        if (mCurrentPref == NOTIFICATION_PREF) {
            builder.setTitle(R.string.string_select_packages_dialog_title);
        } else {
            builder.setTitle(R.string.string_filter_packages_dialog_title);
        }
        builder.setAdapter(mAdapter, this);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.string_uncheck_all, null);
        super.onPrepareDialogBuilder(builder);
    }

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
	}

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case android.R.id.button1:

            mAlertMap = mAdapter.getMonitorMap();
            ArrayList<ResolveInfo> pkgList = mAdapter.getSelectedPackages();
            mIsChecked = mAdapter.getCheckedArray();
            if (mCurrentPref == NOTIFICATION_PREF) {

                // Send complete map to preference.
                UsageSharedPrefernceHelper.setApplicationsForTracking(mContext, mAlertMap);
                
                // Set date in preferences for apps.
                UsageSharedPrefernceHelper.setCurrentDate(getContext());
                
                // Send broadcast to service to track set packages.
                Intent notifyBroadcast = new Intent();
                
                if (UsageSharedPrefernceHelper.isServiceRunning(getContext())) {
                    notifyBroadcast.setAction("com.android.asgj.appusage.action.NOTIFICATION_ALERT");
                } else {
                    notifyBroadcast.setAction("com.android.asgj.appusage.action.NOTIFICATION_ALERT_ACTIVITY");
                }
                mContext.sendBroadcast(notifyBroadcast);
            } else {
            for (int i = 0; i < pkgList.size(); i++) {
                ResolveInfo info = pkgList.get(i);

                    if (mIsChecked[i]) {
                        
                            UsageSharedPrefernceHelper.setApplicationForFiltering(mContext,
                                    info.getmPackageName(), true);
                        
                        // TODO :: please save time also.
                    } else {
                        UsageSharedPrefernceHelper.setApplicationForFiltering(mContext,
                                info.getmPackageName(), false);
                    }
                }
            }

            getDialog().dismiss();
            break;
        case android.R.id.button2:
            getDialog().dismiss();
            break;
        case android.R.id.button3:

            if (mAdapter != null && mAdapter.getSelectedPackages().size() != 0) {

                mIsChecked = new boolean[mAdapter.getSelectedPackages().size()];
                Arrays.fill(mIsChecked, false);

                Iterator<ResolveInfo> iterator = mAdapter.getSelectedPackages().iterator();

                if (mCurrentPref == NOTIFICATION_PREF) {
                    mAlertMap.clear();
                    UsageSharedPrefernceHelper.setApplicationsForTracking(mContext, mAlertMap);
                } else {
                    while (iterator.hasNext()) {
                        ResolveInfo info = iterator.next();
                        String pkg = info.getmPackageName();

                        UsageSharedPrefernceHelper.setApplicationForFiltering(mContext, pkg, false);
                    }
                    // info.setChecked(false);
                }
                mAdapter.setCheckedArray(mIsChecked,null);
                mAdapter.setSelectedCount(0);
                mAdapter.notifyDataSetChanged();
            }
            break;
        }
    }
}

package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    

	private void initPackageList() {
		mResolveInfo = new ArrayList<ResolveInfo>();

		PackageManager packageManager = mContext.getPackageManager();
		Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
		launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		ArrayList<android.content.pm.ResolveInfo> mAppInfo = new ArrayList<>();
		mAppInfo = (ArrayList<android.content.pm.ResolveInfo>) packageManager
				.queryIntentActivities(launcherIntent, 0);

		for (android.content.pm.ResolveInfo info : mAppInfo) {
			ResolveInfo infoItem = new ResolveInfo();
			infoItem.setmApplicationName(info.loadLabel(packageManager)
					.toString());
			infoItem.setmPackageName(info.activityInfo.packageName);
			mResolveInfo.add(infoItem);
		}

		mAdapter = new PreferenceListAdapter(mResolveInfo, mContext,mCurrentPref);
	}

    @Override
    protected void showDialog(Bundle state) {
        // TODO Auto-generated method stub
    	
    	if(mResolveInfo == null || mResolveInfo.size() == 0)
    		return;
        Set<String> mAlreadySelectedSet = null;
        int selectedCount = 0;
        if (mAdapter.getCurrentPref() == PreferenceListAdapter.NOTIFICATION_PREF) {
            mAlreadySelectedSet = UsageSharedPrefernceHelper
                    .getSelectedApplicationForTracking(mContext);
        } else {
            mAlreadySelectedSet = UsageSharedPrefernceHelper
                    .getSelectedApplicationForFiltering(mContext);
        }
        ArrayList<ResolveInfo> list = mAdapter.getSelectedPackages();
        mIsChecked = new boolean[list.size()];

        // TODO : need to get time also from prefernce to show on seekbar.
        if (mAlreadySelectedSet != null && mAlreadySelectedSet.size() > 0) {

            for (int i = 0; i < list.size(); i++) {
                ResolveInfo info = list.get(i);
                if (mAlreadySelectedSet.contains(info.getmPackageName())) {
                    mIsChecked[i] = true;
                    // info.setChecked(true);
                    selectedCount++;
                } else {
                    // info.setChecked(false);
                    mIsChecked[i] = false;
                }
            }
        }

        // mAdapter.setPackageList(list);
        mAdapter.setSelectedCount(selectedCount);
        mAdapter.setCheckedArray(mIsChecked);

        super.showDialog(state);
        Button positiveBtn = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeBtn = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);
        Button uncheckAllBtn = ((AlertDialog) getDialog())
                .getButton(DialogInterface.BUTTON_NEUTRAL);

        positiveBtn.setOnClickListener(this);
        negativeBtn.setOnClickListener(this);
        uncheckAllBtn.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToActivity() {

        if (this.getKey().equals("user_packges_pref")) {
            mCurrentPref = NOTIFICATION_PREF;
            this.setTitle(R.string.string_select_packages_pref_title);
            this.setSummary(R.string.string_select_packages_dialog_title);
        } else if (this.getKey().equals("filter_pkages")) {
            mCurrentPref = PACKAGES_FILTER_PREF;
            this.setTitle(R.string.string_filter_packages_pref_title);
            this.setSummary(R.string.string_filter_packages_dialog_title);
        }
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				  initPackageList();
			}
		}).start();
      
        super.onAttachedToActivity();
    }

	public UserDialogPreference(Context context, AttributeSet attrs)
			throws Exception {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected void onBindView(View view) {

		super.onBindView(view);
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

            ArrayList<ResolveInfo> pkgList = mAdapter.getSelectedPackages();
            mIsChecked = mAdapter.getCheckedArray();
            for (int i = 0; i < pkgList.size(); i++) {
                ResolveInfo info = pkgList.get(i);

                if (mIsChecked[i]) {
                    if (mCurrentPref == NOTIFICATION_PREF) {
                        UsageSharedPrefernceHelper.setApplicationForTracking(mContext,
                                info.getmPackageName(), true);
                    } else {
                        UsageSharedPrefernceHelper.setApplicationForFiltering(mContext,
                                info.getmPackageName(), true);
                    }
                    // TODO :: please save time also.
                } else {
                    if (mCurrentPref == NOTIFICATION_PREF) {
                        UsageSharedPrefernceHelper.setApplicationForTracking(mContext,
                                info.getmPackageName(), false);
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

                while (iterator.hasNext()) {
                    ResolveInfo info = iterator.next();
                    String pkg = info.getmPackageName();
                    if (mCurrentPref == NOTIFICATION_PREF) {
                        UsageSharedPrefernceHelper.setApplicationForTracking(mContext, pkg, false);
                    } else {
                        UsageSharedPrefernceHelper.setApplicationForFiltering(mContext, pkg, false);
                    }
                    // info.setChecked(false);
                }
                mAdapter.setCheckedArray(mIsChecked);
                mAdapter.setSelectedCount(0);
                mAdapter.notifyDataSetChanged();
            }
            break;
        }
    }
}

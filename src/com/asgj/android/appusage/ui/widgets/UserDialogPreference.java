package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.ResolveInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;

public class UserDialogPreference extends DialogPreference implements
		DialogInterface.OnClickListener {

	Context mContext = null;
	ArrayList<ResolveInfo> mResolveInfo;
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
		initPackageList();
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
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
		super.onPrepareDialogBuilder(builder);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			ArrayList<ResolveInfo> selectedPackages = mAdapter
					.getSelectedPackages();
			for (ResolveInfo pkg : selectedPackages) {
				if (pkg.isChecked()) {
					if (mCurrentPref == NOTIFICATION_PREF) {
						UsageSharedPrefernceHelper.setApplicationForTracking(
								mContext, pkg.getmPackageName(),true);
					} else {
						UsageSharedPrefernceHelper.setApplicationForFiltering(
								mContext, pkg.getmPackageName(),true);
					}
					// TODO :: please save time also.
				}
			}
			
			for(String pkg : mAdapter.getUnSelectedPackages()){
				if (mCurrentPref == NOTIFICATION_PREF) {
					UsageSharedPrefernceHelper.setApplicationForTracking(
							mContext, pkg,false);
				} else {
					UsageSharedPrefernceHelper.setApplicationForFiltering(
							mContext, pkg,false);
				}
			}
		}
		super.onClick(dialog, which);
	}

}

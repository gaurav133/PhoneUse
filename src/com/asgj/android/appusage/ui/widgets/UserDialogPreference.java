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
            infoItem.setmApplicationName(info.loadLabel(packageManager).toString());
            mResolveInfo.add(infoItem);
        }

        mAdapter = new PreferenceListAdapter(mResolveInfo, mContext);
    }

	public UserDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		this.setTitle(R.string.string_select_packages_pref_title);
		this.setSummary(R.string.string_select_packages_dialog_title);
	}

	@Override
	protected void onBindView(View view) {
		
		super.onBindView(view);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		initPackageList();
		builder.setAdapter(mAdapter, this);
		builder.setTitle(R.string.string_select_packages_dialog_title);
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
			ArrayList<ResolveInfo> selectedPackages = mAdapter.getSelectedPackages();
			for (ResolveInfo pkg : selectedPackages) {
				if(pkg.isChecked()){
				UsageSharedPrefernceHelper.setApplicationForTracking(mContext,
						pkg.getmApplicationName());
				//TODO :: please save time also.
				}
			}
		}
		super.onClick(dialog, which);
	}

}

package com.asgj.android.appusage.activities;

import java.util.ArrayList;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.ResolveInfo;
import com.asgj.android.appusage.ui.widgets.UserDialogPreference;

public class SettingPreferenceFragment extends PreferenceFragment{
	
	ArrayList<ResolveInfo> mPackageListLauncher = null;
	UserDialogPreference mFilterPref = null;
	UserDialogPreference mMoniterPref = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefernces);
		mPackageListLauncher = getActivity().getIntent().getParcelableArrayListExtra("packageList");
		mMoniterPref = (UserDialogPreference)findPreference("user_packges_pref");
		mFilterPref = (UserDialogPreference)findPreference("filter_pkages");
		mMoniterPref.setPackageList(mPackageListLauncher);
		mFilterPref.setPackageList(mPackageListLauncher);
	}

}

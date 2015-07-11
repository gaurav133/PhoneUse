package com.asgj.android.appusage.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.ResolveInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.ui.widgets.UserDialogPreference;

public class SettingPreferenceFragment extends PreferenceFragment implements OnPreferenceClickListener{
	
	ArrayList<ResolveInfo> mPackageListLauncher = null;
	UserDialogPreference mFilterPref = null;
	UserDialogPreference mMoniterPref = null;
	CheckBoxPreference mSwipeEnablePref = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefernces);
		mPackageListLauncher = getActivity().getIntent().getParcelableArrayListExtra("packageList");
		mMoniterPref = (UserDialogPreference)findPreference("user_packges_pref");
		mFilterPref = (UserDialogPreference)findPreference("filter_pkages");
		mSwipeEnablePref = (CheckBoxPreference)findPreference("pref_swipe_share");
		mSwipeEnablePref.setOnPreferenceClickListener(this);
		mSwipeEnablePref.setChecked(UsageSharedPrefernceHelper.getSwipeFeatureEnable(getActivity()));
		mMoniterPref.setPackageList(mPackageListLauncher);
		mFilterPref.setPackageList(mPackageListLauncher);
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(mSwipeEnablePref.getKey())){
			UsageSharedPrefernceHelper.setSwipeFeatureEnable(getActivity(), mSwipeEnablePref.isChecked());
		}
		return false;
	}

}

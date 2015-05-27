package com.asgj.android.appusage.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class SettingActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 FragmentManager mFragmentManager = getFragmentManager();
         FragmentTransaction mFragmentTransaction = mFragmentManager
                                 .beginTransaction();
         SettingPreferenceFragment mPrefsFragment = new SettingPreferenceFragment();
         mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
         mFragmentTransaction.commit();
	}
}

package com.asgj.android.appusage.activities;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.ResolveInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.ui.widgets.UserDialogPreference;

public class SettingPreferenceFragment extends PreferenceFragment implements OnPreferenceClickListener{
	
	ArrayList<ResolveInfo> mPackageListLauncher = null;
	UserDialogPreference mFilterPref = null;
	UserDialogPreference mMoniterPref = null;
	CheckBoxPreference mSwipeEnablePref = null;
	Preference mAutoTrackPref;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefernces);
		mPackageListLauncher = getActivity().getIntent().getParcelableArrayListExtra("packageList");
		mMoniterPref = (UserDialogPreference)findPreference("user_packges_pref");
		mAutoTrackPref = findPreference("tracking_type_pref");
		mAutoTrackPref.setOnPreferenceClickListener(this);
		mFilterPref = (UserDialogPreference)findPreference("filter_pkages");
		mSwipeEnablePref = (CheckBoxPreference)findPreference("pref_swipe_share");
		mSwipeEnablePref.setOnPreferenceClickListener(this);
		mSwipeEnablePref.setChecked(UsageSharedPrefernceHelper.getSwipeFeatureEnable(getActivity()));
		mMoniterPref.setPackageList(mPackageListLauncher);
		mFilterPref.setPackageList(mPackageListLauncher);
	}
	@Override
	public void onResume() {
	    // TODO Auto-generated method stub
	    super.onResume();
	    if (getActivity() != null && getActivity().getActionBar() != null) {
            View view = getActivity().getActionBar().getCustomView();
            if(view != null){
                TextView mTitleTextView = (TextView) view.findViewById(R.id.title_text);
                mTitleTextView.setText(getString(R.string.string_settings));
            }
        }
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(mSwipeEnablePref.getKey())){
			UsageSharedPrefernceHelper.setSwipeFeatureEnable(getActivity(), mSwipeEnablePref.isChecked());
		} else if (preference.getKey().equals("tracking_type_pref")) {
		    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
		        if (!Utils.isPermissionGranted(getActivity())) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.string_error_title)
                            .setMessage(R.string.string_error_msg)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                int which) {
                                            Intent intent = new Intent(
                                                    Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                            startActivity(intent);
                                        }
                                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                
                    return true;
		        }
		    }
		    
		    Fragment fragment = getFragmentManager().findFragmentByTag("auto_track_frag");
		    if (fragment != null) {
		        getFragmentManager().popBackStackImmediate();
		    }
		    
		    FragmentTransaction transaction = getFragmentManager().beginTransaction();
		    transaction.replace(android.R.id.content, new AutoTrackingFragment(), "auto_track_frag");
		    transaction.addToBackStack(null);
		    transaction.commit();
		}
		return false;
	}

}

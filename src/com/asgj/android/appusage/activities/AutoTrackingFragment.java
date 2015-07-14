package com.asgj.android.appusage.ui.widgets;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.TextView;

import com.asgj.android.appusage.R;

public class AutoTrackingFragment extends PreferenceFragment implements OnPreferenceClickListener{
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.auto_track);
        
        if (getActivity() != null && getActivity().getActionBar() != null) {
            View view = getActivity().getActionBar().getCustomView();
            if(view != null){
                TextView mTitleTextView = (TextView) view.findViewById(R.id.title_text);
                mTitleTextView.setText(getString(R.string.string_auto_track));
            }
        }
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        
        return false;
    }

}

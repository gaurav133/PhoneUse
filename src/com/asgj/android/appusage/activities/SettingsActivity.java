package com.asgj.android.appusage.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asgj.android.appusage.R;

public class SettingsActivity extends Activity implements View.OnClickListener {
    
    private ActionBar mActionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayShowHomeEnabled(false);
		initActionBar();
		 FragmentManager mFragmentManager = getFragmentManager();
         FragmentTransaction mFragmentTransaction = mFragmentManager
                                 .beginTransaction();
         SettingPreferenceFragment mPrefsFragment = new SettingPreferenceFragment();
         mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
         mFragmentTransaction.commit();
	}
	
	private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View customView = mInflater.inflate(R.layout.custom_action_bar, null);
        
        LinearLayout actionView = (LinearLayout) customView.findViewById(R.id.action_title_view);
        ImageView imageView = (ImageView) actionView.findViewById(R.id.imageView1);
        imageView.setVisibility(View.VISIBLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            actionView.setOnClickListener(this);
        } else {
            actionView.setBackground(null);
            imageView.setBackground(getResources().getDrawable(R.drawable.image_item_selector));
            imageView.setOnClickListener(this);
        }

        TextView mTitleTextView = (TextView) actionView.findViewById(R.id.title_text);
        mTitleTextView.setTextColor(getResources().getColor(android.R.color.white));
        mTitleTextView.setText(getString(R.string.action_settings));
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        LinearLayout layout = (LinearLayout) customView.findViewById(R.id.action_title_view);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        layout.setPadding(0, 0, 0, 0);
        layout.setLayoutParams(params);

        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);
    }
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item != null && item.getItemId() == android.R.id.home){
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.action_title_view: onBackPressed();
                              		 break;
        case R.id.imageView1 : onBackPressed();
                               break;
        }
    }
}

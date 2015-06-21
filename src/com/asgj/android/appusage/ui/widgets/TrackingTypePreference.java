package com.asgj.android.appusage.ui.widgets;

import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.ui.widgets.RangeSeekBar.OnRangeSeekBarChangeListener;

public class TrackingTypePreference extends SwitchPreference implements
		OnCheckedChangeListener, OnRangeSeekBarChangeListener<Integer> {

	Context mContext = null;
	Switch mSwitchView = null;
	String summaryOn = null;
	String summaryOff = null;
	String mTitle = null;
	TextView mTitleView = null;
	TextView mTimeSelectedStart = null;
	TextView mTimeSelectedEnd = null;
	RangeSeekBar mTimeRangeSelectionBar = null;

	public TrackingTypePreference(Context context, AttributeSet attrs) {
		super(context);
		mContext = context;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.custom_track_pref_layout, null);
		mSwitchView = (Switch) view.findViewById(R.id.switchWidget);
		mTitleView = (TextView) view.findViewById(R.id.title);
		mTimeSelectedStart = (TextView) view
				.findViewById(R.id.timeSelectedStart);
		mTimeSelectedEnd = (TextView) view.findViewById(R.id.timeSelectedEnd);
		mTimeRangeSelectionBar = (RangeSeekBar) view
				.findViewById(R.id.timeRangeView);
		mTimeRangeSelectionBar.setSelectedMinValue(UsageSharedPrefernceHelper
				.getTrackingStartTime(mContext));
		mTimeRangeSelectionBar.setSelectedMaxValue(UsageSharedPrefernceHelper
				.getTrackingEndTime(mContext));
		mSwitchView.setOnCheckedChangeListener(this);
		mTimeRangeSelectionBar.setNotifyWhileDragging(true);
		mTimeRangeSelectionBar.setOnRangeSeekBarChangeListener(this);
		mSwitchView.setChecked(UsageSharedPrefernceHelper
				.getTrackingMode(mContext));
		this.setSummaryOff(R.string.string_default_message_track_pref);
		this.setSummaryOn(R.string.string_user_choice_message_track_pref);
		this.setTitle(R.string.string_title_track_pref);
		this.setSummaryPref(UsageSharedPrefernceHelper
				.getTrackingMode(mContext));
		return view;
	}

	@Override
	public void setSummaryOff(int summaryResId) {
		summaryOff = mContext.getResources().getString(summaryResId);
	}

	@Override
	public void setSummaryOn(int summaryResId) {
		summaryOn = mContext.getResources().getString(summaryResId);
	}

	private void setSummaryPref(boolean isCustomMode) {
		if (isCustomMode) {
			mSwitchView.setText(summaryOn);
			mTimeSelectedStart.setVisibility(View.VISIBLE);
			mTimeSelectedEnd.setVisibility(View.VISIBLE);
			mTimeSelectedStart.setText(""+UsageSharedPrefernceHelper
					.getTrackingStartTime(mContext));
			mTimeSelectedEnd.setText(""+UsageSharedPrefernceHelper
					.getTrackingEndTime(mContext));
			mTimeRangeSelectionBar.setVisibility(View.VISIBLE);
		} else {
			mSwitchView.setText(summaryOff);
			mTimeSelectedStart.setVisibility(View.GONE);
			mTimeSelectedEnd.setVisibility(View.GONE);
			mTimeRangeSelectionBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void setTitle(int summaryResId) {
		mTitle = mContext.getResources().getString(summaryResId);
		mTitleView.setText(mTitle);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		UsageSharedPrefernceHelper.setTrackingMode(mContext, isChecked);
		this.setSummaryPref(isChecked);

	}

	@Override
	public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Integer minValue,
			Integer maxValue) {
		UsageSharedPrefernceHelper.setTrackingStartTime(mContext, minValue);
		UsageSharedPrefernceHelper.setTrackingEndTime(mContext, maxValue);
		mTimeSelectedStart.setText(""+minValue);
		mTimeSelectedEnd.setText(""+maxValue);
	}

}

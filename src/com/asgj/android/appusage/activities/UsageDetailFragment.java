package com.asgj.android.appusage.activities;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.ui.widgets.GraphView;

public class UsageDetailFragment extends Fragment {

	private float mMaxDuration;
	private ArrayList<UsageInfo> mIntervalList = null;
	private String[] mVerticalLabels = new String[10];
	private String[] mHorizontalLabels = new String[] { "0", "1", "2", "3",
			"4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
			"16", "17", "18", "19", "20", "21", "22", "23", "24" };

	private float[] mIntervalDurationList = new float[24];
	private float[] mIntervalStartTimeList = new float[24];
	private float[] mIntervalEndTimeList = new float[24];
	private String mApplicationName;

	UsageDetailFragment(ArrayList<UsageInfo> intervalList, String appName) {
		mIntervalList = intervalList;
		mApplicationName = appName;
		try {
			prepareDataForGraph();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void prepareDataForGraph() throws Exception {
		if (mIntervalList == null || mIntervalList.size() == 0) {
			throw new Exception("empty enterval list found..");
		}
		long maxDuration = mIntervalList.get(0).getmIntervalDuration();
		for (UsageInfo interval : mIntervalList) {
			long starttime = interval.getmIntervalStartTime();
			for (int i = 0; i < mHorizontalLabels.length - 1; i++) {
				float horValue = (float) Integer.parseInt(mHorizontalLabels[i]);
				float horValue1 = (float) Integer
						.parseInt(mHorizontalLabels[i + 1]);
				if (starttime >= horValue && starttime < horValue1) {
					mIntervalStartTimeList[i] = starttime;
					mIntervalEndTimeList[i] = interval.getmIntervalEndTime();
					mIntervalDurationList[i] = interval.getmIntervalDuration();
				}
			}
			if (maxDuration < interval.getmIntervalDuration()) {
				maxDuration = interval.getmIntervalDuration();
			}
		}

		mMaxDuration = maxDuration;
		for (int i = 0; i < 10; i++) {
			mVerticalLabels[i] = "" + (mMaxDuration * (i + 1) / 10);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// parent layout
		LinearLayout parentLayout = new LinearLayout(getActivity());

		// scrollview for making view vertically scrollable
		ScrollView scrollView = new ScrollView(getActivity());

		// horizontal scrollview for making view horizontally scrollable
		HorizontalScrollView horiScrollView = new HorizontalScrollView(
				getActivity());

		// graph view used for showng bar chart.
		GraphView graph = new GraphView(getActivity(), mIntervalDurationList,
				mApplicationName, mHorizontalLabels, mVerticalLabels,
				mIntervalStartTimeList, mIntervalEndTimeList);

		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		horiScrollView.addView(graph);
		scrollView.addView(horiScrollView);
		scrollView.setLayoutParams(params);
		parentLayout.addView(scrollView);
		return parentLayout;
	}

}

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

import com.asgj.android.appusage.Utility.UsageInterval;
import com.asgj.android.appusage.ui.widgets.GraphView;

public class UsageDetailFragment extends Fragment {

	float maxDuration;
	ArrayList<UsageInterval> mIntervalList = null;
	String[] verlabels = new String[10];
	String[] horlabels = new String[] { "0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18",
			"19", "20", "21", "22", "23", "24" };

	float[] durations = new float[24];
	float[] startTime = new float[24];
	float[] endTime = new float[24];
	String mApplicationName;

	UsageDetailFragment(ArrayList<UsageInterval> intervalList, String appName) {
		mIntervalList = intervalList;
		try {
			maxDuration = maxDuration();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 10; i++) {
			verlabels[i] = "" + (maxDuration * (i + 1) / 10);
		}
		mApplicationName = appName;

	}

	private int maxDuration() throws Exception {
		if (mIntervalList == null || mIntervalList.size() == 0) {
			throw new Exception("empty enterval list found..");
		}
		int maxDuration = mIntervalList.get(0).getDuration();
		for (UsageInterval interval : mIntervalList) {
			int starttime = interval.getStartTime();
			for (int i = 0; i < horlabels.length - 1; i++) {
				float horValue = (float) Integer.parseInt(horlabels[i]);
				float horValue1 = (float) Integer.parseInt(horlabels[i + 1]);
				if (starttime >= horValue && starttime < horValue1) {
					startTime[i] = starttime;
					endTime[i] = interval.getEndTime();
					durations[i] = interval.getDuration();
				}
			}
			if (maxDuration < interval.getDuration()) {
				maxDuration = interval.getDuration();
			}
		}
		return maxDuration;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout ll = new LinearLayout(getActivity());
		ScrollView scrollView = new ScrollView(getActivity());
		HorizontalScrollView horiScrollView = new HorizontalScrollView(
				getActivity());
		GraphView graph = new GraphView(getActivity(), durations,
				mApplicationName, horlabels, verlabels, startTime, endTime);
		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		horiScrollView.addView(graph);
		scrollView.addView(horiScrollView);
		scrollView.setLayoutParams(params);
		ll.addView(scrollView);
		return ll;
	}

}

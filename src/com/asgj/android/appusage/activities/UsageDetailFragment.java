package com.asgj.android.appusage.activities;

import java.util.ArrayList;
import java.util.Calendar;

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
	private String[] mHorizontalLabels = null;
	private float[] mIntervalDurationList = new float[24];
	private float[] mIntervalStartTimeList = new float[24];
	private float[] mIntervalEndTimeList = new float[24];
	private String mApplicationName;

	/**
	 * 
	 * @param intervalList list of intervals having starttime, endtime and durations. it totally dependent on the showby.
	 * @param appName name of application for which graph is need to shown
	 * @param showBy this is most important parameter for detail fragment. it actually decides the horizontal labels.
	 */
	UsageDetailFragment(ArrayList<UsageInfo> intervalList, String appName,String showBy) {
		mIntervalList = intervalList;
		mApplicationName = appName;
		switch (showBy) {
		case "Today":
			mHorizontalLabels =new String[] { "0", "1", "2", "3",
					"4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
					"16", "17", "18", "19", "20", "21", "22", "23", "24" };
			break;
		case "Weekly":
			mHorizontalLabels =new String[] { "0", "1", "2", "3",
					"4", "5", "6", "7"};
			break;
		case "Monthly":
			mHorizontalLabels =new String[] { "0", "1", "2", "3",
					"4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
					"16", "17", "18", "19", "20", "21", "22", "23", "24","25","26","27","28","29","30" };
			break;
		case "Yearly":
			mHorizontalLabels =new String[] { "0", "1", "2", "3",
					"4", "5", "6", "7", "8", "9", "10", "11"};
			break;
		case "Custom":
			
			long minStartTime = Integer.MAX_VALUE;
			long maxStarttime = Integer.MIN_VALUE;
			for(UsageInfo interval : mIntervalList){
				if(interval.getmIntervalStartTime() < minStartTime){
					minStartTime = interval.getmIntervalStartTime();
				}
				if(interval.getmIntervalStartTime() > maxStarttime){
					maxStarttime = interval.getmIntervalStartTime();
				}
			}
			Calendar cal1 = Calendar.getInstance();
			cal1.setTimeInMillis(minStartTime);
			Calendar cal2 = Calendar.getInstance();
			cal2.setTimeInMillis(maxStarttime);
			int comp1 = cal1.get(Calendar.YEAR);
	        int comp2 = cal2.get(Calendar.YEAR);
	        
	        
	        if (comp1 != comp2) {
	        	//if dates diff is nore than year than show months.
	        	mHorizontalLabels =new String[] { "0", "1", "2", "3",
						"4", "5", "6", "7", "8", "9", "10", "11"};
	        	break;
	        }
	        
	        comp1 = cal1.get(Calendar.MONTH);
	        comp2 = cal2.get(Calendar.MONTH);
	        
	        if (comp1 != comp2) {
	        	//if dates diff is nore than no of months.
	        	mHorizontalLabels =new String[] { "0", "1", "2", "3",
						"4", "5", "6", "7", "8", "9", "10", "11"};
	        	break;
	        }
	        
	        comp1 = cal1.get(Calendar.DAY_OF_MONTH);
	        comp2 = cal2.get(Calendar.DAY_OF_MONTH);
	        int noofdays = 0;
	        if (comp1 != comp2) {
	        	//if dates are not equal and have diff less than 30 days then show days upto diff.
	        	noofdays = noofdays+ (Math.abs(comp1-comp2)); 
	        	mHorizontalLabels =new String[noofdays];
				for(int i =0; i< noofdays ; i++){
					mHorizontalLabels[i] = ""+ i;
				}
	        }else{
	        	//if dates are equal, then show in hours.
	        	mHorizontalLabels =new String[] { "0", "1", "2", "3",
						"4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
						"16", "17", "18", "19", "20", "21", "22", "23", "24" };
	        	break;
	        }
			
			
			break;
		}
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

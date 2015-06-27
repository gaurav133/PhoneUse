package com.asgj.android.appusage.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.ui.widgets.MusicListAdapter;

public class UsageDetailListFragment extends Fragment {

	MusicListAdapter mAdapter = null;
	private OnDetachFromActivity mOnDetachListener = null;
	private HashMap<Long,UsageInfo> mInfoList = null;
	
	public UsageDetailListFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public interface OnDetachFromActivity{
		public void onDetach();
	}
	
	public void setOnDetachListener(OnDetachFromActivity onDetachListener){
		mOnDetachListener = onDetachListener;
	}
	
	@Override
	public void onDetach() {
		if(mOnDetachListener != null){
			mOnDetachListener.onDetach();
		}
		super.onDetach();
	}
	

	public UsageDetailListFragment(HashMap<Long,UsageInfo> infoList) {
		mInfoList = infoList;
	}

	public void updateDetailList(HashMap<Long,UsageInfo> infoList) {
		mInfoList = infoList;
		mAdapter = new MusicListAdapter(infoList, getActivity());
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout parentLayout = new LinearLayout(getActivity());
		ExpandableListView list = new ExpandableListView(getActivity());
		LinearLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		list.setLayoutParams(params);
		mAdapter = new MusicListAdapter(mInfoList, getActivity());
		list.setAdapter(mAdapter);
		parentLayout.addView(list);
		return parentLayout;
	}

}

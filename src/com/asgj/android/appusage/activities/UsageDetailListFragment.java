package com.asgj.android.appusage.activities;

import java.util.HashMap;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.ui.widgets.MusicListAdapter;

public class UsageDetailListFragment extends Fragment {

	MusicListAdapter mAdapter = null;
	private OnDetachFromActivity mOnDetachListener = null;
	private HashMap<Long,UsageInfo> mInfoList = null;
	private String mCurrentPackageName = null;
	private String mTotalDuration = null;
	
	public UsageDetailListFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public void setPackageNameAndDuration(String pkgName,String totalDur){
		mCurrentPackageName = pkgName;
		mTotalDuration = totalDur;
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
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    // TODO Auto-generated method stub
	    super.onCreateOptionsMenu(menu, inflater);
	    
	    if (!Utils.isTabletDevice(getActivity())) {
	        if (menu != null) {
	            menu.findItem(R.id.action_start).setVisible(false);
	            menu.findItem(R.id.action_settings).setVisible(false);
	            menu.findItem(R.id.action_showBy).setVisible(false);
	        }
	    }
	}

	public UsageDetailListFragment(HashMap<Long,UsageInfo> infoList) {
		mInfoList = infoList;		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    // TODO Auto-generated method stub
	    super.onCreate(savedInstanceState);
        if (!Utils.isTabletDevice(getActivity().getApplicationContext())) {
            if (getActivity() != null && getActivity().getActionBar() != null) {
                View view = getActivity().getActionBar().getCustomView();
                if(view != null){
                	TextView mTitleTextView = (TextView) view.findViewById(R.id.title_text);
                	mTitleTextView.setText(mCurrentPackageName);
                }
            }
        }
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    // TODO Auto-generated method stub
	    super.onActivityCreated(savedInstanceState);
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
		mAdapter.setPackageNameAndDuration(mCurrentPackageName, mTotalDuration);
		getActivity().getActionBar().setTitle(mCurrentPackageName);
		list.setAdapter(mAdapter);
		list.setChildDivider(null);
		list.setDivider(null);
		list.setDividerHeight(0);
		list.setGroupIndicator(null);
		parentLayout.addView(list);
		
		setHasOptionsMenu(true);
		return parentLayout;
	}

}

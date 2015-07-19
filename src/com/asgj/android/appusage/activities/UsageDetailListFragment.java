package com.asgj.android.appusage.activities;

import java.util.HashMap;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.ui.widgets.MusicListAdapter;

public class UsageDetailListFragment extends Fragment implements View.OnClickListener {

	MusicListAdapter mAdapter = null;
	private OnDetachFromActivity mOnDetachListener = null;
	private HashMap<Long,UsageInfo> mInfoList = null;
	private String mCurrentPackageName = null;
	private String mTotalDuration = null;
	private ActionBar mActionBar;
	
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
	            menu.findItem(R.id.action_overflow).setVisible(false);
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
        
	    if (!Utils.isTabletDevice(getActivity())) {
	        initActionBar();
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
	
	private void initActionBar() {
        mActionBar = getActivity().getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(getActivity());

        View customView = mInflater.inflate(R.layout.custom_action_bar, null);
        
        LinearLayout actionView = (LinearLayout) customView.findViewById(R.id.action_title_view);
        ImageView imageView = (ImageView) actionView.findViewById(R.id.imageView1);
        imageView.setVisibility(View.VISIBLE);

        if (Utils.isAndroidLDevice(getActivity())) {
            actionView.setOnClickListener(this);
        } else {
            actionView.setBackground(null);
            imageView.setBackground(getResources().getDrawable(R.drawable.image_item_selector));
            imageView.setOnClickListener(this);
        }
        if (!Utils.isTabletDevice(getActivity().getApplicationContext())) {
            if (getActivity() != null && getActivity().getActionBar() != null) {
                View view = getActivity().getActionBar().getCustomView();
                if(view != null){
                    TextView mTitleTextView = (TextView) customView.findViewById(R.id.title_text);
                    mTitleTextView.setText(mCurrentPackageName);
                    mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                }
            }
        }


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) actionView.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        actionView.setPadding(0, 0, 0, 0);
        actionView.setLayoutParams(params);

        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.action_title_view: getActivity().onBackPressed();
                                     break;
        case R.id.imageView1 : getActivity().onBackPressed();
                               break;
        }
    }

}

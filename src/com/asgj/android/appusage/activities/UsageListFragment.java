/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asgj.android.appusage.activities;

import java.util.ArrayList;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.ui.widgets.MusicListAdapter;
import com.asgj.android.appusage.ui.widgets.SlidingTabLayout;
import com.asgj.android.appusage.ui.widgets.UsageListAdapter;

/**
 * A basic sample which shows how to use
 * {@link com.example.android.common.view.SlidingTabLayout} to display a custom
 * {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class UsageListFragment<AppData, MusicData> extends
		Fragment {

	static final String LOG_TAG = UsageListFragment.class.getSimpleName();

	/**
	 * A custom {@link ViewPager} title strip which looks much like Tabs present
	 * in Android v4.0 and above, but is designed to give continuous feedback to
	 * the user when scrolling.
	 */
	private SlidingTabLayout mSlidingTabLayout;

	private AppData mUsageAppData = null;

	private MusicData mMusicData = null;

	/**
	 * A {@link ViewPager} which will be used in conjunction with the
	 * {@link SlidingTabLayout} above.
	 */
	private ViewPager mViewPager;
	private SamplePagerAdapter mPageAdapter = null;

	private UsageListAdapter<AppData> mAppDataListAdapter = null;
	private MusicListAdapter mMusicDataListAdapter = null;
	private OnUsageItemClickListener mItemClickListener = null;
	
	public void setOnUsageItemClickListener(OnUsageItemClickListener listener){
		mItemClickListener = listener;
	}
	
	
	public interface OnUsageItemClickListener {
		public void onUsageItemClick(int tabIndex,int listItem);
	}

	/**
	 * Inflates the {@link View} which will be displayed by this
	 * {@link Fragment}, from the app's resources.
	 */

	public void setmUsageAppData(AppData mUsageAppData) {
		this.mUsageAppData = mUsageAppData;
		try {
			mAppDataListAdapter = new UsageListAdapter<AppData>(getActivity(), mUsageAppData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPageAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("unchecked")
    public void setmMusicData(MusicData mMusicData) {
		this.mMusicData = mMusicData;
		try {
			mMusicDataListAdapter = new MusicListAdapter((ArrayList<UsageInfo>)this.mMusicData,getActivity());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPageAdapter.notifyDataSetChanged();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.usage_fragment_layout, container,
				false);
	}

	// BEGIN_INCLUDE (fragment_onviewcreated)
	/**
	 * This is called after the
	 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
	 * Here we can pick out the {@link View}s we need to configure from the
	 * content view.
	 *
	 * We set the {@link ViewPager}'s adapter to be an instance of
	 * {@link SamplePagerAdapter}. The {@link SlidingTabLayout} is then given
	 * the {@link ViewPager} so that it can populate itself.
	 *
	 * @param view
	 *            View created in
	 *            {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// BEGIN_INCLUDE (setup_viewpager)
		// Get the ViewPager and set it's PagerAdapter so that it can display
		// items
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mPageAdapter = new SamplePagerAdapter();
		mViewPager.setAdapter(mPageAdapter);
		// END_INCLUDE (setup_viewpager)

		// BEGIN_INCLUDE (setup_slidingtablayout)
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the
		// ViewPager has had
		// it's PagerAdapter set.
		mSlidingTabLayout = (SlidingTabLayout) view
				.findViewById(R.id.sliding_tabs);
		mSlidingTabLayout.setViewPager(mViewPager);
		// END_INCLUDE (setup_slidingtablayout)
	}

	// END_INCLUDE (fragment_onviewcreated)

	/**
	 * The {@link android.support.v4.view.PagerAdapter} used to display pages in
	 * this sample. The individual pages are simple and just display two lines
	 * of text. The important section of this class is the
	 * {@link #getPageTitle(int)} method which controls what is displayed in the
	 * {@link SlidingTabLayout}.
	 */
	class SamplePagerAdapter extends PagerAdapter implements OnItemClickListener , OnChildClickListener{

		String[] mList = new String[] { "Apps", "Media", "Call" };

		/**
		 * @return the number of pages to display
		 */
		@Override
		public int getCount() {
			return mList.length;
		}
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		/**
		 * @return true if the value returned from
		 *         {@link #instantiateItem(ViewGroup, int)} is the same object
		 *         as the {@link View} added to the {@link ViewPager}.
		 */
		@Override
		public boolean isViewFromObject(View view, Object o) {
			return o == view;
		}

		// BEGIN_INCLUDE (pageradapter_getpagetitle)
		/**
		 * Return the title of the item at {@code position}. This is important
		 * as what this method returns is what is displayed in the
		 * {@link SlidingTabLayout}.
		 * <p>
		 * Here we construct one using the position value, but for real
		 * application the title should refer to the item's contents.
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			return mList[position];
		}

		// END_INCLUDE (pageradapter_getpagetitle)

		/**
		 * Instantiate the {@link View} which should be displayed at
		 * {@code position}. Here we inflate a layout from the apps resources
		 * and then change the text view to signify the position.
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// Inflate a new layout from our resources
		    View returnView = null;
			View viewData = getActivity().getLayoutInflater().inflate(
                    R.layout.usage_list, container, false);
			View viewNoData = getActivity().getLayoutInflater().inflate(R.layout.layout_no_data_tracking_info, container, false);
			TextView textViewNoDataStartTracking = (TextView) viewNoData.findViewById(R.id.textView_start_tracking_no_data_navigate);
			TextView textViewNoData = (TextView) viewNoData.findViewById(R.id.textView_no_data_navigate);
			// Add the newly created View to the ViewPager

			// Retrieve a TextView from the inflated View, and update it's text
			ListView title = (ListView) viewData.findViewById(R.id.usage_list);
			ExpandableListView musicListView = (ExpandableListView)viewData.findViewById(R.id.music_list);
			if (position == 0 && mAppDataListAdapter != null){
				title.setVisibility(View.VISIBLE);
				musicListView.setVisibility(View.GONE);
			    textViewNoData.setVisibility(View.GONE);
                textViewNoDataStartTracking.setVisibility(View.GONE);
				title.setAdapter(mAppDataListAdapter);
				mAppDataListAdapter.notifyDataSetChanged();
			    if (mAppDataListAdapter.isEmpty()) {
			        if (UsageSharedPrefernceHelper.isServiceRunning(getActivity())) {
			            textViewNoData.setVisibility(View.VISIBLE);
			        } else {
			            textViewNoDataStartTracking.setVisibility(View.VISIBLE);
			        }
			        container.addView(viewNoData);
			        returnView = viewNoData;
			    } else {
			        textViewNoData.setVisibility(View.GONE);
			        textViewNoDataStartTracking.setVisibility(View.GONE);
                    
			        container.addView(viewData);
			        returnView = viewData;
			    }
			}
			else if (position == 1 && mMusicDataListAdapter != null){
				title.setVisibility(View.GONE);
				musicListView.setChildDivider(null);
				musicListView.setDivider(null);
				musicListView.setDividerHeight(0);
				musicListView.setVisibility(View.VISIBLE);
				musicListView.setAdapter(mMusicDataListAdapter);
				if (mMusicDataListAdapter.isEmpty()) {
			        if (UsageSharedPrefernceHelper.isServiceRunning(getActivity())) {
			            textViewNoData.setVisibility(View.VISIBLE);
			        } else {
			            textViewNoDataStartTracking.setVisibility(View.VISIBLE);
			        }
			        container.addView(viewNoData);
			        returnView = viewNoData;
			    } else {
			        textViewNoData.setVisibility(View.GONE);
			        textViewNoDataStartTracking.setVisibility(View.GONE);
                    
			        container.addView(viewData);
			        returnView = viewData;
			    }
			}
                    container.addView(viewData);
                    returnView = viewData;

			
			title.setTag(position);
			title.setOnItemClickListener(this);
			musicListView.setOnChildClickListener(this);
			musicListView.setTag(position);
			Log.i(LOG_TAG, "instantiateItem() [position: " + position + "]");

			// Return the View
			return returnView;
		}

		/**
		 * Destroy the item from the {@link ViewPager}. In our case this is
		 * simply removing the {@link View}.
		 */
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
			Log.i(LOG_TAG, "destroyItem() [position: " + position + "]");
		}
		@Override
		public void onItemClick(AdapterView<?> parentView, View v, int position,
				long id) {
			if(mItemClickListener != null){
				mItemClickListener.onUsageItemClick((Integer)parentView.getTag(), position);
			}
			
		}
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			if(mItemClickListener != null){
				mItemClickListener.onUsageItemClick(groupPosition, childPosition);
			}
			return false;
		}

	}
}

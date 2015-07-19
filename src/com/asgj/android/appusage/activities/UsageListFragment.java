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
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.database.PhoneUsageDatabase;
import com.asgj.android.appusage.ui.widgets.MusicListAdapter;
import com.asgj.android.appusage.ui.widgets.SlidingTabLayout;
import com.asgj.android.appusage.ui.widgets.UsageListAdapter;
import com.asgj.android.appusage.ui.widgets.listview.SwipeListView;
import com.asgj.android.appusage.ui.widgets.listview.SwipeListViewTouchListener.OnItemSwiped;

/**
 * A basic sample which shows how to use
 * {@link com.example.android.common.view.SlidingTabLayout} to display a custom
 * {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class UsageListFragment<AppData, MusicData> extends
		Fragment implements ViewPager.OnPageChangeListener {

	static final String LOG_TAG = UsageListFragment.class.getSimpleName();
	
	private boolean mIsFilteredMap = false;

    HashMap<String, Long> mFilteredMap;
    
    private ActionBar mActionBar;

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present
     * in Android v4.0 and above, but is designed to give continuous feedback to
     * the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    public HashMap<String, String> mLabelMap;

    private PhoneUsageDatabase mDatabase;

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
	private UsageDetailListFragment mDetailFragment = null;
	private Calendar cal2;
	private Calendar cal1;
	private String mAlertPackage = null;
	public void setOnUsageItemClickListener(OnUsageItemClickListener listener){
		mItemClickListener = listener;
	}
	
	
	public interface OnUsageItemClickListener {
		public void onUsageItemClick(String pkg,int position);
		public void onUsageItemSwiped(String pkg,int position);
		public void onMusicItemClick(String pkg,int groupPosition,int childPosition);
	}

	/**
	 * Inflates the {@link View} which will be displayed by this
	 * {@link Fragment}, from the app's resources.
	 */

	@SuppressWarnings("unchecked")
    public void setmUsageAppData(AppData mUsageAppData) {

        this.mUsageAppData = mUsageAppData;
        Log.v("gaurav", "setmusageAppData call");
        if (mIsFilteredMap) {
            HashMap<String, Long> usageMap = (HashMap<String, Long>) ((HashMap<String, Long>) mUsageAppData)
                    .clone();
            Set<String> filterPkg = UsageSharedPrefernceHelper
                    .getSelectedApplicationForFiltering(getActivity());
            mFilteredMap = new HashMap<>();

            Iterator<String> iterator = filterPkg.iterator();

            while (iterator.hasNext()) {
                // Search for each application name in mLabelMap to get it's
                // package name.
                String pkgName = iterator.next();

                if (pkgName != null && usageMap.containsKey(pkgName)) {
                    mFilteredMap.put(pkgName, usageMap.get(pkgName));
                }
            }
            Log.v("gaurav", "Set to filter map through onResume");

        }
        try {
            if (!mIsFilteredMap)
                mAppDataListAdapter = new UsageListAdapter<AppData>(getActivity(), mUsageAppData);
            else
                mAppDataListAdapter = new UsageListAdapter<AppData>(getActivity(),
                        (AppData) mFilteredMap);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mPageAdapter.notifyDataSetChanged();
        if (mUsageAppData != null && !((HashMap<String, Long>) mUsageAppData).isEmpty()) {
            HashMap<String, Long> tempMap = (HashMap<String, Long>) mUsageAppData;
            mLabelMap = new HashMap<>();
            
            for (Map.Entry<String, Long> entry : tempMap.entrySet()) {
                mLabelMap.put(entry.getKey(), Utils.getApplicationLabelName(getActivity(), entry.getKey()));
            }
        }
	}

	@SuppressWarnings("unchecked")
    public void setmMusicData(MusicData mMusicData,long totalDuration) {
		this.mMusicData = mMusicData;
		try {
			mMusicDataListAdapter = new MusicListAdapter((ArrayList<UsageInfo>)this.mMusicData,getActivity());
			mMusicDataListAdapter.setPackageNameAndDuration(null, Utils.getTimeFromSeconds(totalDuration));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPageAdapter.notifyDataSetChanged();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getActivity() != null && UsageSharedPrefernceHelper.isFilterMode(getActivity())) {
            mIsFilteredMap = true;
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAlertPackage = bundle.getString("package");
        }
        super.onCreate(savedInstanceState);
    }

    public int getViewPagerPage() {
        return mViewPager.getCurrentItem();
    }
    private void initActionBar() {

        mActionBar = getActivity().getActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(getActivity());

        View customView = mInflater.inflate(R.layout.custom_action_bar, null);
        LinearLayout layout = (LinearLayout) customView.findViewById(R.id.action_title_view);
        TextView mTitleTextView = (TextView) customView.findViewById(R.id.title_text);
        mTitleTextView.setTextColor(getResources().getColor(android.R.color.white));
        mTitleTextView.setText(getString(R.string.app_name));
        layout.setBackground(null);

        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);
    
    }
    
    public void setStartEndCalForCustomInterval(Calendar cal1,Calendar cal2){
    	this.cal1 = cal1;
    	this.cal2 = cal2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mDatabase = new PhoneUsageDatabase(getActivity());
            getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.app_name));
            initActionBar();

        return inflater.inflate(R.layout.usage_fragment_layout, container, false);
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
		mSlidingTabLayout.setOnPageChangeListener(this);
		// END_INCLUDE (setup_slidingtablayout)
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    // TODO Auto-generated method stub
	    inflater.inflate(R.menu.list_fragment_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuItem filterMenu = (MenuItem) menu.findItem(R.id.filter_menu);
        if (UsageSharedPrefernceHelper.isFilterMode(getActivity())) {
            filterMenu.setTitle(R.string.string_filter);
        } else {
            filterMenu.setTitle(R.string.string_all);
        }

        MenuItem menuItemFilter = (MenuItem) menu.findItem(R.id.filter_menu);

        if (mViewPager != null) {
            switch (mViewPager.getCurrentItem()) {
            case 0:
                if (mAppDataListAdapter != null && !mAppDataListAdapter.isEmpty()) {
                    menuItemFilter.setVisible(true);
                }
                break;
            case 1: // menuItemSortBy.setVisible(false);
                menuItemFilter.setVisible(false);
                break;
            default:
                break;
            }
        }

        super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // TODO Auto-generated method stub
	    switch (item.getItemId()) {
	    case R.id.filter_menu:
			if (UsageSharedPrefernceHelper.isFilterMode(getActivity())) {
				UsageSharedPrefernceHelper.setFilterMode(getActivity(), false);
				item.setTitle(R.string.string_all);
                mIsFilteredMap = false;
                setmUsageAppData(mUsageAppData);
            } else {
                if (UsageSharedPrefernceHelper.getSelectedApplicationForFiltering(getActivity()) == null
                        || UsageSharedPrefernceHelper.getSelectedApplicationForFiltering(
                                getActivity()).size() == 0) {
                    Toast.makeText(getActivity(), R.string.string_filter_menu_enable_message,
                            Toast.LENGTH_LONG).show();
                } else {
                    UsageSharedPrefernceHelper.setFilterMode(getActivity(), true);
                    item.setTitle(R.string.string_filter);
                    mIsFilteredMap = true;
                    setmUsageAppData(mUsageAppData);
                }
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    // END_INCLUDE (fragment_onviewcreated)

	/**
	 * The {@link android.support.v4.view.PagerAdapter} used to display pages in
	 * this sample. The individual pages are simple and just display two lines
	 * of text. The important section of this class is the
	 * {@link #getPageTitle(int)} method which controls what is displayed in the
	 * {@link SlidingTabLayout}.
	 */
	class SamplePagerAdapter extends PagerAdapter implements OnItemSwiped,OnItemClickListener, OnChildClickListener,Comparator<Map.Entry<Long, UsageInfo>>{

		String[] mList = new String[] { "Apps", "Media" };

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
		private void updateDetailFragmentForTablet(String pkg, int position) {
	        // Check current preference first.
	    	HashMap<Long,UsageInfo> infoMap = null;
	        // Check whether custom and end day not today.
	        if (UsageSharedPrefernceHelper.getShowByType(getActivity()).equals(
	                getActivity().getString(R.string.string_Custom))
	                && Utils.compareDates(cal2, Calendar.getInstance()) != 0) {

	            infoMap = mDatabase.getAppIntervalsBetweenDates(pkg, cal1, cal2);
	        } else {

	            switch (UsageSharedPrefernceHelper.getShowByType(getActivity())) {
	            case "Today":
	                infoMap = mDatabase.getAppIntervalsBetweenDates(pkg, Calendar.getInstance(),
	                        Calendar.getInstance());
	                break;
	            case "Weekly":
	            case "Monthly":
	            case "Yearly":
	                infoMap = mDatabase.getAppIntervalsBetweenDates(pkg,
	                        UsageSharedPrefernceHelper.getCalendarByShowType(getActivity()),
	                        Calendar.getInstance());
	        	
	                break;
	            case "Custom":
	                infoMap = mDatabase.getAppIntervalsBetweenDates(pkg,
	                        cal1, Calendar.getInstance());
	                break;
	            default:
	                break;
	            }
	        }
	        
	        LinkedHashMap<Long, UsageInfo> linkedMap = Utils.sortMapByKey(infoMap,this);
	        initDetailFragment(linkedMap, Utils.getApplicationLabelName(getActivity(), pkg));
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
		private void initDetailFragment(HashMap<Long,UsageInfo> intervalMap, String applicationName) {
	        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
	        
	        LinkedHashMap<Long, UsageInfo> linkedMap = null;
	        // First sort map by key (start duration).
	        if (intervalMap != null && !intervalMap.isEmpty()) {
	             linkedMap = Utils.sortMapByKey(intervalMap,this);
	        }
	        
	        mDetailFragment = new UsageDetailListFragment(linkedMap);
	        transaction.replace(R.id.usage_detail_fragment_layout, mDetailFragment);
	        transaction.commit();
	    }
		
		@Override
	    public int compare(Entry<Long, UsageInfo> lhs, Entry<Long, UsageInfo> rhs) {
	        // TODO Auto-generated method stub
	        return (int) (rhs.getKey() - lhs.getKey());
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
		    View viewData = null;
		    FrameLayout mDetailFragmentLayout = null;
		    LinearLayout mAppListLayout = null;
		    LinearLayout mMusicListLayout = null;
		    if(!Utils.isTabletDevice(getActivity())){
			viewData = getActivity().getLayoutInflater().inflate(
                    R.layout.usage_list, container, false);
		    }else{
		    	viewData = getActivity().getLayoutInflater().inflate(
	                    R.layout.usage_list_tablet_layout, container, false);
		    	mDetailFragmentLayout = (FrameLayout)viewData.findViewById(R.id.usage_detail_fragment_layout);
		    	mAppListLayout = (LinearLayout)viewData.findViewById(R.id.usage_parent_tab_layout);
		    	mMusicListLayout = (LinearLayout)viewData.findViewById(R.id.music_parent_tab_layout);
		    	
		    }
			View viewNoData = getActivity().getLayoutInflater().inflate(R.layout.layout_no_data_tracking_info, container, false);
			TextView textViewNoDataStartTracking = (TextView) viewNoData.findViewById(R.id.textView_start_tracking_no_data_navigate);
			TextView textViewNoData = (TextView) viewNoData.findViewById(R.id.textView_no_data_navigate);
			// Add the newly created View to the ViewPager

			// Retrieve a TextView from the inflated View, and update it's text
			SwipeListView title = (SwipeListView)viewData.findViewById(R.id.usage_list);
			ExpandableListView musicListView = (ExpandableListView)viewData.findViewById(R.id.music_list);
			if(!Utils.isAndroidLDevice(getActivity())){
				title.setSelector(getActivity().getResources().getDrawable(R.drawable.list_item_selector));
			}	
			if (position == 0 && mAppDataListAdapter != null){
				if(Utils.isTabletDevice(getActivity())){
					mMusicListLayout.setVisibility(View.GONE);
					mAppListLayout.setVisibility(View.VISIBLE);
				}
				title.setVisibility(View.VISIBLE);
				musicListView.setVisibility(View.GONE);
			    textViewNoData.setVisibility(View.GONE);
                textViewNoDataStartTracking.setVisibility(View.GONE);
//                mAppDataListAdapter.setOnItemTouchListener(this);
                title.setOnItemClickListener(this);
                title.setOnItemSwipeListener(this);
				title.setAdapter(mAppDataListAdapter);
				mAppDataListAdapter.notifyDataSetChanged();
			    if (mAppDataListAdapter.isEmpty()) {
			        textViewNoData.setText(getString(R.string.string_no_data_navigate_apps));
			        if (UsageSharedPrefernceHelper.isFilterMode(getActivity())) {
			            textViewNoData.setText(getString(R.string.string_no_data_available_filtered_apps));
			        }
			        if (UsageSharedPrefernceHelper.isServiceRunning(getActivity())) {
			            textViewNoData.setVisibility(View.VISIBLE);
			        } else {
			            Calendar endCalendar;
	                    endCalendar = Calendar.getInstance();
	                    endCalendar.setTimeInMillis(UsageSharedPrefernceHelper.getCalendar(getActivity(), "endCalendar"));
	                    
	                    if (UsageSharedPrefernceHelper.getShowByType(getActivity()).equals(getString(R.string.string_Custom)) && 
                        Utils.compareDates(Calendar.getInstance(), endCalendar) > 0) {
	                        textViewNoData.setVisibility(View.VISIBLE);
	                    } else {
	                        textViewNoDataStartTracking.setText(getString(R.string.string_start_tracking_no_data_navigate_apps));
	                        
	                        if (UsageSharedPrefernceHelper.isFilterMode(getActivity())) {
	                            textViewNoDataStartTracking.setText(getString(R.string.string_no_data_available_filtered_apps));
	                        }
	                        textViewNoDataStartTracking.setVisibility(View.VISIBLE);
	                    }
			            
			        }
			        container.addView(viewNoData);
			        returnView = viewNoData;
			    } else {
			        if (mAlertPackage != null) {
			            if (mAppDataListAdapter.getPackageNameKeys().contains(mAlertPackage)) {
			                
			                if (Utils.isTabletDevice(getActivity())) {
			                    mAppDataListAdapter.setClickedItem(mAppDataListAdapter.getPackageNameKeys().indexOf(mAlertPackage));
	                            mAppDataListAdapter.notifyDataSetChanged();
			                    updateDetailFragmentForTablet(mAlertPackage, mAppDataListAdapter.getPackageNameKeys().indexOf(mAlertPackage));
			                } else {
			                    if (mItemClickListener != null) {
			                        mItemClickListener.onUsageItemClick(mAlertPackage, mAppDataListAdapter.getPackageNameKeys().indexOf(mAlertPackage));
			                    }
			                }
			                mAlertPackage = null;
			            }
			        }
			        textViewNoData.setVisibility(View.GONE);
			        textViewNoDataStartTracking.setVisibility(View.GONE);
                    
			        container.addView(viewData);
			        returnView = viewData;
			    }
			}
			else if (position == 1 && mMusicDataListAdapter != null){
				if(Utils.isTabletDevice(getActivity())){
					mMusicListLayout.setVisibility(View.VISIBLE);
					mAppListLayout.setVisibility(View.GONE);
				}
				title.setVisibility(View.GONE);
				musicListView.setChildDivider(null);
				musicListView.setDivider(null);
				musicListView.setDividerHeight(0);
				musicListView.setGroupIndicator(null);
				musicListView.setVisibility(View.VISIBLE);
				musicListView.setAdapter(mMusicDataListAdapter);
				if (mMusicDataListAdapter.getGroupCount() == 1) {
				    textViewNoData.setText(getString(R.string.string_no_data_music));
			        if (UsageSharedPrefernceHelper.isServiceRunning(getActivity())) {
			            textViewNoData.setVisibility(View.VISIBLE);
			        } else {

			            Calendar endCalendar;
                        endCalendar = Calendar.getInstance();
                        endCalendar.setTimeInMillis(UsageSharedPrefernceHelper.getCalendar(getActivity(), "endCalendar"));
                        
                        if (UsageSharedPrefernceHelper.getShowByType(getActivity()).equals(getString(R.string.string_Custom)) && 
                        Utils.compareDates(Calendar.getInstance(), endCalendar) > 0) {
                            textViewNoData.setVisibility(View.VISIBLE);
                        } else {
                            textViewNoDataStartTracking.setText(getString(R.string.string_start_tracking_no_data_music));
                            textViewNoDataStartTracking.setVisibility(View.VISIBLE);
                        }
                        
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

			title.setTag(position);
			
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
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			if(mItemClickListener != null){
				mItemClickListener.onMusicItemClick("a", groupPosition, childPosition);
			}
			return false;
		}
		@Override
		public void onItemSwiped(int position) {
			if(mItemClickListener != null){
				mItemClickListener.onUsageItemSwiped(mAppDataListAdapter.getPackageNameKeys().get(position), position);
			}
			
		}
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if(!Utils.isTabletDevice(getActivity()) && mItemClickListener != null){
				mItemClickListener.onUsageItemClick(mAppDataListAdapter.getPackageNameKeys().get(position), position);
			}else{
				mAppDataListAdapter.setCurrentSelectedPos(position);
				mAppDataListAdapter.notifyDataSetChanged();
				updateDetailFragmentForTablet(mAppDataListAdapter.getPackageNameKeys().get(position), position);
			}
			
		}

	}

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
       Log.d("anurag","page scrolled.. "+ arg0 + " "+ arg1 + " "+ arg2);
    }

    @Override
    public void onPageSelected(int arg0) {
    	 Log.d("anurag","page selected.. "+ arg0);
        getActivity().invalidateOptionsMenu();
    }
}

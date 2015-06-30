package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.Utils;

public class MusicListAdapter implements ExpandableListAdapter {

	private HashMap<String, ArrayList<UsageInfo>> mIntervalMap = null;
	private Context mContext = null;
	private ArrayList<String> mGroupList = null;
	
	private void prepareDataFromMap(HashMap<Long,UsageInfo> infoMap) {
		mIntervalMap = new HashMap<String, ArrayList<UsageInfo>>();
		mGroupList = new ArrayList<String>();
		
        if (infoMap != null && !infoMap.isEmpty()) {
            for (UsageInfo info : infoMap.values()) {
                String date = Utils.getDateForDisplay(info
                        .getmIntervalStartTime());
                if (mGroupList.contains(date)) {
                    mIntervalMap.get(date).add(info);
                } else {
                    ArrayList<UsageInfo> list = new ArrayList<UsageInfo>();
                    list.add(info);
                    mIntervalMap.put(date, list);
                    mGroupList.add(date);
                }
            }
        }
	}

	private void prepareDataFromList(ArrayList<UsageInfo> list) {
		mIntervalMap = new HashMap<String, ArrayList<UsageInfo>>();
		mGroupList = new ArrayList<String>();
		
        if (list != null && !list.isEmpty()) {
            for (UsageInfo info : list) {
                String date = Utils.getDateForDisplay(info
                        .getmIntervalStartTime());
                if (mGroupList.contains(date)) {
                    mIntervalMap.get(date).add(info);
                } else {
                    ArrayList<UsageInfo> infoList = new ArrayList<UsageInfo>();
                    infoList.add(info);
                    mIntervalMap.put(date, infoList);
                    mGroupList.add(date);
                }
            }
        }
	}

	public MusicListAdapter(ArrayList<UsageInfo> list, Context context) {
		mContext = context;
		prepareDataFromList(list);
	}
	
	public MusicListAdapter(HashMap<Long,UsageInfo> map, Context context) {
		mContext = context;
		prepareDataFromMap(map);
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater li = LayoutInflater.from(mContext);
			convertView = li.inflate(R.layout.usage_list_item, null);
		}
		TextView text_left = (TextView) convertView.findViewById(R.id.text1);
		TextView text_middle = (TextView) convertView.findViewById(R.id.text2);
		TextView text_right = (TextView) convertView.findViewById(R.id.text3);
		ImageView image_view_app_icon = (ImageView) convertView
				.findViewById(R.id.app_icon);

		if (mIntervalMap != null) {
			image_view_app_icon.setVisibility(View.GONE);
			text_middle.setVisibility(View.VISIBLE);
			UsageInfo info = mIntervalMap.get(mGroupList.get(groupPosition)).get(
					childPosition);
			text_left.setText(""
					+ Utils.getTimeFromTimeStamp(mContext,
							info.getmIntervalStartTime()) + "-");
			text_middle.setText(""
					+ Utils.getTimeFromTimeStamp(mContext,
							info.getmIntervalEndTime()));
			text_right.setText(""
					+ Utils.getTimeFromSeconds(info.getmIntervalDuration()));
			if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				text_left.setTextAppearance(mContext, android.R.style.TextAppearance_DeviceDefault_Medium);
				text_middle.setTextAppearance(mContext, android.R.style.TextAppearance_DeviceDefault_Medium);
				text_right.setTextAppearance(mContext, android.R.style.TextAppearance_DeviceDefault_Medium);
			}else{
				text_left.setTextAppearance(mContext, android.R.style.TextAppearance_DeviceDefault_Small);
				text_middle.setTextAppearance(mContext, android.R.style.TextAppearance_DeviceDefault_Small);
				text_right.setTextAppearance(mContext, android.R.style.TextAppearance_DeviceDefault_Small);
			}
			// text_left.setTypeface(mNormalTypeface);
			// text_right.setTypeface(mNormalTypeface);
			text_left.setTextColor(mContext.getResources().getColor(
					android.R.color.black));
			text_middle.setTextColor(mContext.getResources().getColor(android.R.color.black));
			text_right.setTextColor(mContext.getResources().getColor(
					android.R.color.black));

		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mIntervalMap.get(mGroupList.get(groupPosition)).size();
	}

	@Override
	public long getCombinedChildId(long groupId, long childId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCombinedGroupId(long groupId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mGroupList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater li = LayoutInflater.from(mContext);
			convertView = li.inflate(
					R.layout.group_item_layout, null);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.drop_icon);
			Utils.getScaledImageView(mContext, imageView);
		}
		TextView textview = (TextView) convertView.findViewById(R.id.group_title);
		String date = mGroupList.get(groupPosition);
		if (Utils.isDateToday(date)) {
			textview.setText(R.string.string_Today);
		} else {
			textview.setText(date);
		}
		ImageView imageView = (ImageView) convertView.findViewById(R.id.drop_icon);
		
		
		if(isExpanded && (imageView.getTag() != null && (!(Boolean)imageView.getTag()))){
			Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_to_bottom);
			imageView.setTag(true);
			imageView.setAnimation(anim);
			imageView.animate();
		}else if(!isExpanded && (imageView.getTag() == null || (Boolean)imageView.getTag())){
			Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.anim_bottom_to_right);
			imageView.setAnimation(anim);
			imageView.setTag(false);
			imageView.animate();
			
		}
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

}

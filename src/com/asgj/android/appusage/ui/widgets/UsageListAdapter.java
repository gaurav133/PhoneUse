package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;

public class UsageListAdapter<Data> extends BaseAdapter {

	Context mContext = null;
	Data mData = null;
	ArrayList<UsageInfo> mList = null;
	HashMap<String, Long> mMap = null;
	ArrayList<String> keys;

	public UsageListAdapter(Context context, Data data) throws Exception {
		mContext = context;
		mData = data;
		keys = new ArrayList<>();
		if (mData instanceof ArrayList) {
			mList = (ArrayList) mData;
		} else if (mData instanceof HashMap) {
			mMap = (HashMap<String, Long>) mData;
			for(String s : mMap.keySet()){
				keys.add(s);
			}

		} else {
			throw new Exception("data should be either arraylist or hashmap");
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mList != null) {
			return mList.size();
		} else if (mMap != null) {
			return keys.size();
		} else {
			return 0;
		}

	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.usage_list_item, null);
		}

		TextView text_left = (TextView) convertView.findViewById(R.id.text1);
		TextView text_middle = (TextView) convertView.findViewById(R.id.text2);
		TextView text_right = (TextView) convertView.findViewById(R.id.text3);

		if (mList != null) {
			text_left.setText("" + mList.get(position).getmIntervalStartTime());
			text_middle.setText("" + mList.get(position).getmIntervalEndTime());
			text_right.setText("" + mList.get(position).getmIntervalDuration());
		} else if (mMap != null) {
			text_left.setText(keys.get(position));
			text_right.setText("" + mMap.get(keys.get(position)));
			text_middle.setVisibility(View.GONE);
		}

		return convertView;
	}
}

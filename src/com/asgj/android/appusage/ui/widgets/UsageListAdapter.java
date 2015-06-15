package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.HttpImageLoader;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.Utils;

public class UsageListAdapter<Data> extends BaseAdapter {

    public static final String mTotalTimeKey = "totalTime";
    private int index = 0;
    Context mContext = null;
    Data mData = null;
    ArrayList<UsageInfo> mList = null;
    HashMap<String, Long> mMap = null;
    ArrayList<String> mKeys;
    HttpImageLoader mImageLoader = null;
    Typeface mNormalTypeface, mBoldTypeface;

    public UsageListAdapter(Context context, Data data) throws Exception {
        mContext = context;
        mData = data;
        mImageLoader = HttpImageLoader.getInstance(context);
        mKeys = new ArrayList<>();
        
        if (mData instanceof ArrayList) {
            if (!((ArrayList) mData).isEmpty()) {
                UsageInfo info = new UsageInfo();
                info.setmIntervalDuration(Utils.calculateListSum((ArrayList) mData));
                
                mList = new ArrayList<>();
                mList.add(index, info);

                mList.addAll((ArrayList<UsageInfo>) ((ArrayList) mData).clone());
            }
        } else if (mData instanceof HashMap) {
            mMap = (HashMap<String, Long>) ((HashMap<String, Long>) mData).clone();

            for (String s : mMap.keySet()) {
                mKeys.add(s);
            }

            if (!mMap.isEmpty()) {
                mKeys.add(index, mTotalTimeKey);
                mMap.put(mTotalTimeKey, Utils.calculateMapSum(mMap));
            }
        } else {
            throw new Exception("data should be either arraylist or hashmap");
        }

        mNormalTypeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
        mBoldTypeface = Typeface.create("sans-serif", Typeface.BOLD);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (mList != null) {
            return mList.size();
        } else if (mMap != null) {
            return mKeys.size();
        } else {
            return 0;
        }
    }
    
    public ArrayList<String> getPackageNameKeys() {
        return mKeys;
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
        ImageView image_view_app_icon = (ImageView) convertView.findViewById(R.id.app_icon);

        if (mList != null) {
            image_view_app_icon.setVisibility(View.GONE);
            if (position == 0) {
                text_middle.setVisibility(View.GONE);
                text_left.setText(mContext.getString(R.string.string_total_time_apps).toUpperCase());
                text_left.setTextColor(mContext.getResources().getColor(
                        R.color.color_total_time_title));
                text_right.setTextColor(mContext.getResources().getColor(
                        R.color.color_total_time_title));
                text_left.setTypeface(mBoldTypeface);
                text_right.setText("" + Utils.getTimeFromSeconds(mList.get(position).getmIntervalDuration()).toUpperCase());
                text_right.setTypeface(mBoldTypeface);
            } else {
                text_middle.setVisibility(View.VISIBLE);
                text_left.setText(""
                        + Utils.getTimeFromTimeStamp(mContext, mList.get(position)
                                .getmIntervalStartTime()) + "-");
                text_middle.setText(""
                        + Utils.getTimeFromTimeStamp(mContext, mList.get(position)
                                .getmIntervalEndTime()));
                text_right.setText(""
                        + Utils.getTimeFromSeconds(mList.get(position).getmIntervalDuration()));
                text_left.setTypeface(mNormalTypeface);
                text_right.setTypeface(mNormalTypeface);
                text_left.setTextColor(mContext.getResources().getColor(android.R.color.black));
                text_right.setTextColor(mContext.getResources().getColor(android.R.color.black));
                
            }
        }  
        
        if (mMap != null) {
            if (position == 0) {
                image_view_app_icon.setVisibility(View.GONE);
                text_left.setTextColor(mContext.getResources().getColor(
                        R.color.color_total_time_title));
                text_left.setTypeface(mBoldTypeface);
                text_left.setText(mContext.getString(R.string.string_total_time_apps).toUpperCase());
                text_right.setText(("" + Utils.getTimeFromSeconds(mMap.get(mKeys.get(position))))
                        .toUpperCase());
                text_right.setTextColor(mContext.getResources().getColor(
                        R.color.color_total_time_title));
                text_right.setTypeface(mBoldTypeface);
                
            } else {
                mImageLoader.display(mKeys.get(position), image_view_app_icon,
                        R.drawable.ic_launcher);
                text_left.setTextColor(mContext.getResources().getColor(android.R.color.black));
                text_left.setText(Utils.getApplicationLabelName(mContext, mKeys.get(position)));
                text_left.setTypeface(mNormalTypeface);
                text_right.setText("" + Utils.getTimeFromSeconds(mMap.get(mKeys.get(position))));
                text_right.setTextColor(mContext.getResources().getColor(android.R.color.black));
                text_right.setTypeface(mNormalTypeface);
                image_view_app_icon.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }
}

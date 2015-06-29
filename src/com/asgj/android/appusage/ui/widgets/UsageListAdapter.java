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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.HttpImageLoader;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.Utils;

public class UsageListAdapter<Data> extends BaseAdapter {

    private static final String LOG_TAG = UsageListAdapter.class.getSimpleName();
    public static final String mTotalTimeKey = "totalTime";
    private int index = 0;
    Context mContext = null;
    Data mData = null;
    ArrayList<UsageInfo> mList = null;
    HashMap<String, Long> mMap = null;
    ArrayList<String> mKeys;
    HttpImageLoader mImageLoader = null;
    Typeface mNormalTypeface, mBoldTypeface;

    @SuppressWarnings("unchecked")
    public UsageListAdapter(Context context, Data data) throws Exception {
        mContext = context;
        mData = data;
        mImageLoader = HttpImageLoader.getInstance(context);
        mKeys = new ArrayList<>();
        
       if (mData instanceof HashMap) {
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
        if (mMap != null) {
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
        
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.usage_list_item, null);

            holder = new ViewHolder();
            holder.image_view_app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.text_dash = (TextView) convertView.findViewById(R.id.textView_dash);
            holder.text_left = (TextView) convertView.findViewById(R.id.text1);
            holder.text_right = (TextView) convertView.findViewById(R.id.text3);
            holder.text_middle = (TextView) convertView.findViewById(R.id.text2);
            holder.parent = (RelativeLayout) convertView.findViewById(R.id.parentLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        
        if (mMap != null) {
            holder.text_dash.setVisibility(View.GONE);
            if (position == 0) {
                holder.image_view_app_icon.setVisibility(View.GONE);
                holder.text_left.setTextColor(mContext.getResources().getColor(
                        R.color.color_total_time_title));
                holder.text_left.setTypeface(mBoldTypeface);
                holder.text_left.setText(mContext.getString(R.string.string_total_time_apps).toUpperCase());
                holder.text_right.setText(("" + Utils.getTimeFromSeconds(mMap.get(mKeys.get(position))))
                        .toUpperCase());
                holder.text_right.setTextColor(mContext.getResources().getColor(
                        R.color.color_total_time_title));
                holder.text_right.setTypeface(mBoldTypeface);
                holder.parent.setFocusable(true);
                holder.parent.setClickable(true);
                
            } else {
                holder.image_view_app_icon.setVisibility(View.VISIBLE);
                mImageLoader.display(mKeys.get(position), holder.image_view_app_icon,
                        R.drawable.ic_launcher);
                holder.text_left.setTextColor(mContext.getResources().getColor(android.R.color.black));
                holder.text_left.setText(Utils.getApplicationLabelName(mContext, mKeys.get(position)));
                holder.text_left.setTypeface(mNormalTypeface);
                holder.text_right.setText("" + Utils.getTimeFromSeconds(mMap.get(mKeys.get(position))));
                holder.text_right.setTextColor(mContext.getResources().getColor(android.R.color.black));
                holder.text_right.setTypeface(mNormalTypeface);
                holder.parent.setFocusable(false);
                holder.parent.setClickable(false);
            }
        }
        return convertView;
    }
    
    private class ViewHolder {
        TextView text_left, text_right, text_middle, text_dash;
        ImageView image_view_app_icon;
        RelativeLayout parent;
    }
}

package com.sj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sj.android.appusage.R;
import com.sj.android.appusage.Utility.HttpImageLoader;
import com.sj.android.appusage.Utility.UsageInfo;
import com.sj.android.appusage.Utility.Utils;

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
	private int mCurrentSelectedItem = -1;
	

	public void setCurrentSelectedPos(int pos){
		mCurrentSelectedItem = pos;
	}
    @SuppressWarnings("unchecked")
    public UsageListAdapter(Context context, Data data) throws Exception {
        mContext = context;
        mData = data;
        mImageLoader = HttpImageLoader.getInstance(context);
        mKeys = new ArrayList<>();
        mCurrentSelectedItem = -1;
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
    
    public void setClickedItem(int position) {
        mCurrentSelectedItem = position;
        notifyDataSetChanged();
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
    public boolean isEnabled(int position) {
    	// TODO Auto-generated method stub
    	return true;
    }
    
    @Override
    public int getItemViewType(int position) {
    	// TODO Auto-generated method stub
    	return 1;
    }

    private int lastPosition = -1;
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.usage_list_item, null);

            holder = new ViewHolder();
            holder.rootView = (FrameLayout)convertView.findViewById(R.id.frameForAnimLayout);
            holder.image_view_app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.text_dash = (TextView) convertView.findViewById(R.id.textView_dash);
            holder.text_left = (TextView) convertView.findViewById(R.id.text1);
            holder.text_right = (TextView) convertView.findViewById(R.id.text3);
            holder.text_middle = (TextView) convertView.findViewById(R.id.text2);
            holder.parent = (RelativeLayout) convertView.findViewById(R.id.parentLayout);
            holder.swipeText = (TextView) convertView.findViewById(R.id.text);
			holder.parent = (RelativeLayout) convertView
					.findViewById(R.id.parentLayout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
		holder.position = position;
		holder.parent.setTag(holder);
		
		if (Utils.isTabletDevice(mContext)) {
			if (mCurrentSelectedItem == position && position != 0) {
				holder.parent
						.setBackgroundColor(mContext.getResources().getColor(R.color.color_action_bar_background));
			} else if (mCurrentSelectedItem != -1) {
				holder.parent.setBackgroundColor(Color.WHITE);
			}
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
        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        convertView.startAnimation(animation);
        lastPosition = position;
        return convertView;
    }


	private class ViewHolder {
		TextView text_left, text_right, text_middle, text_dash,swipeText;
		ImageView image_view_app_icon;
		RelativeLayout parent;
		FrameLayout rootView;
		int position;
	}
}

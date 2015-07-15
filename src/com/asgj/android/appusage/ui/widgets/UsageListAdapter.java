package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.HttpImageLoader;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;

public class UsageListAdapter<Data> extends BaseAdapter implements
		View.OnTouchListener {

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
	private static final int MININUM_DISTANCE_FOR_SWIPE = 50;
	private static final int MAX_DISTANCE_FOR_CLICK = 0;
	OnItemTouchListener mTouchListener = null;
	private static final int SWIPE_DURATION = 400;
	float x_touchDown = 0;
	private int mCurrentSelectedItem = -1;
	
	
	public interface OnItemTouchListener {
		public void onItemSwiped(int position);

		public void onItemClicked(int position);
	}
	
	public void setCurrentSelectedPos(int pos){
		mCurrentSelectedItem = pos;
	}

	public void setOnItemTouchListener(OnItemTouchListener listener) {
		mTouchListener = listener;
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

			holder.parent.setOnTouchListener(this);
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
        return convertView;
    }
	Drawable rootDrawable = null;

	@Override
	public boolean onTouch(final View v, MotionEvent event) {
		if(!UsageSharedPrefernceHelper.getSwipeFeatureEnable(mContext)){
			return false;
		}
		final ViewHolder holder = (ViewHolder) v.getTag();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			x_touchDown = event.getX();
			v.setBackgroundColor(mContext.getResources().getColor(R.color.color_action_bar_background_transparent));
			rootDrawable = holder.rootView.getBackground();
			break;
		case MotionEvent.ACTION_MOVE:
			float deltaX = event.getRawX() - x_touchDown;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			v.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
			if (x_touchDown != 0) {
				float x_touchUp = event.getX();
				
				if ((x_touchUp - x_touchDown) > MININUM_DISTANCE_FOR_SWIPE) {
					
					if (mTouchListener != null) {
						if (event.getAction() == MotionEvent.ACTION_CANCEL) {
							holder.swipeText.setVisibility(View.VISIBLE);
							holder.rootView.setBackgroundColor(mContext.getResources().getColor(R.color.color_action_bar_background));
							float x = event.getX() + v.getTranslationX();
							deltaX = x - x_touchDown;
							float deltaXAbs = Math.abs(deltaX);
							float fractionCovered = 0;
							float endX = 0;
							float endAlpha = 0;
							fractionCovered = deltaXAbs / v.getWidth();
							endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
							endAlpha = 0;
							long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
							v.animate().setDuration(duration).alpha(endAlpha)
									.translationX(endX)
									.withEndAction(new Runnable() {
										@Override
										public void run() {
											 v.setAlpha(1);
											v.setTranslationX(0);
											holder.rootView.setBackground(rootDrawable);
											holder.swipeText.setVisibility(View.INVISIBLE);
											mTouchListener.onItemSwiped(holder.position);
										}
									});
						}else{
							v.setAlpha(1);
							v.setTranslationX(0);
						}

					}
				} else if ((x_touchUp - x_touchDown) == MAX_DISTANCE_FOR_CLICK) {
					v.setAlpha(1);
					v.setTranslationX(0);
					mCurrentSelectedItem = holder.position;
					notifyDataSetChanged();
					mTouchListener.onItemClicked(holder.position);
				}
			}
			break;
		}
		return true;
	}

	private class ViewHolder {
		TextView text_left, text_right, text_middle, text_dash,swipeText;
		ImageView image_view_app_icon;
		RelativeLayout parent;
		FrameLayout rootView;
		int position;
	}
}

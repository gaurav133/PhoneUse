package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.ResolveInfo;

public class PreferenceListAdapter extends BaseAdapter implements View.OnClickListener,
        OnSeekBarChangeListener {

    private ArrayList<ResolveInfo> mPackageList = null;
    private Context mContext = null;
    private int mSelectedCount = 0;
    private static final int MAXIMUM_APPLICATIONS_NOTIFICATION = 5;
    private static final int MAXIMUM_APPLICATIONS_FILTERING = 10;
    private boolean mIsChecked[];
    private Set<String> mAlreadySelectedSet;
    private int mCurrentPrefType = 0;
    protected static final int NOTIFICATION_PREF = 1;
    private static final int PACKAGES_FILTER_PREF = 2;

    PreferenceListAdapter(ArrayList<ResolveInfo> packageList, Context context, int currentPref) {
        mPackageList = packageList;
        mContext = context;
        mIsChecked = new boolean[mPackageList.size()];
        mCurrentPrefType = currentPref;
    }

    protected int getCurrentPref() {
        return mCurrentPrefType;
    }

    protected void setSelectedCount(int count) {
        if (count >= 0) {
            mSelectedCount = count;
        }
    }

    protected void setPackageList(ArrayList<ResolveInfo> list) {
        this.mPackageList = list;
    }

	}

	public ArrayList<ResolveInfo> getSelectedPackages() {
		return mPackageList;
	}
	
	public ArrayList<String> getUnSelectedPackages(){
		return unSelectedList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mPackageList.size();
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

    protected void setCheckedArray(boolean[] mIsChecked) {
        this.mIsChecked = mIsChecked;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        ResolveInfo info;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.prefernce_list_item_layout, null);

			holder.labelTextView = (TextView) convertView
					.findViewById(R.id.title_package);
			holder.checkbox = (CheckBox) convertView
					.findViewById(R.id.checkBox_package);
			holder.timeBar = (SeekBar) convertView.findViewById(R.id.seekBar1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.position = position;
		if (!mIsChecked[position]) {
			holder.timeBar.setVisibility(View.GONE);
			holder.checkbox.setChecked(false);
		} else {
			if(mCurrentPrefType == NOTIFICATION_PREF)
			holder.timeBar.setVisibility(View.VISIBLE);
			holder.checkbox.setChecked(true);
		}
		holder.labelTextView.setText(mPackageList.get(position)
				.getmApplicationName());

		convertView.setOnClickListener(this);
		return convertView;
	}

	@Override
	public void onClick(View v) {

		if (v instanceof RelativeLayout) {
			ViewHolder holder = (ViewHolder) v.getTag();
			int pos = holder.position;
			SeekBar seekbar = holder.timeBar;
			CheckBox checkbox = holder.checkbox;

            if (!mIsChecked[pos]) {

                if (mCurrentPrefType == NOTIFICATION_PREF
                        && mSelectedCount + 1 > MAXIMUM_APPLICATIONS_NOTIFICATION) {
                    Toast.makeText(
                            mContext,
                            String.format(mContext
                                    .getString(R.string.string_select_packages_maximum_app_toast),
                                    MAXIMUM_APPLICATIONS_NOTIFICATION), Toast.LENGTH_LONG).show();
                    return;
                } else if (mCurrentPrefType == PACKAGES_FILTER_PREF
                        && mSelectedCount + 1 > MAXIMUM_APPLICATIONS_FILTERING) {
                    Toast.makeText(
                            mContext,
                            String.format(mContext
                                    .getString(R.string.string_select_packages_maximum_app_toast),
                                    MAXIMUM_APPLICATIONS_FILTERING), Toast.LENGTH_LONG).show();
                    return;
                } else {
                    mIsChecked[pos] = true;
                    checkbox.setChecked(true);
                    if (mCurrentPrefType == NOTIFICATION_PREF) {
                        seekbar.setOnSeekBarChangeListener(this);

                        seekbar.setTag(holder.position);
                        seekbar.setProgress(mPackageList.get(pos).getmInputtime());
                        seekbar.setVisibility(View.VISIBLE);
                    }
                    mSelectedCount++;
                }
            } else {
                mIsChecked[pos] = false;
                seekbar.setVisibility(View.GONE);
                checkbox.setChecked(false);
                mSelectedCount--;
            }
        }
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int position = (Integer) seekBar.getTag();
		mPackageList.get(position).setmInputtime(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	private static class ViewHolder {
		TextView labelTextView;
		CheckBox checkbox;
		SeekBar timeBar;
		int position;
	}
}

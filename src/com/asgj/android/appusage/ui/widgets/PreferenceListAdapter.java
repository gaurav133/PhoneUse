package com.asgj.android.appusage.ui.widgets;

import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.PackageInfo;
import com.asgj.android.appusage.Utility.UsageSharedPrefernceHelper;
import com.asgj.android.appusage.Utility.Utils;

public class PreferenceListAdapter extends BaseAdapter implements
		View.OnClickListener {

	private ArrayList<PackageInfo> mPackageList = null;
	private Context mContext = null;
	private int mSelectedCount = 0;
	private static int MAXIMUN_APPLICATIONS = 5;
	private ArrayList<String> mSelectionList = null;

	PreferenceListAdapter(ArrayList<PackageInfo> packageList, Context context) {
		mPackageList = packageList;
		mContext = context;
		mSelectionList = new ArrayList<String>();
		Set<String> alreadySelectedList = UsageSharedPrefernceHelper
				.getSelectedApplicationForTracking(mContext);
		Log.d("anurag", "already selcted list.. :" + alreadySelectedList);
		if (alreadySelectedList != null) {
			Log.d("anurag",
					"already selcted list.. :" + alreadySelectedList.size());
		}
		if (alreadySelectedList != null && alreadySelectedList.size() > 0) {
			mSelectionList.addAll(alreadySelectedList);
			mSelectedCount = mSelectionList.size();
			for (String s : mSelectionList) {
				for(int i = 0; i< mPackageList.size() ; i++){
					if(mPackageList.get(i).getmApplicationName().equals(s)){
						Log.d("anurag", "already selcted list.set checked true. :" + s);
						mPackageList.get(i).setChecked(true);
					}
				}
			}
			this.notifyDataSetChanged();
		}

	}

	public ArrayList<String> getSelectedPackages() {
		return mSelectionList;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.prefernce_list_item_layout,
					null);
		}

		CheckBox checkbox = (CheckBox) convertView
				.findViewById(R.id.checkBox_package);
		checkbox.setTag(position);
		checkbox.setChecked(mPackageList.get(position).isChecked());
		checkbox.setText(Utils.getApplicationLabelName(mContext, mPackageList
				.get(position).getmApplicationName()));
		checkbox.setOnClickListener(this);
		return convertView;
	}

	@Override
	public void onClick(View v) {
		if (v instanceof CheckBox) {
			int pos = (int)v.getTag();
			String name = mPackageList.get(pos).getmApplicationName();
			if (!mSelectionList.contains(name)) {
				mSelectionList.add(name);
				mPackageList.get(pos).setChecked(true);
				if (mSelectedCount < MAXIMUN_APPLICATIONS) {
					mSelectedCount++;
					Log.d("anurag", "seelcted count increase to.. :"
							+ mSelectedCount);

				} else {
					((CheckBox) v).setChecked(false);
					Toast.makeText(
							mContext,
							mContext.getString(R.string.string_select_packages_maximum_app_toast),
							Toast.LENGTH_LONG).show();
				}
			} else {
				mSelectionList.remove(name);
				mPackageList.get(pos).setChecked(false);
				mSelectedCount--;
			}
		}

	}
}

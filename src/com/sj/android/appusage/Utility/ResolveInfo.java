package com.sj.android.appusage.Utility;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
public class ResolveInfo implements Parcelable {
	
	private String mApplicationName;
	private int mInputtime;
	private String mPackageName;
	
	public ResolveInfo(){
		
	}
	public int getmInputtime() {
		return mInputtime;
	}
	public void setmInputtime(int mInputtime) {
		this.mInputtime = mInputtime;
	}
	public String getmApplicationName() {
		return mApplicationName;
	}
	public void setmApplicationName(String mApplicationName) {
		this.mApplicationName = mApplicationName;
	}
	
	public String getmPackageName() {
        return mPackageName;
    }
    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle b = new Bundle();
		b.putInt("mInputtime", mInputtime);
		b.putString("mPackageName", mPackageName);
		b.putString("mApplicationName", mApplicationName);
		dest.writeBundle(b);

	}

	public static final Parcelable.Creator<ResolveInfo> CREATOR = new Parcelable.Creator<ResolveInfo>() {
		public ResolveInfo createFromParcel(Parcel in) {
			return new ResolveInfo(in);
		}

		public ResolveInfo[] newArray(int size) {
			return new ResolveInfo[size];
		}
	};
	
	private ResolveInfo(Parcel in) {
        Bundle b = in.readBundle();
        mPackageName = b.getString("mPackageName");
        mApplicationName = b.getString("mApplicationName");
        mInputtime = b.getInt("mInputtime");
    }

}

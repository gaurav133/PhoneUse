package com.asgj.android.appusage.Utility;

public class ResolveInfo {
	
	private String mApplicationName;
	private boolean isChecked;
	private int mInputtime;
	private String mPackageName;
	
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
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public String getmPackageName() {
        return mPackageName;
    }
    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }
	
	

}

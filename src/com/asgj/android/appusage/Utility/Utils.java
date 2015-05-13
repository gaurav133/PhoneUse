package com.asgj.android.appusage.Utility;

import java.util.concurrent.TimeUnit;

public class Utils {
	
	public static long getTimeInSecFromNano(long nanoSec){
		return TimeUnit.SECONDS.convert(nanoSec, TimeUnit.NANOSECONDS);
	}
	
	public static int getTimeInHourFromNano(long nanoSec){
		return (int)(getTimeInSecFromNano(nanoSec)/3600L);
	}
	
	public static int getTimeInMinsFromNano(long nanoSec){
		return (int)(getTimeInSecFromNano(nanoSec)/60L);
	}

}

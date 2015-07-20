package com.asgj.android.appusage.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.asgj.android.appusage.R;
import com.asgj.android.appusage.Utility.UsageInfo;
import com.asgj.android.appusage.Utility.Utils;
import com.asgj.android.appusage.database.PhoneUsageDbHelper.Columns;
import com.asgj.android.appusage.database.PhoneUsageDbHelper.Table;

public class PhoneUsageDatabase {

	public static final String MUSIC_PACKAGE_NAME = "music_package";
	private static final String LOG_TAG = PhoneUsageDatabase.class.getSimpleName();
	private Context mContext;
	PhoneUsageDbHelper mDbHelper = null;
	SQLiteDatabase mDatabase = null;

	public PhoneUsageDatabase(Context context) {
		mContext = context;
		mDbHelper = PhoneUsageDbHelper.getInstance(mContext);
		mDatabase = mDbHelper.getWritableDatabase();
	}

	
	public void exportDatabse(String databaseName) {
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				String currentDBPath = "//data//" + "com.asgj.android.appusage"
						+ "//databases//" + databaseName + "";
				String backupDBPath = "backupname.db";
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);

				if (currentDB.exists()) {
					FileInputStream fis = new FileInputStream(currentDB);
					FileChannel src = fis.getChannel();
					FileOutputStream fos = new FileOutputStream(backupDB);
					FileChannel dst = fos.getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
					fis.close();
					fos.close();
				}
			}
		} catch (Exception e) {

		}
	}

	public void insertMusicEntry(UsageInfo mValues) {
		insertApplicationEntry(MUSIC_PACKAGE_NAME, mValues);
	}

    public ArrayList<UsageInfo> getMusicEntryInInterval(String date, long start_time,
            long end_time, boolean isShowExtendDurationIntervals) {
        return getApplicationEntryInInterval(MUSIC_PACKAGE_NAME, date, start_time, end_time,
                isShowExtendDurationIntervals);
    }

    public ArrayList<UsageInfo> getApplicationEntryInInterval(String packageName, String date,
            long start_time, long end_time, boolean isShowExtendDurationIntervals) {
        String selection = Columns.COLUMN_APP_NAME + "= '" + packageName + "'  AND "
                + Columns.COLUMN_DATE + "= '" + date + "' AND "
                + Columns.COLUMN_START_INTERVAL_TIME + ">" + start_time;
        if (!isShowExtendDurationIntervals)
            selection = selection + " AND " + Columns.COLUMN_END_INTERVAL_TIME + "<" + end_time;
        Cursor cursor = mDatabase.query(Table.TABLE_NAME, null, selection, null, null, null, null);
        ArrayList<UsageInfo> mInfoList = new ArrayList<UsageInfo>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int startIntervalIndex = cursor.getColumnIndex(Columns.COLUMN_START_INTERVAL_TIME);
            int endIntervalIndex = cursor.getColumnIndex(Columns.COLUMN_END_INTERVAL_TIME);
            int durationIndex = cursor.getColumnIndex(Columns.COLUMN_INTERVAL_DURATION);
            do {
                UsageInfo info = new UsageInfo();
                info.setmIntervalStartTime(cursor.getLong(startIntervalIndex));
                info.setmIntervalEndTime(cursor.getLong(endIntervalIndex));
                info.setmIntervalDuration(cursor.getLong(durationIndex));
                mInfoList.add(info);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mInfoList;

    }

    public HashMap<Long, UsageInfo> getAppIntervalsBetweenDates(String packageName,
            Calendar startCalendar, Calendar endCalendar) {

        String startDate = Utils.getDateFromMiliSeconds(startCalendar.getTimeInMillis());
        String endDate = Utils.getDateFromMiliSeconds(endCalendar.getTimeInMillis());

        String indexCreate = "CREATE INDEX IF NOT EXISTS " + "date " + "ON " + Table.TABLE_NAME
                + "(" + Columns.COLUMN_DATE + "," + Columns.COLUMN_APP_NAME + ")";
        mDatabase.execSQL(indexCreate);
        String selection = Columns.COLUMN_APP_NAME + "= '" + packageName + "' AND "
                + Columns.COLUMN_DATE + " BETWEEN '" + startDate + "'  AND '" + endDate + "'";

        HashMap<Long, UsageInfo> map = new HashMap<>();
        Cursor cursor = mDatabase.query(Table.TABLE_NAME, null, selection, null, null, null, null);
        Log.v(LOG_TAG, "Cursor count is: " + cursor.getCount());
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int startIntervalIndex = cursor
                    .getColumnIndex(Columns.COLUMN_START_INTERVAL_TIME);
            int endIntervalIndex = cursor
                    .getColumnIndex(Columns.COLUMN_END_INTERVAL_TIME);
            int durationIndex = cursor
                    .getColumnIndex(Columns.COLUMN_INTERVAL_DURATION);
            do {
                long startTime = System.nanoTime();
                UsageInfo info = new UsageInfo();
                info.setmIntervalStartTime(cursor.getLong(startIntervalIndex));
                info.setmIntervalEndTime(cursor.getLong(endIntervalIndex));
                info.setmIntervalDuration(cursor.getLong(durationIndex));
                map.put(info.getmIntervalStartTime(), info);
                //mInfoList.add(info);
                Log.v(LOG_TAG, "This loop  cursor iteration took : " + Utils.getTimeInSecFromNano(System.nanoTime() - startTime) + " ms.");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }
	
    public void insertApplicationEntry(String pkgname, UsageInfo mValues) {
        ContentValues cv = new ContentValues();
        cv.put(Columns.COLUMN_APP_NAME, pkgname);
        String date = Utils.getDateFromMiliSeconds(mValues.getmIntervalStartTime());
        cv.put(Columns.COLUMN_DATE, date);
        cv.put(Columns.COLUMN_START_INTERVAL_TIME, mValues.getmIntervalStartTime());
        cv.put(Columns.COLUMN_END_INTERVAL_TIME, mValues.getmIntervalEndTime());
        cv.put(Columns.COLUMN_INTERVAL_DURATION, mValues.getmIntervalDuration());
        mDatabase.insert(Table.TABLE_NAME, null, cv);
    }

    public HashMap<String, Long> getAppDurationForGivenTimes(Context context,
            Calendar startCalendar, Calendar endCalendar) {

        String startDate = Utils.getDateFromMiliSeconds(startCalendar.getTimeInMillis());
        String endDate = Utils.getDateFromMiliSeconds(endCalendar.getTimeInMillis());
        
        String selection = Columns.COLUMN_APP_NAME + "<> '" + MUSIC_PACKAGE_NAME + "' AND "
                + Columns.COLUMN_DATE + " BETWEEN '" + startDate + "'  AND '" + endDate + "'";
        String[] projection = new String[] {Columns.COLUMN_APP_NAME, Columns.COLUMN_INTERVAL_DURATION};

        Cursor cursor = mDatabase.query(Table.TABLE_NAME, projection, selection, null, null, null, null);
        HashMap<String, Long> map = new HashMap<String, Long>();

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int columnDuration = cursor
                    .getColumnIndex(Columns.COLUMN_INTERVAL_DURATION);
            int columnAppName = cursor
                    .getColumnIndex(Columns.COLUMN_APP_NAME);
            do {
                String pkgName = cursor.getString(columnAppName);
                long duration = cursor.getLong(columnDuration);
                if (map.containsKey(pkgName)) {
                    duration = duration + map.get(pkgName);
                }
                map.put(pkgName, duration);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }
    
    public HashMap<String, Long> getApplicationEndTimeStampsForMentionedTimeBeforeToday(Context context,
            Calendar startCalendar, Calendar endCalendar) {

        String startDate = Utils.getDateFromMiliSeconds(startCalendar.getTimeInMillis());
        String endDate = Utils.getDateFromMiliSeconds(endCalendar.getTimeInMillis());
        
        String selection = Columns.COLUMN_APP_NAME + "<> '" + MUSIC_PACKAGE_NAME + "' AND "
                + Columns.COLUMN_DATE + " BETWEEN '" + startDate + "'  AND '" + endDate + "'";
        String[] projection = new String[] {Columns.COLUMN_APP_NAME, Columns.COLUMN_END_INTERVAL_TIME};
        
        Cursor cursor = mDatabase.query(Table.TABLE_NAME, projection, selection, null, Columns.COLUMN_APP_NAME, null, Columns.COLUMN_END_INTERVAL_TIME + " DESC");
        HashMap<String, Long> map = new HashMap<String, Long>();

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String pkgName = cursor.getString(cursor.getColumnIndex(Columns.COLUMN_APP_NAME));
                long timestamp = cursor.getLong(cursor
                        .getColumnIndex(Columns.COLUMN_END_INTERVAL_TIME));
                if (map.containsKey(pkgName)) {
                    if (map.get(pkgName) > timestamp) {
                        map.put(pkgName, timestamp);
                    }
                } else {
                    map.put(pkgName, timestamp);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }

    public ArrayList<UsageInfo> getMusicIntervalsBetweenDates(Context context,
            Calendar startCalendar, Calendar endCalendar) {

        String startDate = Utils.getDateFromMiliSeconds(startCalendar.getTimeInMillis());
        String endDate = Utils.getDateFromMiliSeconds(endCalendar.getTimeInMillis());

        String selection = Columns.COLUMN_APP_NAME + "= '" + MUSIC_PACKAGE_NAME + "' AND "
                + Columns.COLUMN_DATE + " BETWEEN '" + startDate + "'  AND '" + endDate + "'";

        Cursor cursor = mDatabase.query(Table.TABLE_NAME, null, selection, null, null, null, Columns.COLUMN_END_INTERVAL_TIME + " DESC");
        ArrayList<UsageInfo> mInfoList = new ArrayList<UsageInfo>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int columnStartInterval = cursor
                    .getColumnIndex(Columns.COLUMN_START_INTERVAL_TIME);
            int columnEndInterval = cursor
                    .getColumnIndex(Columns.COLUMN_END_INTERVAL_TIME);
            int columnDuration = cursor
                    .getColumnIndex(Columns.COLUMN_INTERVAL_DURATION);
            do {
                UsageInfo info = new UsageInfo();
                info.setmIntervalStartTime(cursor.getLong(columnStartInterval));
                info.setmIntervalEndTime(cursor.getLong(columnEndInterval));
                info.setmIntervalDuration(cursor.getLong(columnDuration));
                mInfoList.add(info);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mInfoList;

	}

    public long getTotalDurationOfAppInInternal(String packageName, Calendar calendar) {
        String date = Utils.getDateFromMiliSeconds(calendar.getTimeInMillis());
        String selection = Columns.COLUMN_APP_NAME + "= '" + packageName + "'  AND "
                + Columns.COLUMN_DATE + "= '" + date + "'";

        Cursor cursor = mDatabase.query(Table.TABLE_NAME, null, selection, null, null, null, null);
        long total_time = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int columnDuration = cursor.getColumnIndex(Columns.COLUMN_INTERVAL_DURATION);
            do {
                total_time = total_time + cursor.getLong(columnDuration);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return total_time;

    }

    

    

    public long getTotalDurationOfAllAppsByDate(String date) {
        long time_start = Utils.getMiliSecFromDate(date);
        long time_end = time_start + (24 * 3600 * 1000);
        String selection = Columns.COLUMN_DATE + "=" + date + " AND "
                + Columns.COLUMN_START_INTERVAL_TIME + ">" + time_start + " AND "
                + Columns.COLUMN_START_INTERVAL_TIME + "<" + time_end;
        Cursor cursor = mDatabase.query(Table.TABLE_NAME, null, selection, null, null, null, null);
        long total_time = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int columnDuration = cursor.getColumnIndex(Columns.COLUMN_INTERVAL_DURATION);
            do {
                total_time = total_time + cursor.getLong(columnDuration);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return total_time;

	}

    public long getTotalDurationOfApplicationOfAppByDate(String packageName, long miliSec) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(miliSec);

        return getTotalDurationOfAppInInternal(packageName, calendar);
    }

    public ArrayList<String> getAllPackagesUsedByDate(String date) {
        long time_start = Utils.getMiliSecFromDate(date);
        long time_end = time_start + (24 * 3600 * 1000);
        String selection = Columns.COLUMN_DATE + "=" + date + " AND "
                + Columns.COLUMN_START_INTERVAL_TIME + ">" + time_start + " AND "
                + Columns.COLUMN_START_INTERVAL_TIME + "<" + time_end;
        Cursor cursor = mDatabase.query(Table.TABLE_NAME, null, selection, null, null, null, null);
        ArrayList<String> mPackList = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int columnAppName = cursor.getColumnIndex(Columns.COLUMN_APP_NAME);
            do {
                mPackList.add(cursor.getString(columnAppName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mPackList;
    }
}

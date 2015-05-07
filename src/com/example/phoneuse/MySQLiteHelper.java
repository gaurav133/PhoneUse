package com.example.phoneuse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

//Class for creating and upgrading DB.
public class MySQLiteHelper extends SQLiteOpenHelper {
    public MySQLiteHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    private Context mContext = null;
    private static MySQLiteHelper instance = null;

    // Get instance of this class
    public static synchronized MySQLiteHelper getInstance(Context context) {
        // Use lazy instantiation.
        if (instance == null) {
            // Instance is null, hence create new one.
            instance = new MySQLiteHelper(context);
        }
        return instance;
    }

    private MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Get database instance.
    private SQLiteDatabase sqlDB = null;
    // Common column names.
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_APP_NAME = "APP_NAME";
    public static final String COLUMN_TIME_SPENT = "TIME_SPENT";
    // Table Names.
    public static final String TABLE_FOREGROUND = "FOREGROUND";
    public static final String TABLE_BACKGROUND = "BACKGROUND";
    public static final String DATABASE_NAME = "PhoneUse.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE_FOREGROUND = "create table " + TABLE_FOREGROUND
            + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_APP_NAME
            + " text not null," + COLUMN_TIME_SPENT + " real not null);";
    private static final String DATABASE_CREATE_BACKGROUND = "create table " + TABLE_BACKGROUND
            + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_APP_NAME
            + " text not null," + COLUMN_TIME_SPENT + " real not null);";
    private static final String GET_FOREGROUND_TIMES = "Select " + COLUMN_APP_NAME + "," + COLUMN_TIME_SPENT + " FROM FOREGROUND";
    private static final String GET_BACKGROUND_TIMES = "Select " + COLUMN_APP_NAME + "," + COLUMN_TIME_SPENT + " FROM BACKGROUND";

    public static void insert(HashMap<String, Double> foregroundMap, SQLiteDatabase db) {
        for (Map.Entry<String, Double> mapValues : foregroundMap.entrySet()) {
            /*String INSERT_QUERY = "Insert INTO FOREGROUND(APP_NAME,TIME_SPENT) values("
                    + mapValues.getKey() + "," + mapValues.getValue() + ")";
            db.execSQL(INSERT_QUERY);*/
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_APP_NAME, mapValues.getKey());
            cv.put(COLUMN_TIME_SPENT, mapValues.getValue());
            db.insert(TABLE_FOREGROUND, null, cv);
        }
    }
    
    public static void exportDatabse(String databaseName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ "com.example.phoneuse"+"//databases//"+databaseName+"";
                String backupDBPath = "backupname.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
    }

    public static void retrieve(SQLiteDatabase db) {
      //  db.execSQL(GET_FOREGROUND_TIMES);
        
        exportDatabse("PhoneUse.db");
        String arr[] = {COLUMN_APP_NAME, COLUMN_TIME_SPENT};
        String selectionFilter = "TIME_SPENT >= 0";
        Cursor c1 = db.query(TABLE_FOREGROUND, arr, selectionFilter, null, COLUMN_TIME_SPENT, null, COLUMN_TIME_SPENT);
        Cursor c = db.rawQuery(GET_FOREGROUND_TIMES, null);
        Log.v ("gaurav", "Cursor column count: " + c.getColumnCount());
        Log.v ("gaurav", "Cursor count: " + c.getCount());
        
        Log.v ("gaurav", "Cursor column count 1: " + c1.getColumnCount());
        Log.v ("gaurav", "Cursor count 1: " + c1.getCount());
       // Log.v ("gaurav", "Cursor string: " + c.getString(1));
       // Log.v ("gaurav", "Cursor 2nd column" + c.getFloat(2));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        // mContext = this;
        // Create Tables.
        db.execSQL(DATABASE_CREATE_FOREGROUND);
        db.execSQL(DATABASE_CREATE_BACKGROUND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOREGROUND);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BACKGROUND);
        // create new tables
        onCreate(db);
    }
}

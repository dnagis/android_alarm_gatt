package vvnx.alrmgatt;



import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.Date;

import android.content.IntentFilter;
import android.content.Intent;


import java.text.SimpleDateFormat;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import android.content.ContentValues;




//sqlite3 /data/data/vvnx.alrmgatt/databases/log.db "select datetime(EPOCH/1000, 'unixepoch', 'localtime'), BATT from log;"

public class BaseDeDonnees extends SQLiteOpenHelper {
	
	private static final String TAG = "LocTrack";

    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_BDD_MAIN = "CREATE TABLE log (ID INTEGER PRIMARY KEY AUTOINCREMENT, EPOCH INTEGER NOT NULL, BATT INTEGER NOT NULL)";

    private SQLiteDatabase bdd;


    public BaseDeDonnees(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BDD_MAIN);  
    }
    
    public void logOne(int batteryPct){
		bdd = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("EPOCH", System.currentTimeMillis());
		values.put("BATT", batteryPct);
		bdd.insert("log", null, values);
	}
	
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
	
}

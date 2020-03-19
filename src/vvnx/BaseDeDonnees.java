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




//sqlite3 /data/data/vvnx.alrmgatt/databases/log.db "select datetime(EPOCH/1000, 'unixepoch', 'localtime'), DATA, BATT from log;"

public class BaseDeDonnees extends SQLiteOpenHelper {
	
	private static final String TAG = "AlrmGatt";

    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_BDD_MAIN = "CREATE TABLE log (ID INTEGER PRIMARY KEY AUTOINCREMENT, EPOCH INTEGER NOT NULL, DATA INT NOT NULL, BATT INT NOT NULL)";

    private SQLiteDatabase bdd;


    public BaseDeDonnees(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BDD_MAIN);  
    }
    
    public void logOne(int data, int batt){
		bdd = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("EPOCH", System.currentTimeMillis());
		values.put("DATA", data);
		values.put("BATT", batt);
		bdd.insert("log", null, values);
	}
	
	
	public long fetchLastEpochBDD(){
		Log.d(TAG, "fetch dans bdd");
		bdd = this.getWritableDatabase();
		Cursor cursor_epoch = bdd.query("log", null, null, null, null, null, "EPOCH", null);
		if (cursor_epoch.getCount() > 0) {
			cursor_epoch.moveToLast();
			return cursor_epoch.getLong(1);
		} else {
			return 0;
		}
	}
	
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
	
}

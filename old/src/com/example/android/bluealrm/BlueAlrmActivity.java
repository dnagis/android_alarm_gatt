/**
 * 
 * 
 * 
adb uninstall com.example.android.bluealrm
adb install out/target/product/mido/system/app/BlueAlrm/BlueAlrm.apk

dumpsys deviceidle whitelist +com.example.android.bluealrm
#car les autorisations de localisation ne sont plus possibles programmatically, remember? (vie privée etc...)
pm grant com.example.android.bluealrm android.permission.ACCESS_COARSE_LOCATION
pm grant com.example.android.bluealrm android.permission.ACCESS_FINE_LOCATION

am start-activity com.example.android.bluealrm/.BlueAlrmActivity
am force-stop com.example.android.bluealrm



sqlite3 /data/data/com.example.android.bluealrm/databases/temp.db "select datetime(ALRMTIME, 'unixepoch','localtime'), MAC, TEMP, sent from temp;"


SElinux:
pour pouvoir accéder de l'extérieur à la db il faut que le context soit kasher et pas un truc qui finisse en MCS comme
"u:object_r:app_data_file:s0:c512,c768" - car avec ça je peux pas faire de SElinux rules.
du coup, j'essaie de ressembler à des packages système et c'est pour ça qu'il y a
android:sharedUserId="android.uid.system" dans le manifest, et LOCAL_CERTIFICATE := platform dans le Android.mk

on obtiens au ls -Z /data/data:
u:object_r:system_app_data_file:s0             com.example.android.bluealrm




*/


package com.example.android.bluealrm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;
import android.os.Bundle;

import android.util.Log;


public class BlueAlrmActivity extends Activity {
	
	private static final String TAG = "BlueAlrm";
	
    //30 * 1000 = 30 seconds in milliseconds 
    //de toutes façons en dessous de 60s: W AlarmManager: Suspiciously short interval 30000 millis; expanding to 60 seconds

    private static final long PERIODE_MS = 900 * 1000;

    // An intent for AlarmService, to trigger it as if the Activity called startService().
    private PendingIntent mAlarmSender;

    // Contains a handle to the system alarm service
    private AlarmManager mAlarmManager;

    /**
     * This method is called when Android starts the activity. 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
				
		//sinon E AndroidRuntime: android.util.SuperNotCalledException: Activity {com.example.android.newalarm/com.example.android.newalarm.AlarmActivity} did not call through to super.onCreate()
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "AlarmActivity **Vvnx**:  onCreate()");
		
        // Create a PendingIntent to trigger a startService() for AlarmService
        mAlarmSender = PendingIntent.getService(  // set up an intent for a call to a service (voir dev guide intents à "Using a pending intent")
            BlueAlrmActivity.this,  // the current context
            0,  // request code (not used)
            new Intent(BlueAlrmActivity.this, BlueAlrmService.class),  // A new Service intent 'c'est un intent explicite'
            0   // flags (none are required for a service)
        );

        // Gets the handle to the system alarm service
        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        
        long firstAlarmTime = SystemClock.elapsedRealtime();
        
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, // based on time since last wake up
                firstAlarmTime,  // sends the first alarm immediately
                PERIODE_MS,  // repeats every XX
                mAlarmSender  // when the alarm goes off, sends this Intent
            );        
        
    }

    //Shuts off the repeating countdown timer.
	private void stopAlarm() {
		    // Cancels the repeating countdown timer
            mAlarmManager.cancel(mAlarmSender);
	};

}

/**
 * 
 * 
 * Parce que avoir à lancer une commande archi longue en adb après chaque reboot... 
 * c'est juste trop chiant....
 * 
 * Ce receiver de boot ne marche que si l'appli a déjà été lancé une fois of course
 * 
 * 
 * **/

package com.example.android.bluealrm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.AlarmManager;
import android.os.SystemClock;
import android.os.Bundle;




public class Receiver extends BroadcastReceiver {
    private static final String TAG = "BlueAlrm";
    
    private static final long PERIODE_MS = 900 * 1000;
    private PendingIntent mAlarmSender;
    private AlarmManager mAlarmManager;
    
    

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive dans le Receiver de BlueAlrm, action=" + intent.getAction());
        
             
        if (intent.getAction().equals(android.content.Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(TAG, "intent boot received");	
			
        // Create a PendingIntent to trigger a startService() for AlarmService
        mAlarmSender = PendingIntent.getService(  // set up an intent for a call to a service (voir dev guide intents à "Using a pending intent")
            context,  // the current context
            0,  // request code (not used)
            new Intent(context, BlueAlrmService.class),  // A new Service intent 'c'est un intent explicite'
            0   // flags (none are required for a service)
        );

        // Gets the handle to the system alarm service
        AlarmManager mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        
        long firstAlarmTime = SystemClock.elapsedRealtime();
        
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, // based on time since last wake up
                firstAlarmTime,  // sends the first alarm immediately
                PERIODE_MS,  // repeats every XX
                mAlarmSender  // when the alarm goes off, sends this Intent
            );   
		}
		
		
	}
	
}

/*adb push ./out/target/product/mido/system/framework/HelloActivity.jar /system/framework

 */

package vvnx.alrmgatt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.util.Log;

import android.content.Context;
import android.content.Intent;

import android.app.AlarmManager;
import android.app.PendingIntent;


import android.os.SystemClock;




/**
 * A minimal "Hello, World!" application.
 */
public class AlrmGattActivity extends Activity {
	
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;

	private static final String TAG = "AlrmGatt";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity.  You can find it
        // in res/layout/hello_activity.xml
        View view = getLayoutInflater().inflate(R.layout.mon_activity, null);
        setContentView(view);
    }
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1");
		
		alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(this, AlarmReceiver.class);
		alarmIntent = PendingIntent.getService(this, 0, intent, 0);
		
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() +
        60 * 1000, alarmIntent);
		
		
	}
}

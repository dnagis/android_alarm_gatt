package vvnx.alrmgatt;

import android.app.Service;
import android.util.Log;
import android.os.IBinder;
import android.content.Intent;

//Broadcast Receiver
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

import android.os.BatteryManager;


//<service android:name=".AlarmReceiver" />
//sinon au logcat:
//ActivityManager: Unable to start service Intent { flg=0x4 cmp=vvnx.alrmgatt/.AlarmReceiver (has extras) } U=0: not found


public class AlarmReceiver extends Service {
	
	private static final String TAG = "AlrmGatt";
	
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
	private BaseDeDonnees maBDD;
	
	
	@Override
    public void onCreate() {
		Log.d(TAG, "onCreate");
		if (maBDD == null)
			maBDD = new BaseDeDonnees(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		
		//récup batt lvl (je peux pas le récup dans BDD car registerReceiver possible que depuis un service)
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		Log.d(TAG, "batt lvl=" + level + " et " + scale);
		int batteryPct = level * 100 / scale;
		
		//log bdd
		maBDD.logOne(batteryPct);
		
		//Set prochaine alarm
		alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent newIntent = new Intent(this, AlarmReceiver.class);
		alarmIntent = PendingIntent.getService(this, 0, newIntent, 0);
		
		alarmMgr.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
			SystemClock.elapsedRealtime() +
			300 * 1000, alarmIntent);
				
		stopSelf();
		
		return START_NOT_STICKY; //not_sticky: le systeme ne le redémarrera pas si ménage à cause de memory
	}

    @Override
    public void onDestroy() {		
		Log.d(TAG, "OnDestroy");
	
	 }
	 
	  @Override
	public IBinder onBind(Intent intent) {
      // We don't provide binding, so return null
      return null;
	}
	
}

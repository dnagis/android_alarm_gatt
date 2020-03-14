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


//<service android:name=".AlarmReceiver" />
//sinon au logcat:
//ActivityManager: Unable to start service Intent { flg=0x4 cmp=vvnx.alrmgatt/.AlarmReceiver (has extras) } U=0: not found


public class AlarmReceiver extends Service {
	
	private static final String TAG = "AlrmGatt";
	
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
	
	@Override
    public void onCreate() {
		Log.d(TAG, "onCreate");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent newIntent = new Intent(this, AlarmReceiver.class);
		alarmIntent = PendingIntent.getService(this, 0, newIntent, 0);
		
		alarmMgr.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() +
        60 * 1000, alarmIntent);
		
		
		
				
		stopSelf();
		
		return START_NOT_STICKY;
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

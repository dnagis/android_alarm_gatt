package vvnx.alrmgatt;

import android.app.Service;
import android.util.Log;
import android.os.IBinder;
import android.os.Handler;
import android.content.Intent;

//Broadcast Receiver
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

import android.os.BatteryManager;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import java.util.UUID;

import java.lang.Runnable;




//<service android:name=".AlarmReceiver" />
//sinon au logcat:
//ActivityManager: Unable to start service Intent { flg=0x4 cmp=vvnx.alrmgatt/.AlarmReceiver (has extras) } U=0: not found


public class AlarmReceiver extends Service {
	
	private static final String TAG = "AlrmGatt";
	
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
	private BaseDeDonnees maBDD;
	
	private BluetoothManager bluetoothManager = null;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothAdapter mBluetoothAdapter = null;	
	private BluetoothGattCharacteristic mCharacteristic = null;	
	private static final UUID SERVICE_UUID = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
	private static final UUID CHARACTERISTIC_PRFA_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
	private String BDADDR = "30:AE:A4:04:C3:5A";	
	
	@Override
    public void onCreate() {
		Log.d(TAG, "onCreate");
		//création de la base de données
		if (maBDD == null)
			maBDD = new BaseDeDonnees(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
	
		//Bluetooth	
		connectmGatt();		
		
		//Set prochaine alarm
		alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent newIntent = new Intent(this, AlarmReceiver.class);
		alarmIntent = PendingIntent.getService(this, 0, newIntent, 0);
		
		alarmMgr.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
			SystemClock.elapsedRealtime() +
			300 * 1000, alarmIntent);
	
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
	
	
	void connectmGatt(){
		
		bluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);	
		mBluetoothAdapter = bluetoothManager.getAdapter();	
		if (mBluetoothAdapter == null) {
			Log.d(TAG, "fail à la récup de l'adapter");
			return;
		}
		
		BluetoothDevice monEsp = mBluetoothAdapter.getRemoteDevice(BDADDR);   
		
		//Il n'y a pas de timeout pour connectGatt: si le device n'est pas dispo ou si something goes wrong on va jamais sortir d'ici
		mBluetoothGatt = monEsp.connectGatt(this, true, gattCallback);
		
		//j'ai pas trouvé d'implémentation du timeout, donc on fait un timeout homebrew
		new Handler().postDelayed(new Runnable() {
		        @Override
		        public void run() {
					terminado();
		         }
			}, 10000);
	}
	
	
	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.i(TAG, "on passe dans onConnectionStateChange()");
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				gatt.discoverServices(); //permet de passer par onServicesDiscovered()
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				//Log.i(TAG, "Disconnected from GATT server.");	
			}
	        //si je mets pas ça  j'ai n+1 onCharacteristicChanged() à chaque passage (nouvelle instance BluetoothGattCallback?)
			//***MAIS***
			//close() la connexion du coup j'ai pas d'auto-reconnect...
			//mBluetoothGatt.close(); 
		}
	
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				Log.i(TAG, "onServicesDiscovered callback.");
				mCharacteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_PRFA_UUID);
				mBluetoothGatt.readCharacteristic(mCharacteristic);
		}
	
		
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				//Log.i(TAG, "onCharacteristicRead callback.");
				byte[] data = characteristic.getValue();
				Log.i(TAG, "onCharacteristicRead callback -> char data: " + data[0] + " " + data[1] + " " + data[2]); //donne pour data[0]: -86 et printf %x -86 --> ffffffffffffffaa or la value côté esp32 est 0xaa 
				maBDD.logOne(data[0]);
				mBluetoothGatt.close();
				//de toutes façons le timeout va stopSelf(), inutile stopSelf() ici
				}
	};
	
	
	public void terminado(){
		    Log.d(TAG, "terminado");
		    mBluetoothGatt.close(); //attention, empêcherait l'auto-reconnect(). mais sans ça j'ai des fantômes de tentatives de connexion. Et ici l'auto-reconnect c'est pas mon soucis
			stopSelf();		
	}
	
	
	
			
		
		//récup batt lvl (je peux pas le récup dans BDD car registerReceiver possible que depuis un service)
		/*IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		Log.d(TAG, "batt lvl=" + level + " et " + scale);
		int batteryPct = level * 100 / scale;*/
	

	
	
	
}

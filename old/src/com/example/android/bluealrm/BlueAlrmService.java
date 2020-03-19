/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluealrm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.Toast;
import android.os.Handler;
import android.util.Log;
import java.util.Collections;
import java.util.List;

//bt
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanRecord;

//sql
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;

public class BlueAlrmService extends Service {
	
	private static final String TAG = "BlueAlrm";
	
	//alarm
    private static final String ALARM_SERVICE_THREAD = "AlarmService";
    public static final long WAIT_TIME_SECONDS = 15;
    public static final long MILLISECS_PER_SEC = 1000;	
	
	//bt
	private BluetoothAdapter mBluetoothAdapter = null;    
	private BluetoothLeScanner mBluetoothLeScanner = null;
	private static final long SCAN_PERIOD = 10000;
	private int capteur_1_lu = 0;
	private int capteur_2_lu = 0;
	

	
	//sql
    private BaseDeDonnees maBDD;
    private SQLiteDatabase bdd;
    
    Context le_bon_context;
	

    IBinder mBinder = new AlarmBinder();

    public class AlarmBinder extends Binder {
        // Constructor. Calls the super constructor to set up the instance.
        public AlarmBinder() {
            super();
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {

            // Call the parent method with the arguments passed in
            return super.onTransact(code, data, reply, flags);
        }
    }
    
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "OnStartCommand");
		
		
		final BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        
        mBluetoothAdapter = bluetoothManager.getAdapter();


        if (mBluetoothAdapter == null) {
            Log.d(TAG, "fail à la récup de l'adapter");
            return START_NOT_STICKY;
        }
        
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        
        if (mBluetoothLeScanner == null) {
            Log.d(TAG, "fail à la récup du LeScanner");
            return START_NOT_STICKY;
        }
		
		
		Log.d(TAG, "visiblement on a pas eu de pb pour recup les handles bt");
		
		
		scanLeDevice();
		
		//stopSelf();
		return START_NOT_STICKY;
	}
    
    
    
    
    

    /**
     * Service
     */
    @Override
    public void onCreate() {
		le_bon_context = this;
        stopSelf();
    }

    @Override
    public void onDestroy() {		
		Log.d(TAG, "BlueAlrm: OnDestroy");		
	 }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    
    
    
    /**
     * Bluetooth
     */    
    
    
	private void scanLeDevice() {

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
			Log.d(TAG, "stopLeScan");
			mBluetoothLeScanner.stopScan(mScanCallback);
			stopSelf();
			}
		}, SCAN_PERIOD);
		
		
		//ScanFilter.Builder fbuilder = new ScanFilter.Builder().setDeviceAddress("30:AE:A4:04:C8:2E");
		ScanFilter.Builder fbuilder = new ScanFilter.Builder();
		ScanFilter filter = fbuilder.build();
		final List<ScanFilter> filters = Collections.singletonList(filter);
		
		ScanSettings.Builder sbuilder = new ScanSettings.Builder()
			.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
			.setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT);
		ScanSettings settings = sbuilder.build();
		
		Log.d(TAG, "startScan");
		mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
	}
  
	private ScanCallback mScanCallback = new ScanCallback() {
	    @Override
	    public void onScanResult(int callbackType, ScanResult result) {
			
			String addr_result = result.getDevice().getAddress();
			
			//filtre perso
			String addrfilter1 = new String("30:AE:A4:45:C8:86");
			String addrfilter2 = new String("30:AE:A4:04:C3:5A");
			
			//mécanisme pour arrêter dès qu'on a une lecture pour chacun		
			if (!addr_result.equals(addrfilter1) && !addr_result.equals(addrfilter2)) return;//si on a pas un de nos capteurs: return
			if (addr_result.equals(addrfilter1) && (capteur_1_lu > 0) ) return; //on a déjà lu le 1
			if (addr_result.equals(addrfilter2) && (capteur_2_lu > 0) ) return; //on a déjà lu le 2
			
			ScanRecord scanRecord = result.getScanRecord();
			byte[] scan_data = scanRecord.getBytes();
			int temp_intpart = scan_data[5];
			float temp_decpart = scan_data[6];
			float temp = temp_intpart + (temp_decpart/100);
			if (scan_data[4] == 0) temp = -temp;
			long timestamp = System.currentTimeMillis()/1000;
	        Log.d(TAG, "onScanResult addr=" + addr_result + " temp=" + temp);  
	         
			maBDD = new BaseDeDonnees(le_bon_context);
            bdd = maBDD.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ALRMTIME", timestamp);
            values.put("TEMP", temp);
            values.put("MAC", addr_result);
            bdd.insert("temp", null, values);
            
            if (addr_result.equals(addrfilter1)) capteur_1_lu++; //on a lu le capteur 1
            if (addr_result.equals(addrfilter2)) capteur_2_lu++; //on a lu le capteur 2
            
            if ( (capteur_1_lu > 0) && (capteur_2_lu > 0))  
	            {	        
		        mBluetoothLeScanner.stopScan(mScanCallback);
				stopSelf();
				}
				
			
	   }
	   
	   	@Override
	    public void onScanFailed(int errorCode) {
	        Log.d(TAG, "onScanFailed");
	   }
	   
	   	@Override
	    public void onBatchScanResults(List<ScanResult> results) {
	        Log.d(TAG, "onBatchScanResults");
	   }
	};

}

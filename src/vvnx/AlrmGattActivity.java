package vvnx.alrmgatt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.util.Log;

import android.content.Context;
import android.content.Intent;






public class AlrmGattActivity extends Activity {
	
	private static final String TAG = "AlrmGatt";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.mon_activity, null);
        setContentView(view);
    }
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1");
 
        startService(new Intent(this, AlarmReceiver.class));
		
	}
}

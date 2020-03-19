package vvnx.alrmgatt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import java.text.SimpleDateFormat;

import android.widget.TextView;

import android.util.Log;

import android.content.Context;
import android.content.Intent;






public class AlrmGattActivity extends Activity {
	
	private static final String TAG = "AlrmGatt";
	private BaseDeDonnees maBDD;
	TextView textview_1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.mon_activity, null);
        setContentView(view);
        textview_1 = findViewById(R.id.textview_1); 
        if (maBDD == null) maBDD = new BaseDeDonnees(this);
    }
    
	@Override
	public void onResume() {
	    super.onResume();
	    //Log.d(TAG, "onResume() récup de epoch: " + maBDD.fetchLastEpochBDD());
	    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss");	
		textview_1.setText("LAST DATA: "+ sdf.format(maBDD.fetchLastEpochBDD()));
		}
    
    public void ActionPressBouton_1(View v) {
		Log.d(TAG, "press bouton 1"); 
        startService(new Intent(this, AlarmReceiver.class));		
	}
}

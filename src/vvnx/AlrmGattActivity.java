/*adb push ./out/target/product/mido/system/framework/HelloActivity.jar /system/framework

 */

package vvnx.alrmgatt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.util.Log;


/**
 * A minimal "Hello, World!" application.
 */
public class AlrmGattActivity extends Activity {

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
	}
}

package com.rakesh.alarmmanagerexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class AlarmManagerActivity extends Activity {

    public static enum HARDWARE {
        NETWORK, VIBRATION, SOUND, SCREEN, SENSOR_ACC, AGPS, GPS
    };
	
    private CheckBox[] mChHardware;

    private final long mStartInterval = 2 * 1000; // ms
    private final long mRepeatInterval = 35 * 1000; // ms
	private AlarmManagerBroadcastReceiver mAlarm;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_manager);
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.llMain);

        mChHardware = new CheckBox[HARDWARE.values().length];
        for (int i = 0; i < HARDWARE.values().length; i++) {
            mChHardware[i] = new CheckBox(this);
            mChHardware[i].setText(HARDWARE.values()[i].name());
            mainLayout.addView(mChHardware[i]);
        }

        mAlarm = new AlarmManagerBroadcastReceiver(this.getApplicationContext());
    }
    
    @Override
	protected void onStart() {
		super.onStart();
	}

    private ArrayList<HARDWARE> getCheckedHardware() {
        ArrayList<HARDWARE> ret = new ArrayList<HARDWARE>();

        for (int i = 0; i < mChHardware.length; i++) {
            if (mChHardware[i].isChecked()) {
                ret.add(HARDWARE.values()[i]);
            }
        }

        return ret;
    }

    public void startRepeatingTimer(View view) {
    	Context context = this.getApplicationContext();
    	if(mAlarm != null){
            mAlarm.setRepeatAlarm(context, System.currentTimeMillis(), mRepeatInterval,
                    getCheckedHardware());
    	}else{
    		Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void cancelRepeatingTimer(View view){
    	Context context = this.getApplicationContext();
    	if(mAlarm != null){
    		mAlarm.cancelRepeatAlarm(context);
    	}else{
    		Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void onetimeTimer(View view){
    	Context context = this.getApplicationContext();
    	if(mAlarm != null){
            mAlarm.setOnetimeAlarm(context, System.currentTimeMillis() + mStartInterval,
                    getCheckedHardware());
    	}else{
    		Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void testCase(View view){
    	Context context = this.getApplicationContext();
    	// TODO: Write an automation test case.
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_widget_alarm_manager, menu);
        return true;
    }
}

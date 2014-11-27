package com.rakesh.alarmmanagerexample;

import com.rakesh.alarmmanagerexample2.R;

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

    public static enum Hardware {
        NETWORK, VIBRATION, SOUND, SCREEN, SENSOR_ACC, AGPS, GPS
    };
	
    private CheckBox[] mChHardware;

    private final long mStartInterval = 2 * 1000; // ms
    private final long mRepeatInterval = 65 * 1000; // ms
	private AlarmManagerBroadcastReceiver mAlarm;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_manager);
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.llMain);

        mChHardware = new CheckBox[Hardware.values().length];
        for (int i = 0; i < Hardware.values().length; i++) {
            mChHardware[i] = new CheckBox(this);
            mChHardware[i].setText(Hardware.values()[i].name());
            mainLayout.addView(mChHardware[i]);
        }

        mAlarm = new AlarmManagerBroadcastReceiver(this.getApplicationContext());
    }
    
    @Override
	protected void onStart() {
		super.onStart();
	}

    private ArrayList<Hardware> getCheckedHardware() {
        ArrayList<Hardware> ret = new ArrayList<Hardware>();

        for (int i = 0; i < mChHardware.length; i++) {
            if (mChHardware[i].isChecked()) {
                ret.add(Hardware.values()[i]);
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

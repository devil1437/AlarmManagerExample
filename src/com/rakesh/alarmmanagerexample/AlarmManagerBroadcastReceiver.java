package com.rakesh.alarmmanagerexample;

import com.rakesh.alarmmanagerexample.AlarmManagerActivity.Hardware;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver implements SensorEventListener {

	public final static String ONE_TIME = "onetime";
	public final static String INTERVAL = "interval";
    public final static String CHECK_HARDWARE = "checkedHardware";
	private final static String TAG = "AlarmManagerBroadcastReceiver";
	private final static String WAKELOCK_TAG = "alarmmanagerexample";
	private final static boolean DEBUG = true;
    private final static String NETWORK_HOST = "1.161.49.106"; // My desktop
    private final static String NETWORK_COMMAND = "ping -c 1 " + NETWORK_HOST;
    private final static long[] DEFAULT_VIBRATE_PATTERN = {
            0, 250, 250, 250
    };

    private final long mStartInterval = 2 * 1000; // ms
    private final long mDefaultRepeatInterval = 15 * 1000; // ms
    private Context mActivityContext;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final long mSensorTimeout = 5 * 1000; // ms
    private long mSensorStartRTC = -1;
	
    public AlarmManagerBroadcastReceiver() {
    }

    public AlarmManagerBroadcastReceiver(Context activityContext) {
        mActivityContext = activityContext;
    }

    private int sendPing() {
        Runtime runtime = Runtime.getRuntime();
        Process proc;
        final int buffLen = 1024;

        try {
            proc = runtime.exec(NETWORK_COMMAND);

            if (DEBUG) {
                InputStream stdout = proc.getInputStream();
                byte[] buffer = new byte[buffLen];
                int read;
                String out = new String();
                while (true) {
                    read = stdout.read(buffer);
                    out += new String(buffer, 0, read);
                    if (read < buffLen) {
                        // we have read everything
                        break;
                    }
                }
                Log.d(TAG, out);
            }

            proc.waitFor();
            int exit = proc.exitValue();
            return exit;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return -1;
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
		// Acquire the lock
		wl.acquire();

		// You can do the processing here update the widget/remote views.
		Bundle extras = intent.getExtras();
		StringBuilder msgStr = new StringBuilder();

        ArrayList<Hardware> checkedHardware = null;
        if (extras != null) {
            checkedHardware = (ArrayList<Hardware>) extras.getSerializable(CHECK_HARDWARE);
            if (extras.getBoolean(ONE_TIME, Boolean.FALSE)) {
                msgStr.append("One time Timer: ");
            } else {
                msgStr.append("Repeat Timer: ");
                long interval = extras.getLong(INTERVAL, mDefaultRepeatInterval);
                setRepeatAlarm(context, interval, checkedHardware);
            }
        }
		
		Format formatter = new SimpleDateFormat("hh:mm:ss a");
		msgStr.append(formatter.format(new Date()));

		Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();
		if(DEBUG){
			Log.d(TAG, msgStr.toString());
		}

        for(Hardware h : checkedHardware){
            switch(h){
                case NETWORK:
                    if (DEBUG) {
                        Log.d(TAG, "NETWORK. Send a ping packet.");
                    }
                    sendPing();
                    break;
                case VIBRATION:
                    if (DEBUG) {
                        Log.d(TAG, "VIBRATION. Default vibration.");
                    }
                    Vibrator vibrator = (Vibrator) context
                            .getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(DEFAULT_VIBRATE_PATTERN, -1);
                    break;
                case SOUND:
                    if (DEBUG) {
                        Log.d(TAG, "NETWORK. Default sound.");
                    }
                    Uri notification = RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(),
                            notification);
                    r.play();
                    break;
                case SCREEN:
                    if (DEBUG) {
                        Log.d(TAG, "NETWORK. Screen on.");
                    }
                    PowerManager.WakeLock fullWl = pm.newWakeLock(
                            PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                    | PowerManager.ON_AFTER_RELEASE, WAKELOCK_TAG);
                    fullWl.acquire();
                    fullWl.release();
                    break;
                case SENSOR_ACC:
                    if (DEBUG) {
                        Log.d(TAG, "NETWORK. Accelerometer 5 sec.");
                    }
                    mSensorStartRTC = -1;
                    mSensorManager = (SensorManager) context
                            .getSystemService(Context.SENSOR_SERVICE);
                    mAccelerometer = mSensorManager
                            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    mSensorManager.registerListener(this, mAccelerometer,
                            SensorManager.SENSOR_DELAY_NORMAL);
                    break;
                case AGPS:
                    if (DEBUG) {
                        Log.d(TAG, "NETWORK. Request one time AGPS.");
                    }
                    break;
                case GPS:
                    if (DEBUG) {
                        Log.d(TAG, "NETWORK. Request one time GPS.");
                    }
                    break;
            }
        }
                
		// Release the lock
		wl.release();

	}

    public void setRepeatAlarm(Context context, long repeatInterval,
            ArrayList<Hardware> checkedHardware) {
		if(DEBUG){
			Log.d(TAG, "setRepeatAlarm().");
		}
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(CHECK_HARDWARE, checkedHardware);
		intent.putExtra(ONE_TIME, Boolean.FALSE);
		intent.putExtra(INTERVAL, repeatInterval);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + mStartInterval, pi);
	}

	public void cancelRepeatAlarm(Context context) {
		if(DEBUG){
			Log.d(TAG, "cancelRepeatAlarm().");
		}
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

    public void setOnetimeAlarm(Context context, ArrayList<Hardware> checkedHardware) {
		if(DEBUG){
			Log.d(TAG, "setOnetimeAlarm().");
		}
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(CHECK_HARDWARE, checkedHardware);
		intent.putExtra(ONE_TIME, Boolean.TRUE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
		am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + mStartInterval, pi);
	}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (DEBUG) {
                Log.d(TAG, "onSensorChanged(). x:" + event.values[0] + " y:" + event.values[1]
                        + " z:" + event.values[2]);
            }
            if (mSensorStartRTC == -1) {
                mSensorStartRTC = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - mSensorStartRTC > mSensorTimeout) {
                mSensorManager.unregisterListener(this);
            }
        }
    }
}

package com.rakesh.alarmmanagerexample;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver implements
        SensorEventListener, ConnectionCallbacks,
        OnConnectionFailedListener {

	public final static String ONE_TIME = "onetime";
	public final static String INTERVAL = "interval";
    public final static String SCHEDULE_TIME = "scheduleTime";
    public final static String CHECK_HARDWARE = "checkedHardware";
    public final static String IS_EXACT = "isExact";
	private final static String TAG = "AlarmManagerBroadcastReceiver";
	private final static String WAKELOCK_TAG = "alarmmanagerexample";

	private final static boolean DEBUG = true;
    private final static String NETWORK_HOST = "140.112.28.143"; // Newslab
                                                                 // server
    private final static String NETWORK_COMMAND = "ping -c 1 " + NETWORK_HOST;
    private final static long[] DEFAULT_VIBRATE_PATTERN = {
            0, 250, 250, 250
    };
    protected static final int RELEASE_WAKELOCK = 10001;

    private Context mApplicationContext;
    private AlarmManagerActivity mActivityContext;
    private final int mId;

    private boolean mIsRepeat;
    private final long mWakelockTimeout = 5 * 1000; // ms
    private final long mAlignment = 60 * 1000; // ms
    private PendingIntent mPendingIntent;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final long mSensorTimeout = 5 * 1000; // ms
    private long mSensorStartRTC = -1;

    LocationManager mLocationManager;
    MyLocationListener mMyLocationListener = new MyLocationListener();
    private final long mGPSTimeout = 5 * 1000; // ms

    PowerManager.WakeLock mWakelock;
    private AlarmView mView;
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case RELEASE_WAKELOCK:
                    if (mWakelock != null && mWakelock.isHeld()) {
                        if (DEBUG) {
                            Log.d(TAG, "release wakelock.");
                        }
                        mWakelock.release();
                    }
            }
        };
    };

    public AlarmManagerBroadcastReceiver() {
        mId = -1;
    }

    public AlarmManagerBroadcastReceiver(Context context, AlarmManagerActivity activityContext,
            int id) {
    	mApplicationContext = context;
    	mActivityContext = activityContext;
        mId = id;
    }

    /**
     * Send a ping packet to specified host.
     * @return
     */
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
            mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
            return exit;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);

        return -1;
    }

    /**
     * Notification default vibration.
     * @param context
     */
    private void vibrate(Context context) {
    	Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(DEFAULT_VIBRATE_PATTERN, -1);
        mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
	}
    
    /**
     * Notification default sound.
     * @param context
     */
    private void playSound(Context context) {
        Uri notification = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(),
                notification);
        r.play();
        mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
	}
    
    /**
     * Turn on the screen.
     * @param context
     */
	private void screenOn(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock fullWl = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE, WAKELOCK_TAG);
        fullWl.acquire();
        fullWl.release();
        mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
	}
	
	/**
	 * Register the listener of accelerometer.
	 * You can set mSensorTimeout to 
	 * @param context
	 */
	private void registerAcc(Context context) {
		mSensorStartRTC = -1;
		mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
	}

	/**
	 * Request one time agps/gps.
	 * @param networkProvider
	 * @param context
	 */
	private void requestSingleGPS(Context context, String networkProvider) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mMyLocationListener = new MyLocationListener();
        Looper myLooper = Looper.myLooper();
        // mLocationManager.requestLocationUpdates(networkProvider, 5 * 1000, 5
        // * 1000,
        // mMyLocationListener,
        // myLooper);
        mLocationManager.requestSingleUpdate(networkProvider,
                mMyLocationListener, myLooper);
        final Handler myHandler = new Handler(myLooper);

        myHandler.postDelayed(new Runnable() {
            public void run() {
                mLocationManager.removeUpdates(mMyLocationListener);
                mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
            }
        }, mGPSTimeout);
	}

    @Override
	public void onReceive(Context context, Intent intent) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakelock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
		// Acquire the lock
        mWakelock.acquire(mWakelockTimeout);
        if (DEBUG && mWakelock.isHeld()) {
            Log.d(TAG, "acquire wakelock.");
        }

		// You can do the processing here update the widget/remote views.
		Bundle extras = intent.getExtras();
		StringBuilder msgStr = new StringBuilder();

        ArrayList<Hardware> checkedHardware = null;
        if (extras != null) {
            checkedHardware = (ArrayList<Hardware>) extras.getSerializable(CHECK_HARDWARE);
            if (extras.getBoolean(ONE_TIME, Boolean.FALSE)) {
                msgStr.append("Id: ");
                msgStr.append(intent.getAction());
                msgStr.append(".One time Timer: ");
            } else {
                Context applicationContext = context.getApplicationContext();
                int id = Integer.parseInt(intent.getAction());
                long interval = extras.getLong(INTERVAL, -1);
                long scheduleTime = extras.getLong(SCHEDULE_TIME, -1);
                boolean isExact = extras.getBoolean(IS_EXACT);

                msgStr.append("Id: ");
                msgStr.append(id);
                msgStr.append(".Repeat Timer: ");

                if (isExact) {
                    long nextScheduleTime = scheduleTime + interval;
                    long nowRTC = System.currentTimeMillis();
                    if (DEBUG) {
                        Log.d(TAG, "nextScheduleTime: " + nextScheduleTime + ", nowRTC: " + nowRTC);
                    }
                    if (nextScheduleTime < nowRTC) {
                        nextScheduleTime += (((nowRTC - nextScheduleTime) / interval) + 1)
                                * interval;
                    }
                    intent.putExtra(SCHEDULE_TIME, nextScheduleTime);
                    AlarmManager am = (AlarmManager) applicationContext
                            .getSystemService(Context.ALARM_SERVICE);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext,
                            id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    am.setExact(AlarmManager.RTC_WAKEUP, nextScheduleTime, pendingIntent);
                }
            }
        }
		
		Format formatter = new SimpleDateFormat("hh:mm:ss a");
		msgStr.append(formatter.format(new Date()));

		Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();
		if(DEBUG){
			Log.d(TAG, msgStr.toString());
		}

        if (checkedHardware.size() == 0) {
            mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
        }

        for(Hardware h : checkedHardware){
        	if (DEBUG) {
                Log.d(TAG, h.name());
            }
            switch(h){
                case NETWORK:
                    sendPing();
                    break;
                case VIBRATION:
                	vibrate(context);
                    break;
                case SOUND:
                	playSound(context);
                    break;
                case SCREEN:
                	screenOn(context);
                    break;
                case SENSOR_ACC:
                	registerAcc(context);
                    break;
                case AGPS:
                	requestSingleGPS(context, LocationManager.NETWORK_PROVIDER);
                    break;
                case GPS:
                	requestSingleGPS(context, LocationManager.GPS_PROVIDER);
                    break;
            }
        }
	}

    private long getNextWakeUpTime(long elapsedTime) {
        return elapsedTime;
        // return elapsedTime - elapsedTime % mAlignment;
    }

    public void setRepeatAlarm(long scheduleTime, long repeatInterval,
            ArrayList<Hardware> checkedHardware, boolean isExact) {
		if(DEBUG){
            Log.d(TAG, "setRepeatAlarm(). Hardware: " + checkedHardware.toString());
		}
        AlarmManager am = (AlarmManager) mApplicationContext
				.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mApplicationContext, AlarmManagerBroadcastReceiver.class);
        intent.setAction(Integer.toString(mId));
        intent.putExtra(CHECK_HARDWARE, checkedHardware);
		intent.putExtra(ONE_TIME, Boolean.FALSE);
		intent.putExtra(INTERVAL, repeatInterval);
        intent.putExtra(SCHEDULE_TIME, scheduleTime);
        intent.putExtra(IS_EXACT, isExact);
        mPendingIntent = PendingIntent.getBroadcast(mApplicationContext, mId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        if (isExact) {
            // Google doesn't provide API to set exact repeating events,
            // so we set exact one-time event repeatedly.
            am.setExact(AlarmManager.RTC_WAKEUP, scheduleTime, mPendingIntent);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, scheduleTime, repeatInterval, mPendingIntent);
        }

        mView = new AlarmView(mActivityContext, this);
        mView.setAttribute(mId, repeatInterval, checkedHardware);
        
        mIsRepeat = true;
	}

    public void cancelRepeatAlarm() {
		if(DEBUG){
			Log.d(TAG, "cancelRepeatAlarm().");
		}
        AlarmManager alarmManager = (AlarmManager) mApplicationContext
				.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mPendingIntent);
	}

    public void setOnetimeAlarm(long scheduleTime, ArrayList<Hardware> checkedHardware,
            boolean isExact) {
		if(DEBUG){
            Log.d(TAG, "setOnetimeAlarm(). Hardware: " + checkedHardware.toString());
		}
        AlarmManager am = (AlarmManager) mApplicationContext
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mApplicationContext, AlarmManagerBroadcastReceiver.class);
        intent.setAction(Integer.toString(mId));
        intent.putExtra(CHECK_HARDWARE, checkedHardware);
        intent.putExtra(ONE_TIME, Boolean.TRUE);
        mPendingIntent = PendingIntent.getBroadcast(mApplicationContext, mId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        if (isExact) {
            am.setExact(AlarmManager.RTC_WAKEUP, scheduleTime, mPendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, scheduleTime, mPendingIntent);
        }
        
        mView = new AlarmView(mActivityContext, this);
        mView.setAttribute(mId, -1, checkedHardware);
        mIsRepeat = false;
	}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (mSensorStartRTC == -1) {
                if (DEBUG) {
                    Log.d(TAG, "onSensorChanged(). x:" + event.values[0] + " y:" + event.values[1]
                            + " z:" + event.values[2]);
                }
                mSensorStartRTC = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - mSensorStartRTC > mSensorTimeout) {
                mSensorManager.unregisterListener(this);
                mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
            }
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (DEBUG) {
                Log.d(TAG, "onLocationChanged(). Location: " + location.toString());
            }
            mLocationManager.removeUpdates(mMyLocationListener);
            mHandler.sendEmptyMessageDelayed(RELEASE_WAKELOCK, 0);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    }

    public int getId() {
        return mId;
    }

    public AlarmView getView() {
        return mView;
    }
    
    public boolean isRepeat() {
        return mIsRepeat;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(200);

        return sb.toString();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

    }
}

package com.rakesh.alarmmanagerexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//TODO onSaveInstance

public class AlarmManagerActivity extends Activity  {
    private static final String SAVED_ALARM = "saved_alarm";
    private final static String TAG = "AlarmManagerActivity";
    private static final boolean DEBUG = true;

    private static final int RANDOM_TIMEOUT[] = {
            26462,
            169797,
            149862,
            46469,
            83663,
            142087,
            1302,
            120315,
            155900,
            28844,
            132224,
            108348,
            165809,
            98117,
            74943,
            18229,
            51716,
            131438,
            124335,
            164933,
            148782,
            6926,
            64147,
            174006,
            97555,
            95075,
            50430,
            44213,
            124982,
            60699,
            144178,
            43885,
            73165,
            60877,
            175076,
            46627,
            148077,
            174996,
            2853,
            12748,
            5157,
            122427,
            141474,
            33930,
            157121,
            31926,
            152393,
            162677,
            22564,
            59636,
            145752,
            64242,
            93281,
            99393,
            14566
    };

    private static int ALARM_ID = 10000;
    private CheckBox[] mChHardware;
    private SeekBar mSbInterval;
    private TextView mTvIntervalValue;
    private GridLayout mGlAlarms;

    private Context mApplicationContext;

    private final int mIntervalStart = 5;
    private final int mIntervalEnd = 900;
    private final int mIntervalIncrement = 5;
    private final int mIntervalIncrementStep = (mIntervalEnd - mIntervalStart) / mIntervalIncrement;
    private long mRepeatInterval = 5 * 1000; // ms
    private final long mStartTimeout = 2 * 1000; // ms
    private ArrayList<AlarmManagerBroadcastReceiver> mAlarms = new ArrayList<AlarmManagerBroadcastReceiver>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplicationContext = this.getApplicationContext();

        setContentView(R.layout.activity_alarm_manager);
        GridLayout hardwareLayout = (GridLayout) findViewById(R.id.glHardware);

        mChHardware = new CheckBox[Hardware.values().length];
        for (int i = 0; i < Hardware.values().length; i++) {
            mChHardware[i] = new CheckBox(this);
            mChHardware[i].setText(Hardware.values()[i].name());
            hardwareLayout.addView(mChHardware[i]);
        }

        mSbInterval = (SeekBar) findViewById(R.id.sbInterval);
        mSbInterval.setOnSeekBarChangeListener(new intervalSeekBarListener());
        mSbInterval.setProgress(0);
        mSbInterval.setMax(mIntervalIncrementStep);
        mTvIntervalValue = (TextView) findViewById(R.id.tvIntervalValue);
        mTvIntervalValue
                .setText(Integer.toString(getIntervalFromProgress(mSbInterval.getProgress()))
                        + "sec");

        mGlAlarms = (GridLayout) findViewById(R.id.glAlarms);
    }
    
    private int getIntervalFromProgress(int progress) {
        return (progress + 1) * mIntervalIncrement;
    }

    @Override
	protected void onStart() {
		super.onStart();
	}

    public void startRepeatingTimer(View view) {
        startRepeatingTimer(ALARM_ID++, 0, mRepeatInterval, getCheckedHardware(), true);
    }

    private void startRepeatingTimer(int id, long timeout, long repeatInterval,
            ArrayList<Hardware> hardware, boolean isExact) {
        if (DEBUG) {
            Log.d(TAG, "startRepeatingTimer(). id: " + id + ", timeout: " + timeout
                    + ", repeatInterval: " + repeatInterval + ", hardware: " + hardware.toString()
                    + ", exact: " + isExact);
        }
        AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver(
                mApplicationContext, this, id);

        alarm.setRepeatAlarm(System.currentTimeMillis() + timeout, repeatInterval, hardware,
                isExact);
        addAlarm(alarm);
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

    private void addAlarm(AlarmManagerBroadcastReceiver alarm) {
        mAlarms.add(alarm);
        mGlAlarms.addView(alarm.getView());
    }

    public void onetimeTimer(View view) {
        AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver(
                mApplicationContext, this, ALARM_ID++);

        alarm.setOnetimeAlarm(System.currentTimeMillis() + mStartTimeout, getCheckedHardware(),
                false);
    }

    public void testCase(View view) {
        ArrayList<Hardware> hardware = new ArrayList<Hardware>();
        // Random ran = new Random();
//        hardware.add(Hardware.NETWORK);
//        startRepeatingTimer(1, ran.nextInt(15 * 60 * 1000), 3 * 60 * 1000, hardware, true);
//        startRepeatingTimer(2, ran.nextInt(15 * 60 * 1000), 5 * 60 * 1000, hardware, true);
//        startRepeatingTimer(3, ran.nextInt(15 * 60 * 1000), 10 * 60 * 1000, hardware, true);
//
//        hardware = new ArrayList<Hardware>();
//        hardware.add(Hardware.SENSOR_ACC);
//        startRepeatingTimer(31, ran.nextInt(15 * 60 * 1000), 3 * 60 * 1000, hardware, true);
//        startRepeatingTimer(32, ran.nextInt(15 * 60 * 1000), 5 * 60 * 1000, hardware, true);
//        startRepeatingTimer(33, ran.nextInt(15 * 60 * 1000), 10 * 60 * 1000, hardware, true);
//
//        hardware = new ArrayList<Hardware>();
//        hardware.add(Hardware.AGPS);
//        startRepeatingTimer(21, ran.nextInt(15 * 60 * 1000), 3 * 60 * 1000, hardware, true);
//        startRepeatingTimer(22, ran.nextInt(15 * 60 * 1000), 5 * 60 * 1000, hardware, true);
//        startRepeatingTimer(23, ran.nextInt(15 * 60 * 1000), 10 * 60 * 1000, hardware, true);
//
//        hardware = new ArrayList<Hardware>();
//        hardware.add(Hardware.SCREEN);
//        startRepeatingTimer(16, ran.nextInt(15 * 60 * 1000), 3 * 60 *
//                1000, hardware, true);
//        startRepeatingTimer(17, ran.nextInt(15 * 60 * 1000), 5 * 60 *
//                1000, hardware, true);
//        startRepeatingTimer(18, ran.nextInt(15 * 60 * 1000), 10 * 60
//                * 1000, hardware, true);

        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(1, RANDOM_TIMEOUT[0], 3 * 60 * 1000, hardware, true);
        startRepeatingTimer(2, RANDOM_TIMEOUT[1], 5 * 60 * 1000, hardware, true);
        startRepeatingTimer(3, RANDOM_TIMEOUT[2], 10 * 60 * 1000, hardware, true);
        startRepeatingTimer(4, RANDOM_TIMEOUT[3], 4 * 60 * 1000, hardware, true);

        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.SENSOR_ACC);
        startRepeatingTimer(31, RANDOM_TIMEOUT[30], 3 * 60 * 1000, hardware, true);
        startRepeatingTimer(32, RANDOM_TIMEOUT[31], 5 * 60 * 1000, hardware, true);
        startRepeatingTimer(33, RANDOM_TIMEOUT[32], 10 * 60 * 1000, hardware, true);
        startRepeatingTimer(34, RANDOM_TIMEOUT[33], 4 * 60 * 1000, hardware, true);

        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.AGPS);
        startRepeatingTimer(21, RANDOM_TIMEOUT[20], 3 * 60 * 1000, hardware, true);
        startRepeatingTimer(22, RANDOM_TIMEOUT[21], 5 * 60 * 1000, hardware, true);
        startRepeatingTimer(23, RANDOM_TIMEOUT[22], 10 * 60 * 1000, hardware, true);
        startRepeatingTimer(24, RANDOM_TIMEOUT[23], 4 * 60 * 1000, hardware, true);

        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.SOUND);
        startRepeatingTimer(11, RANDOM_TIMEOUT[10], 3 * 60 *
                1000, hardware, true);
        startRepeatingTimer(12, RANDOM_TIMEOUT[11], 5 * 60 *
                1000, hardware, true);
        startRepeatingTimer(13, RANDOM_TIMEOUT[12], 10 * 60
                * 1000, hardware, true);
        startRepeatingTimer(14, RANDOM_TIMEOUT[13], 4 * 60 *
                1000, hardware, true);
    }

    public void testCase2(View view) {
        // Simulate the real application's behavior.
        AddRealApplicationTrace(100, 0);
        AddRealApplicationTrace(200, 25);

        // ArrayList<Hardware> hardware = new ArrayList<Hardware>();
        // hardware.add(Hardware.AGPS);
        // startRepeatingTimer(16, 0, 10 * 1000, hardware, true);
        // startRepeatingTimer(1, 0, 10 * 1000, hardware, false);
        // startRepeatingTimer(2, 0, 10 * 1000, hardware, false);
        // startRepeatingTimer(19, 0, 10 * 1000, hardware, false);
    }

    private void AddRealApplicationTrace(int startId, int timeoutId) {
        ArrayList<Hardware> hardware = new ArrayList<Hardware>();

        // WeChat
        startRepeatingTimer(startId + 0, RANDOM_TIMEOUT[timeoutId + 0], 5 * 60 * 1000, hardware,
                true);
        startRepeatingTimer(startId + 1, RANDOM_TIMEOUT[timeoutId + 1], 15 * 60 * 1000, hardware,
                true);
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 2, RANDOM_TIMEOUT[timeoutId + 2], 15 * 60 * 1000, hardware,
                true);

        // WhatsApp
        hardware = new ArrayList<Hardware>();
        startRepeatingTimer(startId + 3, RANDOM_TIMEOUT[timeoutId + 3], 4 * 60 * 1000, hardware,
                true);
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 4, RANDOM_TIMEOUT[timeoutId + 4], 4 * 60 * 1000, hardware,
                false);

        // Line
        hardware = new ArrayList<Hardware>();
        startRepeatingTimer(startId + 5, RANDOM_TIMEOUT[timeoutId + 5], 200 * 1000, hardware, false);
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 6, RANDOM_TIMEOUT[timeoutId + 6], 200 * 1000, hardware, false);

        // Facebook
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 7, RANDOM_TIMEOUT[timeoutId + 7], 15 * 60 * 1000, hardware,
                false);
        startRepeatingTimer(startId + 8, RANDOM_TIMEOUT[timeoutId + 8], 60 * 60 * 1000, hardware,
                false);

        // Twitter
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 9, RANDOM_TIMEOUT[timeoutId + 9], 60 * 60 * 1000, hardware,
                false);

        // GoWeather
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 10, RANDOM_TIMEOUT[timeoutId + 10], 9 * 60 * 1000, hardware,
                true);
        startRepeatingTimer(startId + 11, RANDOM_TIMEOUT[timeoutId + 11], 50 * 60 * 1000, hardware,
                true);

        // Viber
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 12, RANDOM_TIMEOUT[timeoutId + 12], 10 * 60 * 1000, hardware,
                false);

        // Weibo
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 13, RANDOM_TIMEOUT[timeoutId + 13], 5 * 60 * 1000, hardware,
                true);

        // Messenger
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 14, RANDOM_TIMEOUT[timeoutId + 14], 15 * 60 * 1000, hardware,
                false);
        startRepeatingTimer(startId + 15, RANDOM_TIMEOUT[timeoutId + 15], 60 * 60 * 1000, hardware,
                true);

        // Weather
        hardware = new ArrayList<Hardware>();
        startRepeatingTimer(startId + 16, RANDOM_TIMEOUT[timeoutId + 16], 5 * 60 * 1000, hardware,
                true);

        // KakaoTalk
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 17, RANDOM_TIMEOUT[timeoutId + 17], 10 * 60 * 1000, hardware,
                false);

        // Comic
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 18, RANDOM_TIMEOUT[timeoutId + 18], 10 * 60 * 1000, hardware,
                true);

        // GOMAJI
        hardware = new ArrayList<Hardware>();
        startRepeatingTimer(startId + 19, RANDOM_TIMEOUT[timeoutId + 19], 15 * 60 * 1000, hardware,
                false);

        // LINE Webtoon
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 20, RANDOM_TIMEOUT[timeoutId + 20], 202 * 1000, hardware,
                false);

        // TTPOD
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.NETWORK);
        startRepeatingTimer(startId + 21, RANDOM_TIMEOUT[timeoutId + 21], 7 * 60 * 1000, hardware,
                false);

        // Family Locator
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.AGPS);
        startRepeatingTimer(startId + 22, RANDOM_TIMEOUT[timeoutId + 22], 10 * 60 * 1000, hardware,
                false);

        // FollowMee
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.AGPS);
        startRepeatingTimer(startId + 23, RANDOM_TIMEOUT[timeoutId + 23], 5 * 60 * 1000, hardware,
                false);

        // CellTracker
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.AGPS);
        startRepeatingTimer(startId + 24, RANDOM_TIMEOUT[timeoutId + 24], 5 * 60 * 1000, hardware,
                false);

        // Performance
        hardware = new ArrayList<Hardware>();
        hardware.add(Hardware.SOUND);
        startRepeatingTimer(startId + 25, RANDOM_TIMEOUT[timeoutId + 0], 30 * 60 * 1000, hardware,
                true);
    }

    public void resetAllTimer(View view) {
        if (mAlarms != null) {
            int size = mAlarms.size();
            while (mAlarms.size() > 0) {
                cancelTimer(mAlarms.get(0));
            }
        } else {
            Toast.makeText(mApplicationContext, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelTimer(AlarmManagerBroadcastReceiver alarm) {
        removeAlarm(alarm);
    }

    public void cancelTimer(int id) {
        if (mAlarms != null) {
            int size = mAlarms.size();
            for (int i = 0; i < size; i++) {
                AlarmManagerBroadcastReceiver alarm = mAlarms.get(i);
                if (alarm.getId() == id) {
                    removeAlarm(alarm);
                }
            }

    	}else{
            Toast.makeText(mApplicationContext, "Alarm is null", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void removeAlarm(AlarmManagerBroadcastReceiver alarm) {
        if (alarm.isRepeat()) {
            alarm.cancelRepeatAlarm();
        }
        mAlarms.remove(alarm);
        mGlAlarms.removeView(alarm.getView());
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_widget_alarm_manager, menu);
        return true;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        // savedInstanceState.putParcelableArrayList(SAVED_ALARM, mAlarms);

        super.onSaveInstanceState(savedInstanceState);
    }

	private class intervalSeekBarListener implements
            SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int interval = getIntervalFromProgress(progress);
            if (mTvIntervalValue != null) {
                mTvIntervalValue.setText(Integer.toString(interval) + "sec");
            }
            mRepeatInterval = interval * 1000;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }
    }
}

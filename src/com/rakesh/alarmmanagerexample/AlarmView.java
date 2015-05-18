package com.rakesh.alarmmanagerexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class AlarmView extends GridLayout {

    private AlarmManagerActivity mActivityContext;
    private LayoutInflater inflater;

    private TextView mTvId;
    private TextView mTvInterval;
    private TextView mTvHardware;
    private Button mBnCancel;

    private int mId;
    private long mInterval;
    private ArrayList<Hardware> mHardware;

    public AlarmView(AlarmManagerActivity context, final AlarmManagerBroadcastReceiver alarm) {
        super(context);

        mActivityContext = context;

        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(mParams);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alarm, this);

        mTvId = (TextView) view.findViewById(R.id.tvId);
        mTvInterval = (TextView) view.findViewById(R.id.tvInterval);
        mTvHardware = (TextView) view.findViewById(R.id.tvHardware);
        mBnCancel = (Button) view.findViewById(R.id.btCancel);

        mBnCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivityContext.cancelTimer(alarm);
            }
        });
    }

    public void setAttribute(int id, long interval, ArrayList<Hardware> hardware) {
        mId = id;
        mInterval = interval;
        mHardware = (ArrayList<Hardware>) hardware.clone();

        updateView();
    }

    private void updateView() {
        mTvId.setText("Id: " + mId);
        mTvInterval.setText("Interval: " + mInterval);
        StringBuffer sb = new StringBuffer(100);
        for (Hardware h : mHardware) {
            sb.append(h.name());
            sb.append(" ");
        }
        mTvHardware.setText("Hardware: " + sb.toString());
    }

}

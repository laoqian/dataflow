package com.example.a85838.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.a85838.myapplication.bluetooth.BlueToothUtils;
import com.example.a85838.myapplication.bluetooth.DynamicLineChartManager;
import com.example.a85838.myapplication.bluetooth.Filter;
import com.example.a85838.myapplication.bluetooth.LimitQueue;
import com.example.a85838.myapplication.bluetooth.MyBluetoothActivity;
import com.github.mikephil.charting.charts.LineChart;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnSeekBarChangeListener  {

    private TextView mTextMessage;
    private Handler handler;
    private static final  String TAG ="MainActivity";
    private LineChart mChart;
    DynamicLineChartManager chartManager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    public void clickHandler(View v) {

        switch (v.getId()) {
            case R.id.pause:
                Button button = findViewById(R.id.pause);
                button.setText(chartManager.change()?"继续":"暂停");
                Log.e(TAG, button.getText().toString());
                break;
            case R.id.change:
                Button change = findViewById(R.id.change);
                change.setText(BlueToothUtils.swtich()?"方式1":"方式2");
                Log.e(TAG, change.getText().toString());
                break;
            default:
                Log.e(TAG, "不支持的操作"+v.getId());
                break;
        }
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        chartManager.addEntry((float)msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };

        BlueToothUtils.setHander(handler);
        mTextMessage =  findViewById(R.id.message);
//        BottomNavigationView navigation = findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mChart = findViewById(R.id.lineChart);
//        final DynamicLineChartManager dynamicLineChartManager = new DynamicLineChartManager(mChart,Arrays.asList("原始","滤波后"), Arrays.asList(Color.CYAN,Color.GREEN));
        final DynamicLineChartManager dynamicLineChartManager = new DynamicLineChartManager(mChart,"波形",Color.CYAN);
        chartManager = dynamicLineChartManager;
        dynamicLineChartManager.setYAxis(4f, 0, 4);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MyBluetoothActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish(){
        BlueToothUtils.close();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


}

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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.a85838.myapplication.bluetooth.BlueToothUtils;
import com.example.a85838.myapplication.bluetooth.DynamicLineChartManager;
import com.example.a85838.myapplication.bluetooth.LimitQueue;
import com.example.a85838.myapplication.bluetooth.MyBluetoothActivity;
import com.github.mikephil.charting.charts.LineChart;
import java.util.List;
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
                Log.e(TAG, "暂停");
                chartManager.pause();
                break;
            case R.id.goon:
                Log.e(TAG, "继续");
                chartManager.goon();
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
                        List<Byte> list = (List<Byte>) msg.obj;
                        for(int i=0;i<list.size();i++){
//                            queue.offer(list.get(i));
                        }

                        break;
                    default:
                        break;
                }
            }
        };

        BlueToothUtils.setHander(handler);
        mTextMessage =  findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mChart = findViewById(R.id.lineChart);
        final DynamicLineChartManager dynamicLineChartManager = new DynamicLineChartManager(mChart,"数据流",Color.CYAN);
        chartManager = dynamicLineChartManager;
        dynamicLineChartManager.setYAxis(100, -100, 5);

//        mChart.setViewPortOffsets(0, 0, 0, 0);
//        mChart.setBackgroundColor(Color.rgb(0, 0, 51));
//
//        // no description text
//        mChart.getDescription().setEnabled(false);
//
//        // enable touch gestures
//        mChart.setTouchEnabled(true);
//
//        // enable scaling and dragging
//        mChart.setDragEnabled(true);
//        mChart.setScaleEnabled(true);
//
//        // if disabled, scaling can be done on x- and y-axis separately
//        mChart.setPinchZoom(false);
//
//        mChart.setDrawGridBackground(false);
//        mChart.setMaxHighlightDistance(300);
//
////        XAxis x = mChart.getXAxis();
////        x.setEnabled(false);
//
//        YAxis y = mChart.getAxisLeft();
//        y.setLabelCount(6, false);
//        y.setTextColor(Color.WHITE);
//        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
//        y.setDrawGridLines(true);
//        y.setAxisLineColor(Color.WHITE);
//
//        XAxis xAxis = mChart.getXAxis();       //获取x轴线
//        xAxis.setDrawAxisLine(true);//是否绘制轴线
//        xAxis.setDrawGridLines(false);//设置x轴上每个点对应的线
//        xAxis.setDrawLabels(true);//绘制标签  指x轴上的对应数值
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置
//        xAxis.setTextSize(12f);//设置文字大小
//        xAxis.setAxisMinimum(0f);//设置x轴的最小值 //`
//        xAxis.setAxisMaximum(31f);//设置最大值 //
//        xAxis.setLabelCount(1000);  //设置X轴的显示个数
//        xAxis.setAvoidFirstLastClipping(false);//图表将避免第一个和最后一个标签条目被减掉在图表或屏幕的边缘
//
//
//        mChart.getAxisRight().setEnabled(false);
//        mChart.getLegend().setEnabled(false);
//        mChart.animateXY(2000, 2000);
//        // dont forget to refresh the drawing
//        mChart.invalidate();

        //死循环添加数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Random random = new Random();
                        dynamicLineChartManager.addEntry(80-random.nextInt(160));
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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

//    private LineData lineDataInit(){
//        LineDataSet set1;
//        List<Entry> yVals = new ArrayList<>(1000);
//
//        set1 = new LineDataSet(yVals, "DataSet 1");
//
//        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        set1.setCubicIntensity(0.2f);
//        set1.setDrawCircles(false);
//        set1.setLineWidth(1f);
//        set1.setCircleRadius(1f);
//        set1.setCircleColor(Color.WHITE);
//        set1.setHighLightColor(Color.rgb(244, 117, 117));
//        set1.setColor(Color.WHITE);
//        set1.setFillColor(Color.WHITE);
//        set1.setFillAlpha(100);
//        set1.setDrawHorizontalHighlightIndicator(false);
//
//
//        LineData data = new LineData(set1);
//
//        data.setValueTextSize(9f);
//        data.setDrawValues(false);
//
//        return data;
//    }
//
//    private void setData() {
//        List<Byte> list = queue.getList();
//        LineData  lineData = mChart.getData();
//
//        if(lineData==null || lineData.getDataSetCount()==0){
//            lineData = lineDataInit();
//            mChart.setData(lineData);
//        }
//
//        for (int i = 0; i < list.size(); i++) {
//            Entry entry = new Entry(lineData.getEntryCount(), list.get(i).floatValue());
//            lineData.addEntry(entry,i);
//            lineData.notifyDataChanged();
//            mChart.notifyDataSetChanged();
//        }
//    }
}

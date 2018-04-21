package com.example.a85838.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a85838.myapplication.bluetooth.BlueToothUtils;
import com.example.a85838.myapplication.bluetooth.DynamicLineChartManager;
import com.example.a85838.myapplication.bluetooth.MyBluetoothActivity;
import com.github.mikephil.charting.charts.LineChart;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;


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

    private String fileName = null;
    private List<Float> voltList = new LinkedList<>();
    private void write(){

        FileOutputStream outputStream;
        try{
            StringBuilder stringBuffer = new StringBuilder();
            for(Float v:voltList){
                stringBuffer.append(v).append(",");
            }
            voltList.clear();
            outputStream =new FileOutputStream (new File(fileName),true);
            outputStream.write(stringBuffer.toString().getBytes());
            outputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermission() {

        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }

            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);

        } else {
            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "checkPermission: 已经授权！");
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

        checkPermission();

        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if(sdCardExist) {
            String dirName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/dataflow";
            File root = new File(dirName);
            if(!root.exists()){
                if (root.mkdirs()) {
                    Log.e(TAG, "创建目录成功！");
                } else {
                    Log.e(TAG, "创建目录失败！");
                }
            }else{
                for (File file : root.listFiles()){
                    if (file.isFile() ){
                        file.delete();
                    }
                }
            }

            fileName = dirName+"/"+DateTime.now().toString("yyyyMMddHHmmss")+".text";
            Log.e(TAG, fileName);
        }


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        if(fileName!=null){
                            voltList.add((float)msg.obj);
                            if(voltList.size()>10 ){
                                write();
                            }
                        }
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

package com.example.a85838.myapplication.bluetooth;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.example.a85838.myapplication.MainActivity;
import com.example.a85838.myapplication.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyBluetoothActivity extends AppCompatActivity {

    private final static int SEARCH_CODE = 0x123;
    private BluetoothAdapter mBluetoothAdapter ;
    private static final String TAG = "MyBluetoothActivity";
    private static List<BluetoothDevice> mBlueList = new ArrayList<>();
    private ListView lisetView;
    private static  boolean enable;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mybluetooth);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        lisetView           =  findViewById(R.id.list_view);
        devListShow();

        lisetView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = mBlueList.get(i);
                BlueToothUtils.connect(device);
                devListShow();
                Toast.makeText(MyBluetoothActivity.this, device.getName(), Toast.LENGTH_LONG).show();
            }
        });

        Log.e(TAG, "onCreate: GPS是否可用：" + isGpsEnable(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.setClass(MyBluetoothActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void devListShow(){
        MyAdapter adapter = new MyAdapter(MyBluetoothActivity.this, mBlueList);
        lisetView.setAdapter(adapter);
    }
    public void clickHandler(View v) {

        switch (v.getId()) {
            case R.id.search:
                Log.e(TAG, "搜索蓝牙设备");
                init();
                break;
            case R.id.closeDev:
                BlueToothUtils.close();
                break;
            case R.id.closeBluetooth:
                bluetoothClose();
                break;
            default:
                Log.e(TAG, "不支持的操作"+v.getId());
        }
    }

    private void requestPermission() {
        Log.e(TAG, "版本：" + Build.VERSION.SDK_INT);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {

        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "开启权限permission granted!");
                    //做下面该做的事
                } else {
                    Log.e(TAG, "没有定位权限，请先开启!");
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //gps是否可用(有些设备可能需要定位)
    public static final boolean isGpsEnable(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return gps || network;
    }

    private void bluetoothClose(){
        enable = false;
        BlueToothUtils.close();
        mBluetoothAdapter.disable();
    }
    /**
     * 判断蓝牙是否开启
     */
    private void init() {
        requestPermission();
        // 判断手机是否支持蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "设备不支持蓝牙");
        }
        // 判断是否打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            //弹出对话框提示用户是后打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,SEARCH_CODE);
        } else {
            // 不做提示，强行打开
            mBluetoothAdapter.enable();
        }

        startDiscovery();
        Log.e(TAG, "startDiscovery: 开启蓝牙");
    }

    /**
     * 注册异步搜索蓝牙设备的广播
     */
    private void startDiscovery() {
        if(!enable){
            // 找到设备的广播
            IntentFilter filter        = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            BroadcastReceiver receiver = getReceiver();
            // 注册广播
            registerReceiver(receiver, filter);
            // 搜索完成的广播
            IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            // 注册广播
            registerReceiver(receiver, filter1);

            Log.e(TAG, "startDiscovery: 注册广播");
            enable = true;
        }

        startScanBluth();
    }

    private BroadcastReceiver getReceiver(){

        return  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!mBlueList.contains(device)) {
                        mBlueList.add(device);
                    }

                    devListShow();
                    Log.e(TAG, "搜索到设备: " + (device.getName() + ":" + device.getAddress() + " ：" + "m" + "\n"));
                    // 搜索完成
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                    // 关闭进度条
                    progressDialog.dismiss();
                    Log.e(TAG, "搜索完成");
                }else {
                    Log.e(TAG, "其他情况" );
                }
            }
        };
    }

    /**
     * 搜索蓝牙的方法
     */
    private void startScanBluth() {

        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // 开始搜索
        mBluetoothAdapter.startDiscovery();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("正在搜索，请稍后！");
        progressDialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 获取本机蓝牙地址
     */
    private String getBluetoothAddress() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Field field = bluetoothAdapter.getClass().getDeclaredField("mService");
            // 参数值为true，禁用访问控制检查
            field.setAccessible(true);
            Object bluetoothManagerService = field.get(bluetoothAdapter);
            if (bluetoothManagerService == null) {
                return null;
            }

            Method method = bluetoothManagerService.getClass().getMethod("getAddress");
            Object address = method.invoke(bluetoothManagerService);
            if (address != null && address instanceof String) {
                return (String) address;
            } else {
                return null;
            }
            //抛一个总异常省的一堆代码...
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

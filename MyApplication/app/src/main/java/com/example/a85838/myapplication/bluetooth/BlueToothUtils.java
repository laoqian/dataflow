package com.example.a85838.myapplication.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlueToothUtils {
    private static BluetoothDevice device;
    private static final String TAG="BlueToothUtils";
    private static BluetoothSocket socket;
    private static  Thread thread;
    private static Handler handler;
    private static  float[] voltages = new float[50];
    private static  int vlenth=0 ;
    private static final int BUFFER_SIZE = 10;

    public static void setHander(Handler handler){
        BlueToothUtils.handler = handler;
    }

    public static void connect(BluetoothDevice d) {
        device = d;

        if(thread!=null){
            thread.isInterrupted();
        }

        thread = new Thread(new Runnable() {
            public void run() {

                Log.e(TAG, "启动后台线程" + Thread.currentThread().getId());

                try {
                    final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                try {
                    socket.connect();
                    InputStream inputStream = socket.getInputStream();

                    while (true) {
                        byte[] buffer = new byte[1024];
                        int lenth = inputStream.read(buffer);

                        if(lenth>0){
                            String key0,key1,key4,key5;
                            int key2,key3;

                            for(int i=0;i<lenth-5;){
                                key0 = Integer.toHexString(buffer[i]&0xff);
                                key1 = Integer.toHexString(buffer[i+1]&0xff);
                                key2 = buffer[i+2];
                                key3 = buffer[i+3];
                                key4 = Integer.toHexString(buffer[i+4]&0xff);
                                key5 = Integer.toHexString(buffer[i+5]&0xff);

//                                if("0x03".equals(key0) && "0xfc".equals(key1) && "0x03".equals(key5) && "0xfc".equals(key4)){
                                if(key2!=0){
                                    int value = (key3<<8)&0x0f00,value2 ;
                                    value2 = key2&0x00ff;
                                    value2 = value+value2;
                                    Log.e(TAG, "value1：" + value+"--value2:"+value2);

                                    voltages[vlenth++] =(((float)value2)/0x0fff)*(3.3f);
                                    if(vlenth>=BUFFER_SIZE){
                                        /*进行滤波去掉一个最大和最小值*/
                                        float[] floats = Filter.doFilter(voltages,vlenth);
                                        Message msg = new Message();
                                        msg.what = 0;
                                        msg.obj  = floats;
                                        handler.sendMessage(msg);
                                        vlenth = 0;
                                    }
                                    i=i+5;
                                }else{
                                    i++;
                                }
                            }



                        }

                        if (Thread.currentThread().isInterrupted()) {
                            Log.e(TAG, "关闭后台线程" + Thread.currentThread().getId());
                            socket.close();
                            break;
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }

        });

        thread.start();
    }

    public static void  close(){
        device = null;
        if(thread!=null){
            thread.isInterrupted();
        }
    }
}
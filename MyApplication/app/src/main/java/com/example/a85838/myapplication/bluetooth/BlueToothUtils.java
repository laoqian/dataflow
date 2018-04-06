package com.example.a85838.myapplication.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlueToothUtils {
    private static BluetoothDevice device;
    private static final String TAG="BlueToothUtils";
    private static BluetoothSocket socket;
    private static boolean isConnect = false;
    private static  Thread thread;
    private static Handler handler;

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
                    isConnect = true;
                    InputStream inputStream = socket.getInputStream();
                    while (true) {
                        byte[] buffer = new byte[1024];
                        int lenth = inputStream.read(buffer);

                        if(lenth>0){
                            Log.e(TAG, "接收到数据" + lenth);
                            Message msg = new Message();
                            List<Byte> list = new ArrayList<>(lenth);
                            for(int i=0;i<lenth;i++){
                                list.add(buffer[i]);
                            }

                            msg.obj = list;
                            handler.sendMessage(msg);
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
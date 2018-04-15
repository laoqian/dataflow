package com.example.a85838.myapplication.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static boolean pastType         = true;
    private static float[] volts_buffer     = new float[50];
    private static int buffer_pos           = 0;
    private static final int BUFFER_SIZE    = 5;

    public static boolean swtich(){
        pastType = !pastType;
        return  pastType;
    }

    private static char[] getChars (byte[] bytes) {
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);

        return cb.array();
    }

    private static float[]  pastParser(byte[] buffer,int length){
        byte[] bytes = Arrays.copyOf(buffer,length);
        char [] chars = getChars(bytes);
        try {

            String s = String.valueOf(chars);
            String[] voltStrings = s.split(",");
            float[] volts = new float[voltStrings.length];
            for(int i=0;i<voltStrings.length;i++){
                volts[i] = Float.parseFloat(voltStrings[i]);
            }

            return volts;
        }catch (Throwable ex){
            return null;
        }
    }

    private static float[] lastParser(byte[] buffer,int length){
        byte key0,key1,key4,key5;
        int key2,key3;
        int vlenth=0;
        float[] voltages = new float[200];

        for(int i=0;i<length-5;){
            key0 = buffer[i];
            key1 = buffer[i+1];
            key2 = buffer[i+2];
            key3 = buffer[i+3];
            key4 = buffer[i+4];
            key5 = buffer[i+5];

            if(key0==3 && key1==-4 && key4==-4 && key5==3 ){
                int value = (key3<<8)&0xff00,value2 ;
                value2 = key2&0x00ff;
                value2 = value|value2;
                voltages[vlenth++] = (((float)value2)/0x0fff)*(3.3f);
                i=i+5;
            }else{
                i++;
            }
        }

        return  Arrays.copyOf(voltages,vlenth);
    }

    private static void parser(byte[] buffer,int length){
        float[] volts  = pastType?pastParser(buffer,length):lastParser(buffer,length);
        if(volts!=null){
            volts_buffer[buffer_pos++] = Filter.doFilter(volts,volts.length);
            if(buffer_pos>=BUFFER_SIZE){
                float volt = Filter.doFilter(volts_buffer,buffer_pos);
                Message msg;
                msg = new Message();
                msg.what = 0;
                msg.obj = volt;
                handler.sendMessage(msg);
                buffer_pos = 0;
            }
        }
    }

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
                           parser(buffer,lenth);
                        }

                        if (Thread.currentThread().isInterrupted()) {
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
        device    = null;
        if(thread!=null){
            thread.isInterrupted();
        }
    }
}
package com.example.a85838.myapplication.bluetooth;


public class Filter{
    private static void sort(float[] filter_buf, int length) {
        int i, j;
        float filter_temp;

        // 采样值从小到大排列（冒泡法）
        for (j = 0; j < length - 1; j++) {
            for (i = 0; i < length - 1 - j; i++) {
                if (filter_buf[i] > filter_buf[i + 1]) {
                    filter_temp         = filter_buf[i];
                    filter_buf[i]       = filter_buf[i + 1];
                    filter_buf[i + 1]   = filter_temp;
                }
            }
        }
    }

    public static float[] doClean(float[] filter_buf, int length) {
        int i,minPos=0,maxPos=0;
        float max ,min;

        max = filter_buf[0];
        min = filter_buf[0];
        for (i = 0; i < length; i++) {
            if(filter_buf[i]>max){
                max = filter_buf[i];
                maxPos = i;
            }

            if(filter_buf[i]<min){
                min = filter_buf[i];
                minPos = i;
            }
        }

        float[] floats = new float[length-2];
        int pos = 0;
        for (i = 0; i < length; i++) {
            if(i==minPos || i== maxPos){
                continue;
            }

            floats[pos++] = filter_buf[i];
        }
        return floats;
    }

    public static float doFilter(float[] filter_buf, int length) {
        int i;
        float sum = 0;
        sort(filter_buf,length);
        for (i = 1; i < length - 1; i++) {
            sum += filter_buf[i];
        }

        return sum/(length-2);
    }
}

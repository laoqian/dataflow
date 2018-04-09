package com.example.a85838.myapplication.bluetooth;


public class Filter{
    /**
     *
     *A、名称：中位值平均滤波法（又称防脉冲干扰平均滤波法）
     *B、方法：
     *    采一组队列去掉最大值和最小值后取平均值，
     *    相当于“中位值滤波法”+“算术平均滤波法”。
     *    连续采样N个数据，去掉一个最大值和一个最小值，
     *    然后计算N-2个数据的算术平均值。
     *    N值的选取：3-14。
     *C、优点：
     *    融合了“中位值滤波法”+“算术平均滤波法”两种滤波法的优点。
     *    对于偶然出现的脉冲性干扰，可消除由其所引起的采样值偏差。
     *    对周期干扰有良好的抑制作用。
     *    平滑度高，适于高频振荡的系统。
     *D、缺点：
     *    计算速度较慢，和算术平均滤波法一样。
     *    比较浪费RAM。
     *E、整理：shenhaiyu 2013-11-01
    */
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


    public static float[] doFilter(float[] filter_buf, int length) {
        int i,j=0;
        float[] buffer = new float[length-2];

        sort(filter_buf,length);
        for (i = 1; i < length - 1; i++) {
            buffer[j++] = filter_buf[i];
        }

        return buffer;
    }
}

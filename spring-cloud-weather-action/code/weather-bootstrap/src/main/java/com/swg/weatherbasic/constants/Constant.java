package com.swg.weatherbasic.constants;

/**
 * @Author 【swg】.
 * @Date 2018/11/20 15:47
 * @DESC
 * @CONTACT 317758022@qq.com
 */
public class Constant {

    //定时更新天气的时间间隔
    public static final int JOB_INTERVAL = 60*30;//每半个小时更新一次

    //redis中天气数据的过期时间
    public static final long TIME_OUT = 60*60L;//过期时间为一个小时
}

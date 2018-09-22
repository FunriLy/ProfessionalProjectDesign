package com.qg.fangrui.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Time: Created by FunriLy on 2018/9/11.
 * Motto: From small beginnings comes great things.
 * Description:
 *      时间工具类
 * @author FunriLy
 */
public class CommonDateUtil {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前时间的时间戳
     * @return 时间戳
     */
    public static Long getStamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获得当前时间的时间戳 毫秒级别
     * @return 时间戳
     */
    public static Long getStampMillisecond() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间的Date对象
     * @return 当前时间的Date对象
     */
    public static Date getNowDate(){
        return new Date(System.currentTimeMillis());
    }

    /**
     * 获取当前时间的SimpleDateFormat对象
     * 加锁确保对象安全(另法：ThreadLocal)
     * @return 当前时间的SimpleDateFormat对象
     */
    public static synchronized String getNowFormat(){
        return SDF.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 将毫秒数转化为 Date
     * @param longtime long型时间参数
     * @return Date时间对象
     */
    public static Date changeLongtimeToDate(long longtime){
        return new Date(longtime);
    }
}

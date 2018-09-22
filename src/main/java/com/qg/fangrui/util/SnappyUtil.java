package com.qg.fangrui.util;

import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * Time: Created by FunriLy on 2018/9/20.
 * Motto: From small beginnings comes great things.
 * Description:
 *          Google Snappy 压缩算法
 * @author FunriLy
 */
public class SnappyUtil {

    public static  byte[] compressData(String data){
        try {
            return Snappy.compress(data.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static  String decompressData(byte[] bytes){
        try {
            return new String(Snappy.uncompress(bytes));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

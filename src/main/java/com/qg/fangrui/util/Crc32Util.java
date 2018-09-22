package com.qg.fangrui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

/**
 * Time: Created by FunriLy on 2018/9/17.
 * Motto: From small beginnings comes great things.
 * Description:
 *          CRC32 校验工具
 * @author FunriLy
 */
public class Crc32Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Crc32Util.class);

    /**
     * 对byte数组进行crc32校验计算
     * @param data byte数组
     * @param offset 起始地址
     * @param length 长度
     * @return crc校验码
     */
    public static long bytes(byte[] data, int offset, int length) {
        CRC32 crc = new CRC32();
        crc.update(data, offset, length);
        return crc.getValue();
    }

    /**
     * 对byte数组进行crc32校验计算
     * @param data byte数组
     * @return crc校验码
     */
    public static long bytes(byte[] data) {
        return bytes(data, 0, data.length);
    }

    /**
     * 对文件内容进行crc校验计算
     * @param file 文件对象
     * @return 文件crc校验码
     * @throws IOException 文件读取异常
     */
    public static long file(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        byte[] buff = new byte[64 * 1024];
        CRC32 crc = new CRC32();
        int len;
        try {
            while ((len = input.read(buff)) != -1) {
                crc.update(buff, 0, len);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return crc.getValue();
    }
}

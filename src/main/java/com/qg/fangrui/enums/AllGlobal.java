package com.qg.fangrui.enums;

import com.qg.fangrui.model.Disk;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Time: Created by FunriLy on 2018/9/16.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
public class AllGlobal {

    /**
     * 是否加密
     */
    public static boolean isEncryption = false;

    /**
     * 原始数据块
     */
    public static final int DATA_BLOCK_NUMBER = 6;

    /**
     * 编码数据库
     */
    public static final int EC_BLOCK_NUMBER = 2;

    /**
     * 编码后数据块数量
     */
    public static final int TOTAL_DATA_NUMBER = 8;

    /**
     * 盘符数量
     */
    public static final int DISK_NUMBER = 24;

    /**
     * 一个盘符的容量
     */
    public static final int DISK_CAPACITY = 3 * 64;

    /**
     * DiskFilePath
     */
    public static final String COMMON_FILE_PATH = "D://file//";

    /**
     * disk group 与 chunk number 之间的映射关系
     */
    public static HashMap<LinkedList<Disk>, AtomicInteger> groupMap = new HashMap<>();

    public static LinkedList<Disk> diskList1 = new LinkedList<>();
    public static LinkedList<Disk> diskList2 = new LinkedList<>();
    static {

        // 策略：6+2份数据，12选8，4个Jbod
        // 两个diskgroup：1-12、13-24
        diskList1.add(new Disk(1, 1));
        diskList1.add(new Disk(2, 1));
        diskList1.add(new Disk(3, 1));
        diskList1.add(new Disk(4, 2));
        diskList1.add(new Disk(5, 2));
        diskList1.add(new Disk(6, 2));
        diskList1.add(new Disk(7, 3));
        diskList1.add(new Disk(8, 3));
        diskList1.add(new Disk(9, 3));
        diskList1.add(new Disk(10, 4));
        diskList1.add(new Disk(11, 4));
        diskList1.add(new Disk(12, 4));
        diskList2.add(new Disk(13, 1));
        diskList2.add(new Disk(14, 1));
        diskList2.add(new Disk(15, 1));
        diskList2.add(new Disk(16, 2));
        diskList2.add(new Disk(17, 2));
        diskList2.add(new Disk(18, 2));
        diskList2.add(new Disk(19, 3));
        diskList2.add(new Disk(20, 3));
        diskList2.add(new Disk(21, 3));
        diskList2.add(new Disk(22, 4));
        diskList2.add(new Disk(23, 4));
        diskList2.add(new Disk(24, 4));

        groupMap.put(diskList1, new AtomicInteger(0));
        groupMap.put(diskList2, new AtomicInteger(0));

        // Collections.sort(diskList1);
    }

    public static void resetAllDisk(LinkedList<Disk> list){
        for (Disk disk : list) {
            disk.resetBeforeDistribution();
        }
    }
}

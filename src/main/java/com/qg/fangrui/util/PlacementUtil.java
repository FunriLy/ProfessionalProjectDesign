package com.qg.fangrui.util;

import com.qg.fangrui.enums.AllGlobal;
import com.qg.fangrui.exception.DiskGroupNotFoundException;
import com.qg.fangrui.exception.PlacementException;
import com.qg.fangrui.model.Disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Time: Created by FunriLy on 2018/9/16.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
public class PlacementUtil {

    /**
     * 从一个diskgroup中，12块磁盘中抽取8块
     * @return
     */
    public static ArrayList<Disk> placement() {
        ArrayList<Disk> array = new ArrayList<>();

        // 对目标进行复制
        LinkedList<Disk> tempDiskGroup = getTargetDiskGroup();
        if (tempDiskGroup == null) {
            throw new DiskGroupNotFoundException("找不到可分配的DiskGroup");
        }

        int[] number = {0, 0, 0, 0};
        int index = 0;
        Collections.sort(tempDiskGroup);
        while (index < tempDiskGroup.size() && tempDiskGroup.get(index).isValid()) {
            Disk disk = tempDiskGroup.get(index);
            index++;
            int n = (disk.getDiskId() % 13 - 1) / 3;
            if (number[n] < 2) {
                number[n] = number[n] + 1;
            } else {
                continue;
            }
            array.add(disk);

            if (array.size() >= AllGlobal.TOTAL_DATA_NUMBER) {
                break;
            }
        }

        if (array.size() < AllGlobal.TOTAL_DATA_NUMBER) {
            throw new PlacementException("磁盘分配不足");
        }

        return array;
    }

    /**
     *
     * @return
     */
    public static LinkedList<Disk> getTargetDiskGroup() {
        LinkedList<Disk> target = null;
        int chunkNumber = Integer.MAX_VALUE;
        Map.Entry<LinkedList<Disk>, AtomicInteger> kv = null;
        for (Map.Entry<LinkedList<Disk>, AtomicInteger> entry : AllGlobal.groupMap.entrySet()) {
            if (chunkNumber > entry.getValue().get()) {
                chunkNumber = entry.getValue().get();
                kv = entry;
            }
        }
        if (kv != null) {
            kv.getValue().incrementAndGet();
            return kv.getKey();
        }
        return null;
    }

    public static void main(String[] args) {
        ArrayList<Disk> arrayList = placement();
        System.out.println("===========");
        for (Disk disk : arrayList) {
            System.out.println(disk.getDiskId() + " " + disk.getFree());
            disk.reduceStorage();
        }
        for (int i=0; i< 10; i++) {
            System.out.println("===========");
            arrayList = placement();
            for (Disk disk : arrayList) {
                System.out.println(disk.getDiskId() + " " + disk.getFree());
                disk.reduceStorage();
            }
        }
        System.out.println("===========");
    }
}

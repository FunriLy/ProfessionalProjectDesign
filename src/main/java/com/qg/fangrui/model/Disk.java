package com.qg.fangrui.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Time: Created by FunriLy on 2018/9/16.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
@Setter
@Getter
public class Disk implements Comparable<Disk> {
    private int diskId;
    private int jbodId;
    private int placeInJbod;
    /**
     * 盘符剩余空间
     */
    private int free;
    private boolean isValid;

    public Disk(){

    }

    public Disk(int diskId, int jbodId) {
        this.diskId = diskId;
        this.jbodId = jbodId;
        this.free = 100;
        this.placeInJbod = 0;
        this.isValid = true;
    }

    /**
     * 在分配磁盘前进行重置，重置分配策略
     */
    public void resetBeforeDistribution() {
        this.placeInJbod = 0;
    }

    public void reduceStorage() {
        if (this.free <= 0) {
            this.isValid = false;
        }
        this.free--;
    }

    /**
     * 判断该磁盘是否可用
     * @return 若可用返回true，否则返回false.
     */
    public boolean isValid() {
        return isValid;
    }

    @Override
    public int compareTo(Disk o) {
        if (free > o.free) {
            return -1;
        } else if (free < o.free) {
            return 1;
        }
        return 0;
    }
}

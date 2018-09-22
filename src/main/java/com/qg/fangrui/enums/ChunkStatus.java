package com.qg.fangrui.enums;

/**
 * Time: Created by FunriLy on 2018/9/19.
 * Motto: From small beginnings comes great things.
 * Description:
 *          Chunk 状态
 * @author FunriLy
 */
public enum ChunkStatus {
    /**
     * 等待删除
     */
    Detele(-1),
    /**
     * 原生Chunk
     */
    Native(0),
    /**
     * 正常Chunk
     */
    Normal(1),
    /**
     * 需要修复Chunk
     */
    Repair(2),
    /**
     * 不定状态
     */
    Uncertain(3)
    ;

    private int status;
    ChunkStatus(int status) {
        this.status = status;
    }
    public int getStatus() {
        return status;
    }
}

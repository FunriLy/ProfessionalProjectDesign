package com.qg.fangrui.model;

import lombok.Data;

/**
 * Time: Created by FunriLy on 2018/9/17.
 * Motto: From small beginnings comes great things.
 * Description:
 *          Chunk 将被切分为多份 Replica
 *          一份 Replica 对应一个 Disk
 * @author FunriLy
 */
@Data
public class Replica {
    private long chunkId;
    private int replicaId;
    private long createTime;
    private byte[] data;
    private long crc32;
    private int diskId;
    /**
     * 标记读写replica是否成功
     */
    private boolean isSuccess;
    /**
     * 标志一份replica的有效长度
     */
    private int length;

    public String getMessage() {
        return "Replica{" +
                "chunkId=" + chunkId +
                ", replicaId=" + replicaId +
                ", diskId=" + diskId +
                ", length=" + length +
                '}';
    }



}

package com.qg.fangrui.model;

import lombok.Data;

/**
 * Time: Created by FunriLy on 2018/9/16.
 * Motto: From small beginnings comes great things.
 * Description:
 *          存储数据的实体
 * @author FunriLy
 */
@Data
public class Chunk {

    private long chunkId;
    private long createTime;
    private String data;
    private long crc32;
    /**
     * 标记Chunk的当前状态
     *
     */
    private int status;
    /**
     * 存储Chunk所在disk盘符号
     */
    private String disks;
}

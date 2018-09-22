package com.qg.fangrui.enums;

/**
 * Time: Created by FunriLy on 2018/9/11.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
public enum  StatusEnum {

    /**
     * 通用模块
     */
    ILLEGAL_PARAMETER(-1, "非法参数"),
    /**
     * Chunk 模块
     */
    CHUNK_CREATE_SUCCESS(1, "创建Chunk成功"),
    CHUNK_CREATE_FAIL(0, "创建Chunk失败"),
    CHUNK_GET_SUCCESS(1, "查找Chunk成功"),
    CHUNK_GET_FAIL(0, "查找Chunk失败"),
    CHUNK_GET_NOTFOUND(0, "目标Chunk不存在"),
    CHUNK_DELETE_SUCCESS(1, "删除Chunk成功"),
    CHUNK_LIST(1, "获取Chunk列表");

    private  int state;
    private  String stateInfo;

    StatusEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }
    public  static  StatusEnum statOf(int index) {
        for (StatusEnum state : values()) {
            if (state.getState() == index) {
                return  state;
            }
        }
        return  null;
    }
}

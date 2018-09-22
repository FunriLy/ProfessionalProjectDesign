package com.qg.fangrui.util;

import com.qg.fangrui.core.SnowFlake;
import org.apache.commons.lang3.RandomUtils;

/**
 * Time: Created by FunriLy on 2018/9/17.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
public class DistributedIDUtil {

    private static final SnowFlake SNOW_FLAKE =
            new SnowFlake(RandomUtils.nextInt(1, 10),RandomUtils.nextInt(1, 10));

    /**
     * 获取下一个 Chunk Id
     * @return 新的 Chunk Id
     */
    public static long getChunkId() {
        return SNOW_FLAKE.nextId();
    }

    /**
     * 更新 ID 生成器设置
     * @param dataCenterId 数据节点标识ID
     * @param machineId 机器标识ID
     */
    public static void updateIdConfig(int dataCenterId, int machineId) {
        SNOW_FLAKE.updateConfig(dataCenterId, machineId);
    }
}

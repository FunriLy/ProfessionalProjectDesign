package com.qg.fangrui.util;

import com.qg.fangrui.enums.AllGlobal;
import com.qg.fangrui.model.Replica;
import com.xiaomi.infra.ec.ErasureCodec;

import java.util.List;

/**
 * Time: Created by FunriLy on 2018/9/26.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
public class ErasureCodecUtil {

    private static ErasureCodec codec = new ErasureCodec.Builder(ErasureCodec.Algorithm.Reed_Solomon)
            .dataBlockNum(AllGlobal.DATA_BLOCK_NUMBER)
            .codingBlockNum(AllGlobal.EC_BLOCK_NUMBER)
            .wordSize(AllGlobal.DISK_CAPACITY)
            .build();

    public static byte[][] encode(List<Replica> replicaList) {
        byte[][] data = new byte[AllGlobal.DATA_BLOCK_NUMBER][AllGlobal.DISK_CAPACITY];
        if (replicaList.size() != AllGlobal.DATA_BLOCK_NUMBER) {
            throw new RuntimeException("EC 编码失败：Replica 数量不足");
        }
        for (int r=0; r<data.length; r++) {
            System.arraycopy(replicaList.get(r).getData(), 0,
                    data[r], 0, data[r].length);
        }
        return codec.encode(data);
    }

    public static void decode(int[] erasures, List<Replica> replicaList) {
        int index = 0;
        if (replicaList.size() != AllGlobal.TOTAL_DATA_NUMBER) {
            throw new RuntimeException("EC 解码失败：Replica 数量不足");
        }
        // 抽取出 data 数据
        byte[][] data = new byte[AllGlobal.DATA_BLOCK_NUMBER][AllGlobal.DISK_CAPACITY];
        for (int r=0; r<data.length; r++) {
            System.arraycopy(replicaList.get(index).getData(), 0,
                    data[r], 0, data[r].length);
            index++;
        }
        // 抽取出 coding 数据
        byte[][] coding = new byte[AllGlobal.EC_BLOCK_NUMBER][AllGlobal.DISK_CAPACITY];
        for (int r=0; r<coding.length; r++) {
            System.arraycopy(replicaList.get(index).getData(), 0,
                    coding[r], 0, coding[r].length);
            index++;
        }
        // 解码
        codec.decode(erasures, data, coding);

        index = 0;
        for (int r=0; r<data.length; r++) {
            replicaList.get(index).setData(data[r]);
            index++;
        }
        for (int r=0; r<coding.length; r++) {
            replicaList.get(index).setData(coding[r]);
            index++;
        }
    }
}

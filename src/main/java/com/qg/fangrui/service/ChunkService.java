package com.qg.fangrui.service;

import com.qg.fangrui.dao.ChunkDao;
import com.qg.fangrui.enums.AllGlobal;
import com.qg.fangrui.enums.ChunkStatus;
import com.qg.fangrui.exception.ChunkNotFoundException;
import com.qg.fangrui.exception.CrcCheckSumFaileException;
import com.qg.fangrui.model.Chunk;
import com.qg.fangrui.model.Disk;
import com.qg.fangrui.model.Replica;
import com.qg.fangrui.util.CommonDateUtil;
import com.qg.fangrui.util.Crc32Util;
import com.qg.fangrui.util.PlacementUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Time: Created by FunriLy on 2018/9/16.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
@Service
public class ChunkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkService.class);

    private ReplicaService replicaService;
    private ChunkDao chunkDao;

    @Autowired
    public ChunkService(ReplicaService replicaService, ChunkDao chunkDao) {
        this.replicaService = replicaService;
        this.chunkDao = chunkDao;
    }

    public List<Chunk> getAllChunk() {
        List<Chunk> list = null;
        try {
            list = chunkDao.getNormalChunkMessage();
        } catch (Exception e) {
            LOGGER.warn("获取Chunk列表未知错误 " + e.getMessage(), e);
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    /**
     * 将 Chunk 写入盘符
     * @param chunk Chunk 实体
     * @return 成功返回 true 否则返回 false
     */
    public boolean createChunk(Chunk chunk) {
        boolean isSuccess = true;
        long crc = Crc32Util.bytes(chunk.getData().getBytes());
        LOGGER.info("Chunk real Crc " + crc);
        chunk.setCrc32(crc);
        // 获得分配的Disk磁盘组
        List<Disk> diskList = PlacementUtil.placement();
        // 获得分割后的Replica组 这里隐性确保了Disk磁盘组数量为8
        List<Replica> replicaList = replicaService.splitReplica(chunk, diskList);
        // 写入磁盘
        CountDownLatch count = new CountDownLatch(AllGlobal.TOTAL_DATA_NUMBER);
        for (int i = 0; i < AllGlobal.TOTAL_DATA_NUMBER; i++) {
            replicaService.writeReplicaTask(replicaList.get(i), count);
        }

        try {
            count.await();
        } catch (InterruptedException e) {
            isSuccess = false;
            LOGGER.warn("Chunk写入磁盘 计数器被中断 ", e.getMessage());
        }

        // 检查每一份Replica是否被成功写入
        StringBuilder disks = new StringBuilder();
        for (Replica replica : replicaList) {
            if (!replica.isSuccess()) {
                isSuccess = false;
                break;
            }
            disks.append(replica.getDiskId()).append("_");
        }

        // 把Chunk信息写入数据库
        if (isSuccess) {
            // 更新 Disk 存储量
            for (Disk disk : diskList) {
                disk.reduceStorage();
            }
            LOGGER.info("Chunk ID " + chunk.getChunkId()
                    + " Date " + CommonDateUtil.changeLongtimeToDate(chunk.getCreateTime()));
            // 去掉最后一个无用字符
            disks.deleteCharAt(disks.length() - 1);
            chunk.setDisks(disks.toString());
            chunkDao.createChunk(chunk);
        }
        return isSuccess;
    }

    public Chunk getChunkById(long chunkId, boolean isRecycling) {
        int damageNum = 0;
        Chunk chunk = chunkDao.getChunkByChunkId(chunkId);
        boolean isSuccess = true;
        if (chunk == null) {
            throw new ChunkNotFoundException("Chunk 不存在！");
        } else if (chunk.getStatus() == ChunkStatus.Detele.getStatus()) {
            if (isRecycling) {
                chunkDao.updateChunkStatus(chunkId, ChunkStatus.Uncertain.getStatus());
                // TODO 为了逻辑通顺 这一步反而有点瑕疵
                Chunk recoveryChunk = getChunkById(chunkId, false);
                if (recoveryChunk != null) {
                    chunkDao.updateChunkStatus(chunkId, ChunkStatus.Normal.getStatus());
                }
            } else {
                throw new ChunkNotFoundException("Chunk 不存在！");
            }
        }

        // 正常复原Chunk
        List<Replica> replicaList = new ArrayList<>();
        String[] diskIdList = chunk.getDisks().split("_");
        CountDownLatch count = new CountDownLatch(AllGlobal.TOTAL_DATA_NUMBER);
        for (int i = 0; i < AllGlobal.TOTAL_DATA_NUMBER; i++) {
            Replica replica = new Replica();
            replica.setChunkId(chunkId);
            // ReplicaId 从 1 开始计数
            replica.setReplicaId(i + 1);
            replica.setCreateTime(chunk.getCreateTime());
            replica.setDiskId(Integer.valueOf(diskIdList[i]));
            replicaList.add(replica);

            replicaService.readReplicaTask(replica, count);
        }

        try {
            count.await();
        } catch (InterruptedException e) {
            isSuccess = false;
            LOGGER.warn("Chunk读取磁盘 计数器被中断 ", e.getMessage());
        }

        // 检查每一份Replica是否被成功写入
        for (Replica replica : replicaList) {
            if (!replica.isSuccess()) {
                isSuccess = false;
                // 损坏副本数量 + 1
                damageNum++;
            }
        }

        // 如果有副本损坏 且损坏数目小于等于EC编码块
        if (!isSuccess && damageNum <= AllGlobal.EC_BLOCK_NUMBER) {
            replicaService.replicaRebuild(replicaList);
            isSuccess = true;
        }

        // 确保每份 CRC 校验成功
        if (isSuccess) {
            byte[] bytes = replicaService.replicaMerge(replicaList);
            // 校验 Crc
            String dataStr = new String(bytes).replaceAll("\u0000", "").replaceAll("\\u0000", "");
            long crc = Crc32Util.bytes(dataStr.getBytes());
            if (crc != chunk.getCrc32()) {
                LOGGER.info("Crc " + crc);
                LOGGER.info("chunkCrc " + chunk.getCrc32());
                LOGGER.warn("Chunk Message" + chunk.toString());
                throw new CrcCheckSumFaileException("Chunk 校验 CRC 失败！");
            }
            chunk.setData(dataStr);
        }
        return chunk;
    }

    /**
     * 删除Chunk->更新Chunk状态，具体回收由GC后台线程执行
     * @param chunkId chunk id
     * @return 若更新成功返回 true 否则返回 false
     */
    public boolean deleteChunkById(long chunkId) {
        int result = chunkDao.updateChunkStatus(chunkId, ChunkStatus.Detele.getStatus());
        return result == 1;
    }

    /**
     * 判断Chunk是否存在
     * @param chunkId chunk id
     * @return 若存在返回 true 否则返回 false
     */
    public boolean isChunkExist(long chunkId) {
        Chunk chunk = chunkDao.getChunkByChunkId(chunkId);
        return chunk != null;
    }


}

package com.qg.fangrui.dao;

import com.qg.fangrui.model.Chunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Time: Created by FunriLy on 2018/9/16.
 * Motto: From small beginnings comes great things.
 * Description:
 *          Chunk Dao 层操作
 * @author FunriLy
 */
@Mapper
@Repository
public interface ChunkDao {

    /**
     * 创建一个Chunk
     * @param chunk Chunk实体
     * @return 创建成功返回 1，否则返回 0
     */
    int createChunk(@Param("chunk")Chunk chunk);

    /**
     * 根据 ChunkId 获取 Chunk 实体
     * @param chunkId Chunk Id
     * @return Chunk 实体
     */
    Chunk getChunkByChunkId(@Param("chunkId") long chunkId);

    /**
     * 根据 ChunkId 删除 Chunk 实体
     * @param chunkId Chunk Id
     * @return 成功返回 1 否则返回 0
     */
    int deleteChunk(@Param("chunkId") long chunkId);

    /**
     * 更新 Chunk 实体状态
     * @param chunkId Chunk Id
     * @param status 状态码
     * @return 成功返回 1 否则返回 0
     */
    int updateChunkStatus(@Param("chunkId") long chunkId,
                          @Param("status") int status);

    /**
     * 获得所有 Normal Chunk
     * @return 正常 Chunk 集合
     */
    List<Chunk> getNormalChunkMessage();

    /**
     * 获得所有移除 Chunk Id
     * @return 移除 Chunk Id 集合
     */
    List<Long> getAllDeleteChunkId();
}

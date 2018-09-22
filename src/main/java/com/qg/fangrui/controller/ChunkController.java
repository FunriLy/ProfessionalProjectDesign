package com.qg.fangrui.controller;

import com.qg.fangrui.dto.ResultInfo;
import com.qg.fangrui.enums.ChunkStatus;
import com.qg.fangrui.enums.StatusEnum;
import com.qg.fangrui.exception.ChunkNotFoundException;
import com.qg.fangrui.model.Chunk;
import com.qg.fangrui.service.ChunkService;
import com.qg.fangrui.util.CommonDateUtil;
import com.qg.fangrui.util.DistributedIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Time: Created by FunriLy on 2018/9/18.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
@RestController
@RequestMapping(value = "/chunk")
public class ChunkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkController.class);

    private ChunkService chunkService;

    @Autowired
    public ChunkController(ChunkService chunkService) {
        this.chunkService = chunkService;
    }

    @RequestMapping("/test")
    public boolean test() {
        String data = "12345678901234567890";
        Chunk chunk = new Chunk();
        chunk.setChunkId(DistributedIDUtil.getChunkId());
        chunk.setCreateTime(CommonDateUtil.getStampMillisecond());
        chunk.setStatus(ChunkStatus.Normal.getStatus());
        chunk.setData(data);

        chunkService.createChunk(chunk);

        Chunk copyChunk = chunkService.getChunkById(chunk.getChunkId(), false);
        System.out.println(copyChunk.getData());
        return data.equals(copyChunk.getData());
    }

    @RequestMapping("/getchunk")
    public String get() {
        long chunkId = Long.valueOf("220871000663941120");
        Chunk chunk = chunkService.getChunkById(chunkId, false);
        return chunk.getData();
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResultInfo<Chunk> create(@RequestParam(value = "data") String data) {
        try {
            Chunk chunk = new Chunk();
            chunk.setChunkId(DistributedIDUtil.getChunkId());
            chunk.setCreateTime(CommonDateUtil.getStampMillisecond());
            chunk.setStatus(ChunkStatus.Normal.getStatus());
            chunk.setData(data);

            if (chunkService.createChunk(chunk)) {
                // 创建 Chunk 成功
                chunk.setData("");
                return new ResultInfo<>(StatusEnum.CHUNK_CREATE_SUCCESS, chunk);
            }
        } catch (Exception e) {
            LOGGER.warn("创建 Chunk 发生未知错误", e.getMessage());
        }
        return new ResultInfo<>(StatusEnum.CHUNK_CREATE_FAIL);
    }

    @RequestMapping(value = "/get/{chunkId}/{command}", method = RequestMethod.GET)
    public ResultInfo<Chunk> getChunk(@PathVariable String chunkId,
                                      @PathVariable String command) {
        ResultInfo<Chunk> result;
        try {
            boolean isSave = (Integer.valueOf(command) == 1);
            long chunkid = Long.valueOf(chunkId);
            // 默认关闭强制找回
            Chunk chunk = chunkService.getChunkById(chunkid, isSave);
            result = new ResultInfo<>(StatusEnum.CHUNK_GET_SUCCESS, chunk);
        } catch (NumberFormatException e) {
            result = new ResultInfo<>(StatusEnum.ILLEGAL_PARAMETER);
        } catch (ChunkNotFoundException e) {
            result = new ResultInfo<>(StatusEnum.CHUNK_GET_NOTFOUND);
        } catch (Exception e) {
            result = new ResultInfo<>(StatusEnum.CHUNK_GET_FAIL);
        }
        return result;
    }

    @RequestMapping(value = "/delete/{chunkId}", method = RequestMethod.GET)
    public ResultInfo<?> deleteChunk(@PathVariable String chunkId){
        ResultInfo<?> result;
        try {
            long chunkid = Long.valueOf(chunkId);
            if(chunkService.isChunkExist(chunkid) && chunkService.deleteChunkById(chunkid)) {
                result = new ResultInfo<>(StatusEnum.CHUNK_DELETE_SUCCESS);
            } else {
                result = new ResultInfo<>(StatusEnum.CHUNK_GET_NOTFOUND);
            }
        } catch (NumberFormatException e) {
            result = new ResultInfo<>(StatusEnum.ILLEGAL_PARAMETER);
        }
        return result;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResultInfo<List<Chunk>> list() {
        return new ResultInfo<>(StatusEnum.CHUNK_LIST, chunkService.getAllChunk());
    }
}

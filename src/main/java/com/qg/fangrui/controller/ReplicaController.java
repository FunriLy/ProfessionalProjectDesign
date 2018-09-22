package com.qg.fangrui.controller;

import com.qg.fangrui.model.Replica;
import com.qg.fangrui.service.ReplicaService;
import com.qg.fangrui.util.CommonDateUtil;
import com.qg.fangrui.util.Crc32Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

/**
 * Time: Created by FunriLy on 2018/9/17.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
@RestController
public class ReplicaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicaController.class);

    private ReplicaService replicaService;

    @Autowired
    public ReplicaController(ReplicaService replicaService) {
        this.replicaService = replicaService;
    }

    @RequestMapping("/replica")
    public String readReplica() {
        CountDownLatch count = new CountDownLatch(1);
        LOGGER.info("开始写入 replica");
        Replica replica = new Replica();
        replica.setChunkId(10001);
        replica.setReplicaId(1);
        replica.setDiskId(1);
        replica.setCreateTime(CommonDateUtil.getStampMillisecond());
        replica.setData("这个是一份Replica数据".getBytes());
        replica.setCrc32(Crc32Util.bytes(replica.getData()));
        replica.setLength(replica.getData().length);
        replica.setSuccess(false);
        replicaService.writeReplicaTask(replica, count);
        try {
            count.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("写入结束 replica");

        LOGGER.info("开始读取 replica");
        CountDownLatch readCount = new CountDownLatch(1);
        Replica readReplica = new Replica();
        readReplica.setChunkId(10001);
        readReplica.setDiskId(1);
        readReplica.setReplicaId(1);
        replicaService.readReplicaTask(readReplica, readCount);
        try {
            readCount.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Data : " + new String(readReplica.getData()));
        LOGGER.info("读取结束 replica");
        return "info : " + replica.isSuccess() + " " + readReplica.isSuccess();
    }
}

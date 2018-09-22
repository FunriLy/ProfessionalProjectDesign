package com.qg.fangrui.service;

import com.qg.fangrui.enums.AllGlobal;
import com.qg.fangrui.exception.CrcCheckSumFaileException;
import com.qg.fangrui.exception.DiskFileNotFoundException;
import com.qg.fangrui.exception.PlacementException;
import com.qg.fangrui.model.Chunk;
import com.qg.fangrui.model.Disk;
import com.qg.fangrui.model.Replica;
import com.qg.fangrui.util.CommonDateUtil;
import com.qg.fangrui.util.Crc32Util;
import com.qg.fangrui.util.DistributedIDUtil;
import com.qg.fangrui.util.SnappyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Time: Created by FunriLy on 2018/9/17.
 * Motto: From small beginnings comes great things.
 * Description:
 *
 * @author FunriLy
 */
@Service
public class ReplicaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicaService.class);

    @Async("asyncReplicaExecutor")
    public void readReplicaTask(Replica replica, CountDownLatch count) {
        replica.setSuccess(false);
        LOGGER.info("开始读取Replica " + replica.getMessage());
        String path = AllGlobal.COMMON_FILE_PATH + "MokeFileDisk" + replica.getDiskId() + ".txt";
        int index = 1;
        String chunkStr = String.valueOf(replica.getChunkId());
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                return;
            }
            BufferedReader reader =new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "utf-8"));
            String str;
            while ((str = reader.readLine()) != null) {
                if (index % 2 == 1 && str.startsWith(chunkStr)) {
                    String[] messages = str.split(" ");
                    replica.setCreateTime(Long.valueOf(messages[1]));
                    replica.setCrc32(Long.valueOf(messages[3]));
                    replica.setLength(Integer.valueOf(messages[4]));

                    String data = reader.readLine();
                    index++;
                    replica.setData(dataAntiFormat(data));

                    //校验CRC数据
                    long replicaCrc = Crc32Util.bytes(replica.getData());
                    if (replica.getCrc32() != replicaCrc) {
                        LOGGER.error("Crc32 校验数据失败 : " + replica.getMessage());
                        LOGGER.error("Disk CRC32 "+ replica.getCrc32());
                        LOGGER.error("Calculation CRC32 " + replicaCrc);
                        throw new CrcCheckSumFaileException("Crc32 校验数据失败");
                    }
                    LOGGER.info("数据盘 " + replica.getReplicaId() + " " + data.getBytes().length
                            + "   " + Arrays.toString(data.getBytes()));
                    replica.setSuccess(true);
                    // break; 以后一份Replica为准 可能是垃圾回收慢
                }
                index++;
            }
        } catch (IOException e) {
            LOGGER.error("读取Replica失败 " + replica.getMessage());
            LOGGER.error("失败消息 " + e.getMessage());
        } finally {
            count.countDown();
        }
        LOGGER.info("成功读取Replica " + replica.getMessage());
        LOGGER.info("Replica 长度 " + replica.getLength());
    }

    @Async("asyncReplicaExecutor")
    public void writeReplicaTask(Replica replica, CountDownLatch count) {
        replica.setSuccess(false);
        LOGGER.info("开始写入Replica " + replica.getMessage());
        String path = AllGlobal.COMMON_FILE_PATH +   "MokeFileDisk" + replica.getDiskId() + ".txt";
        String replicaMessage = replica.getChunkId() +" " + replica.getCreateTime() + " "
                + replica.getReplicaId() + " " + replica.getCrc32() + " "
                + replica.getLength() + System.lineSeparator();
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                boolean result = file.getParentFile().mkdirs();
                if (!result) {
                    LOGGER.error("创建 Disk File 失败! Path : " + path);
                    throw new DiskFileNotFoundException("创建 Disk File 失败！");
                }
            }
            FileOutputStream output  =new FileOutputStream(file, true);
            output.write(replicaMessage.getBytes());
            // 对 byte 数组格式化后写入
            output.write(dataFormat(replica.getData()).getBytes());
            output.write(System.lineSeparator().getBytes());
            output.close();
            LOGGER.info("Replica Data : " + new String(replica.getData()));
            replica.setSuccess(true);
        } catch (IOException e) {
            LOGGER.error("写入Replica失败 " + replica.getMessage());
            LOGGER.error("失败消息 " + e.getMessage());
            LOGGER.info("Replica Data : " + new String(replica.getData()));
        } finally {
            count.countDown();
        }
        LOGGER.info("写入Replica成功" + replica.getMessage());
    }


    public List<Replica> splitReplica(Chunk chunk, List<Disk> diskList) {
        if (diskList.size() < AllGlobal.TOTAL_DATA_NUMBER) {
            throw new PlacementException("磁盘分配错误");
        }
        List<Replica> replicaList = new ArrayList<>(8);
        byte[] bytes = chunk.getData().getBytes();
        for (int i=1; i<=AllGlobal.TOTAL_DATA_NUMBER; i++) {
            Replica replica = new Replica();
            replica.setChunkId(chunk.getChunkId());
            replica.setReplicaId(i);
            replica.setCreateTime(chunk.getCreateTime());
            replica.setDiskId(diskList.get(i-1).getDiskId());

            int length = AllGlobal.DISK_CAPACITY, start = 0;
            boolean isFill = false;
            if ((i-1)*AllGlobal.DISK_CAPACITY < bytes.length) {
                start = (i-1) * AllGlobal.DISK_CAPACITY;
                isFill = true;
            }
            byte[] data = new byte[AllGlobal.DISK_CAPACITY];
            // 需要填充数据
            if (isFill) {
                length = (length <= (bytes.length-start) ? AllGlobal.DISK_CAPACITY : (bytes.length-start));
                System.arraycopy(bytes, start, data, 0, length);
                replica.setLength(length);
            } else {
                replica.setLength(0);
            }
            replica.setData(data);
            replica.setCrc32(Crc32Util.bytes(data));
            replicaList.add(replica);
        }
        return replicaList;
    }

    public byte[] replicaMerge(List<Replica> replicaList) {
        if (replicaList.size() < AllGlobal.TOTAL_DATA_NUMBER) {
            return null;
        }
        return byteMergeAllReplicaData(
                replicaList.get(0).getData(),
                replicaList.get(1).getData(),
                replicaList.get(2).getData(),
                replicaList.get(3).getData(),
                replicaList.get(4).getData(),
                replicaList.get(5).getData(),
                replicaList.get(6).getData(),
                replicaList.get(7).getData()
        );
    }

    private byte[] byteMergeAllReplicaData(byte[]...values) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for(int i=0; i<values.length; i++) {
            try {
                bos.write(values[i]);
            } catch (IOException e) {
                LOGGER.warn("合并byte数组出错 " + e.getMessage());
                e.printStackTrace();
            }
        }
        return bos.toByteArray();
    }

    /**
     * 对 byte 数组进行格式化
     * @param data
     * @return
     */
    private String dataFormat(byte[] data) {
        StringBuilder builder = new StringBuilder("");
        if (data != null && data.length > 0) {
            for (int i=0; i<data.length; i++) {
                builder.append(data[i]).append("_");
            }
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * 解格式化为 byte 数组
     * @param dataStr
     * @return
     */
    private byte[] dataAntiFormat(String dataStr) {
        byte[] data = new byte[AllGlobal.DISK_CAPACITY];
        String[] byteList= dataStr.split("_");
        if (AllGlobal.DISK_CAPACITY < byteList.length) {
            return data;
        }
        for (int i=0; i<byteList.length; i++) {
            data[i] = Byte.valueOf(byteList[i]);
        }
        return data;
    }
}

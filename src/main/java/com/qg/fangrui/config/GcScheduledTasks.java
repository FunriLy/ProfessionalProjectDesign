package com.qg.fangrui.config;

import com.qg.fangrui.dao.ChunkDao;
import com.qg.fangrui.enums.AllGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Time: Created by FunriLy on 2018/9/20.
 * Motto: From small beginnings comes great things.
 * Description:
 *          GC 回收空间
 * @author FunriLy
 */
@Component
public class GcScheduledTasks {

    private ChunkDao chunkDao;

    @Autowired
    public GcScheduledTasks(ChunkDao chunkDao) {
        this.chunkDao = chunkDao;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GcScheduledTasks.class);

    /**
     * 每天晚上 11 点触发 GC 任务
     */
    @Scheduled(cron="0 0 23 * * ?")
    public void gcTask() {
        // TODO : Mock情况下需要考虑一下并发访问问题，但是在实际场景中通常采用 one-to-one 线程模型 可堵塞了该方法
        for (int i=1; i<= AllGlobal.DISK_NUMBER; i++) {
            String oldPath = AllGlobal.COMMON_FILE_PATH + "MokeFileDisk" + i + ".txt";
            String newPath = AllGlobal.COMMON_FILE_PATH + "copy_MokeFileDisk" + i + ".txt";
            List<Long> chunkIdList = chunkDao.getAllDeleteChunkId();
            if (chunkIdList.isEmpty()) {
                continue;
            } else {
                for (long id : chunkIdList) {
                    // 对于单机情况下，这里可以改为批量删除
                    // 考虑到分布式数据一致性问题时，这里不能采用批量操作
                    chunkDao.deleteChunk(id);
                }
            }
            Set<Long> chunkIdSet = new HashSet<>(chunkIdList);
            if (copyFile(oldPath, newPath, chunkIdSet)) {
                // 更换文件
                File oldFile = new File(oldPath);
                if (oldFile.delete()) {
                    // 删除文件成功才执行文件重命名
                    File newFile = new File(newPath);
                    if (!newFile.renameTo(new File(oldPath))) {
                        LOGGER.warn("文件重命名失败 NewPath : " + newPath);
                    }
                } else {
                    LOGGER.warn("旧文件删除失败 OldPath : " + oldPath);
                }
                LOGGER.info("后台线程定时清除GC文件 DiskId : " + i);
            } else {
                LOGGER.info("后台清理线程没有更新定时任务 DiskId : " + i);
            }
        }
    }

    private boolean copyFile(String oldPath, String newPath, Set<Long> chunkIdSet) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        boolean isSuccess;
        if (!isFileExist(oldFile) || !isFileExist(newFile)) {
            return false;
        }

        int count = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(oldFile));
            FileOutputStream output  =new FileOutputStream(newFile, true);
            String text;
            while ((text = reader.readLine()) != null) {
                String[] messages = text.split(" ");
                if (chunkIdSet.contains(Long.valueOf(messages[0]))) {
                    // 找到需要删除的行 跳过
                    count++;
                    reader.readLine();
                } else {
                    output.write(text.getBytes());
                    text = reader.readLine();
                    output.write(text.getBytes());
                }
            }
            reader.close();
            output.close();
            isSuccess = true;
        } catch (IOException e) {
            //在空状态下，不会对文件进行回收
            isSuccess = false;
        }
        if (count == 0) {
            // 若无需更改
            isSuccess = false;
        }
        return isSuccess;
    }

    private boolean isFileExist(File file) {
        boolean isExist = true;
        if(!file.exists())
        {
            try {
                isExist = file.createNewFile();
            } catch (IOException e) {
                LOGGER.warn("File is Not Exist, Path : " + file.getPath());
                LOGGER.warn("Error Message : " + e.getMessage());
                e.printStackTrace();
                isExist = false;
            }
        }
        return isExist;
    }
}

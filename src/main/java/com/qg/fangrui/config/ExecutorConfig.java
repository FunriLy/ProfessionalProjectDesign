package com.qg.fangrui.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Time: Created by FunriLy on 2018/9/17.
 * Motto: From small beginnings comes great things.
 * Description:
 *          线程池配置
 * @author FunriLy
 */
@Configuration
@EnableAsync
public class ExecutorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorConfig.class);

    @Bean
    public Executor asyncReplicaExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 配置核心线程数
        executor.setCorePoolSize(5);
        // 配置最大线程数
        executor.setMaxPoolSize(12);
        // 配置队列大小
        executor.setQueueCapacity(28);
        // 配置线程池中线程前缀
        executor.setThreadNamePrefix("read-write-replica-thread-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        return executor;
    }
}

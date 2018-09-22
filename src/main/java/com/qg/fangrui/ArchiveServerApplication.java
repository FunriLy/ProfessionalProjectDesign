package com.qg.fangrui;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Time: Created by FunriLy on 2018/9/11.
 * Motto: From small beginnings comes great things.
 * Description:
 *		控制主类
 * @author FunriLy
 */

@SpringBootApplication
@EnableScheduling
public class ArchiveServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchiveServerApplication.class, args);
    }
}

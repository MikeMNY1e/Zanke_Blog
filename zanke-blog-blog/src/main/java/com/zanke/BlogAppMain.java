package com.zanke;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Zanke
 * @version 1.0.0
 * @description blog项目启动入口类
 */
@EnableScheduling
@MapperScan("com.zanke.mapper")
@SpringBootApplication
public class BlogAppMain {

    public static void main(String[] args) {

        ConfigurableApplicationContext run = SpringApplication.run(BlogAppMain.class, args);
        System.out.println("前台启动成功");
    }
}

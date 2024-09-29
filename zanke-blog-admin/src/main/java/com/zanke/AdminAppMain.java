package com.zanke;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@EnableMethodSecurity
@MapperScan("com.zanke.mapper")
@SpringBootApplication
public class AdminAppMain {

    public static void main(String[] args) {

        ConfigurableApplicationContext run = SpringApplication.run(AdminAppMain.class, args);
        System.out.println("后台启动成功");
    }
}
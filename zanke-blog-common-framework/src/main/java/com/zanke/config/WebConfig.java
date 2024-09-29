package com.zanke.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    /**
     * 跨域配置
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置可以跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                .allowedOriginPatterns("*")
                //设置允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                //设置允许的header属性
                .allowedHeaders("*")
                //是否允许Cookie
                .allowCredentials(true)
                //设置跨域允许时间
                .maxAge(3600);
    }
}
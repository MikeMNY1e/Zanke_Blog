package com.zanke.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.text.SimpleDateFormat;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 全局控制配置类
 */
@ControllerAdvice
public class GlobalControllerConfig {

    /**
     * 配置Jackson序列化器
     * @return
     */
    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        return new Jackson2ObjectMapperBuilder() {
            public void configure(ObjectMapper objectMapper) {
                // 注册 JavaTimeModule
                objectMapper.registerModule(new JavaTimeModule());

                // 禁用将日期作为时间戳序列化的功能
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // 设置日期格式
                objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

                // 注册自定义的序列化器和反序列化器
                //  Long类型序列化为String类型
                SimpleModule module = new SimpleModule();
                module.addSerializer(Long.class, ToStringSerializer.instance);
                module.addSerializer(Long.TYPE, ToStringSerializer.instance);
                objectMapper.registerModule(module);
            }
        };
    }
}
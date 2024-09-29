package com.zanke.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 设置请求接口的业务功能介绍
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlLogBusiness {
    String value() default "";
}

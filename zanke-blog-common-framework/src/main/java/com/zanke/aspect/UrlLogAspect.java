package com.zanke.aspect;

import com.zanke.annotation.UrlLogBusiness;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 请求调用的日志打印切面
 */
@Slf4j
@Component
@Aspect
public class UrlLogAspect {

    @Around("@annotation(com.zanke.annotation.UrlLogBusiness)")
    public Object printLog(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取 http请求
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        // 获取该请求的UrlLogAnnotation
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        UrlLogBusiness urlLogBusiness = methodSignature.getMethod().getAnnotation(UrlLogBusiness.class);

        log.info("=======Start=======");
        // 打印请求 URL
        log.info("URL : {}", request.getRequestURL());
        // 打印描述信息
        log.info("BusinessName : {}", urlLogBusiness.value());
        // 打印 Http method
        log.info("HTTP Method : {}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        log.info("Class Method : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        // 打印请求的 IP
        log.info("IP : {}", request.getRemoteHost());
        // 打印请求入参
        log.info("Request Args : {}", joinPoint.getArgs());

        Object res;
        try {
            // 执行目标方法
            res = joinPoint.proceed();

            // 打印出参
            log.info("Response : {}", res);

        } finally {
            // 结束后换行
            log.info("=======End=======" + System.lineSeparator());
        }

        return res;
    }
}

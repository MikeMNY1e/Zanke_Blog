package com.zanke.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import com.zanke.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 认证失败处理器
 */
@Slf4j
@Component
public class AuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        // 打印异常信息
        log.error("认证失败!", authException);

        ResponseResult<Object> responseResult;
        if (authException instanceof InsufficientAuthenticationException){
            // 未认证
            responseResult = ResponseResult.build(null, ResultCodeEnum.NOTLOGIN);
        } else {
            responseResult = ResponseResult.build(null, ResultCodeEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        WebUtil.responseJson(response, responseResult);
    }
}

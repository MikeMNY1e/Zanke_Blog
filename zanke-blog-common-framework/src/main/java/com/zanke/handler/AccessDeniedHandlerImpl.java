package com.zanke.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zanke.exception.SystemException;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import com.zanke.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 拒绝访问处理器
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {


        accessDeniedException.printStackTrace();

        ResponseResult<Object> responseResult = ResponseResult.build(null, ResultCodeEnum.HAVE_NO_AUTHORITY);

        WebUtil.responseJson(response, responseResult);
    }
}

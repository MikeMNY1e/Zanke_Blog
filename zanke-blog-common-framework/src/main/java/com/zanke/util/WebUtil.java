package com.zanke.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Slf4j
public class WebUtil {

    /**
     * 设置下载文件的请求头
     * @param filename
     * @param response
     * @throws UnsupportedEncodingException
     */
    public static void setDownLoadHeader(String filename, HttpServletResponse
            response) throws UnsupportedEncodingException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fname= URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition","attachment; filename="+fname);
    }


    /**
     * 响应json数据
     * @param response
     * @param data
     */
    public static <T> void responseJson(HttpServletResponse response, T data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            log.error("响应json异常", e);
        }
    }
}

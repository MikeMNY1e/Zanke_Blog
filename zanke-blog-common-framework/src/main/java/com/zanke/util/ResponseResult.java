package com.zanke.util;

import lombok.Data;

/**
 * 全局统一返回结果类
 */
@Data
public class ResponseResult<T> {
    // 返回码
    private Integer code;
    // 返回消息
    private String msg;
    // 返回数据
    private T data;


    // 返回数据
    protected static <T> ResponseResult<T> build(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        if (data != null)
            result.setData(data);
        return result;
    }
    public static <T> ResponseResult<T> build(T data, Integer code, String message) {
        ResponseResult<T> result = build(data);
        result.setCode(code);
        result.setMsg(message);
        return result;
    }
    public static <T> ResponseResult<T> build(T data, ResultCodeEnum codeEnum) {
        ResponseResult<T> result = build(data);
        result.setCode(codeEnum.getCode());
        result.setMsg(codeEnum.getMessage());
        return result;
    }

    /**
     * 操作成功
     * @param data  baseCategory1List
     * @param <T>
     * @return
     */
    public static<T> ResponseResult<T> ok(T data){
        ResponseResult<T> result = build(data);
        return build(data, ResultCodeEnum.SUCCESS);
    }
}
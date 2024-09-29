package com.zanke.exception;

import com.zanke.util.ResultCodeEnum;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 自定义全局异常
 */
public class SystemException extends RuntimeException{
    private int code;
    private String msg;
    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public SystemException(ResultCodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMessage();
    }
}
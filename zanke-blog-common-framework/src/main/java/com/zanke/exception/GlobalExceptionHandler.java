package com.zanke.exception;

import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
//@Component
public class GlobalExceptionHandler {

    /**
     * 处理自定义全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(SystemException.class)
    public ResponseResult<Void> systemExceptionHandler(SystemException e) {
        //打印异常信息
        log.error("自定义异常!", e);
        //从异常对象中获取提示信息封装返回
        return ResponseResult.build(null, e.getCode(), e.getMsg());
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseResult<Void> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException e) {
        //打印异常信息
        log.error("文件大小超出限制!", e);
        //从异常对象中获取提示信息封装返回
        return ResponseResult.build(null,
                ResultCodeEnum.FILE_SIZE_ERROR.getCode(),
                ResultCodeEnum.FILE_SIZE_ERROR.getMessage());
    }


//    /**
//     * 处理全局异常
//     *
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(Exception.class)
//    public ResponseResult<Void> exceptionHandler(Exception e) {
//        //打印异常信息
//        log.error("出现了异常!", e);
//        //从异常对象中获取提示信息封装返回
//        return ResponseResult.build(null, ResultCodeEnum.SYSTEM_ERROR.getCode(), e.getMessage());
//    }
}





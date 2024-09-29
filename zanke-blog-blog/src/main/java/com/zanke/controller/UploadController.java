package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.exception.SystemException;
import com.zanke.service.UploadService;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping
public class UploadController {

    @Resource
    private UploadService uploadService;


    /**
     * 上传头像到七牛云oss
     *
     * @return
     */
    @PostMapping("/upload")
    @UrlLogBusiness("上传头像")
    public ResponseResult<String> uploadImage(@RequestParam("img") MultipartFile img) {
        return uploadService.upLoadImage(img);
    }
}
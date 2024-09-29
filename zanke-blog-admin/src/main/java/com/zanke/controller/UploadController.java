package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.exception.SystemException;
import com.zanke.service.UploadService;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
public class UploadController {

    @Resource
    private UploadService uploadService;


    @PostMapping("/upload")
    @UrlLogBusiness("上传图片")
    public ResponseResult<String> upLoadImage(@RequestParam("img") MultipartFile image) {
        return uploadService.upLoadImage(image);
    }
}

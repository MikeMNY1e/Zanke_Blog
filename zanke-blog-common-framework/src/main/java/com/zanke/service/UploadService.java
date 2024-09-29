package com.zanke.service;

import com.zanke.util.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface UploadService {

    /**
     * 上传图片到七牛云oss
     * @param image
     * @return
     */
    ResponseResult<String> upLoadImage(MultipartFile image);
}

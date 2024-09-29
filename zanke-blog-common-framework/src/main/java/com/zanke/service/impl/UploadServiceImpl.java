package com.zanke.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.zanke.exception.SystemException;
import com.zanke.service.UploadService;
import com.zanke.util.FilePathUtil;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Slf4j
@Data
@Service("uploadService")
@ConfigurationProperties("zanke.oss")
public class UploadServiceImpl implements UploadService {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domainName;
    private String maxSize;

    /**
     * 上传图片到七牛云oss
     * @param image
     * @return
     */
    public ResponseResult<String> upLoadImage(MultipartFile image) {

        if (image == null) {
            throw new SystemException(ResultCodeEnum.FILE_ERROR);
        }
        // 获取原始文件名
        String originalFilename = image.getOriginalFilename();
        // 判断文件类型
        if(originalFilename == null || (!originalFilename.endsWith(".png") && !originalFilename.endsWith(".jpg"))) {
            throw new SystemException(ResultCodeEnum.FILE_TYPE_ERROR);
        }
        // 判断文件大小
        if (image.getSize() > Long.parseLong(maxSize)) {
            throw new SystemException(ResultCodeEnum.FILE_SIZE_ERROR);
        }

        // 生成文件路径
        String filePath = FilePathUtil.generateFilePath(originalFilename);
        String url;
        try {
            // 上传文件到OSS
            url = uploadOss(image, filePath);
        } catch (Exception e) {
            throw new SystemException(ResultCodeEnum.IMAGE_UPLOAD_ERROR);
        }
        return ResponseResult.ok(url);
    }
    private String uploadOss(MultipartFile imgFile, String key){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.autoRegion());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            // 获取图片输入流
            InputStream inputStream = imgFile.getInputStream();
            // 创建凭证
            Auth auth = Auth.create(accessKey, secretKey);
            String uploadToken = auth.uploadToken(bucket);
            try {
                //默认不指定key的情况下，以文件内容的hash值作为文件名
                Response response =
                        uploadManager.put(inputStream, key, uploadToken, null, null);
                //解析上传成功的结果
                DefaultPutRet putRet = new
                        Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                log.info(putRet.key);
                log.info(putRet.hash);
                return domainName + key;

            } catch (QiniuException ex) {
                ex.printStackTrace();
                if (ex.response != null) {
                    System.err.println(ex.response);
                    try {
                        String body = ex.response.toString();
                       log.error(body);
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        return "www";
    }
}
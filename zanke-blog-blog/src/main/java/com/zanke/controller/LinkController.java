package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.vo.LinkVo;
import com.zanke.service.LinkService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping("/link")
public class LinkController {

    @Resource
    private LinkService linkService;


    /**
     * 获取所有友链
     * @return
     */
    @GetMapping("/getAllLink")
    @UrlLogBusiness("获取所有友链")
    public ResponseResult<List<LinkVo>> getAllLink() {
        return linkService.getAllLink();
    }
}

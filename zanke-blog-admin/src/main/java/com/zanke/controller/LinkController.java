package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.dto.link.LinkListDto;
import com.zanke.pojo.dto.link.LinkStatusChangeDto;
import com.zanke.pojo.entity.Link;
import com.zanke.pojo.vo.LinkVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.LinkService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping("/content/link")
public class LinkController {

    @Resource
    private LinkService linkService;


    @GetMapping("/list")
    @UrlLogBusiness("获取友链列表")
    public ResponseResult<PageVo<List<LinkVo>>> getAllLink(int pageNum, int pageSize, LinkListDto linkListDto) {
        return linkService.getAllLinkForAdmin(pageNum, pageSize, linkListDto);
    }


    @PostMapping
    @UrlLogBusiness("添加友链")
    public ResponseResult<Void> addLink(@RequestBody Link link) {
        return linkService.addLink(link);
    }


    @GetMapping("/{id}")
    @UrlLogBusiness("获取友链信息")
    public ResponseResult<LinkVo> getLinkInfoById(@PathVariable Long id) {
        return linkService.getLinkInfoById(id);
    }


    @PutMapping
    @UrlLogBusiness("修改友链")
    public ResponseResult<Void> updateLink(@RequestBody Link link) {
        return linkService.updateLink(link);
    }


    @PutMapping("/changeLinkStatus")
    @UrlLogBusiness("修改友链状态")
    public ResponseResult<Void> updateLinkStatus(@RequestBody LinkStatusChangeDto linkStatusChangeDto) {
        return linkService.changeStatus(linkStatusChangeDto);
    }


    @DeleteMapping("/{id}")
    @UrlLogBusiness("删除友链")
    public ResponseResult<Void> deleteLink(@PathVariable Long id) {
        return linkService.deleteLink(id);
    }
}

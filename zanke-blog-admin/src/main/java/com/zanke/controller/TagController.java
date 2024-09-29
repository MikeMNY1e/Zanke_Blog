package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.dto.TagListDto;
import com.zanke.pojo.entity.Tag;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.TagVo;
import com.zanke.service.TagService;
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
@RequestMapping("/content/tag")
public class TagController {

    @Resource
    private TagService tagService;


    @GetMapping("/list")
    @UrlLogBusiness("分页获取标签列表")
    public ResponseResult<PageVo<List<TagVo>>> pageTagList(int pageNum, int pageSize, TagListDto tagListDto) {
        return tagService.pageTagList(pageNum, pageSize, tagListDto);
    }


    @PostMapping
    @UrlLogBusiness("添加标签")
    public ResponseResult<Void> addTag(@RequestBody Tag tag) {
        return tagService.addTag(tag);
    }


    @DeleteMapping("/{id}")
    @UrlLogBusiness("删除标签")
    public ResponseResult<Void> deleteTagById(@PathVariable Long id) {
        return tagService.deleteTagById(id);
    }


    @GetMapping("/{id}")
    @UrlLogBusiness("获取标签信息")
    public ResponseResult<TagVo> getTagInfoById(@PathVariable Long id) {
        return tagService.getTagInfoById(id);
    }


    @PutMapping
    @UrlLogBusiness("修改标签")
    public ResponseResult<Void> updateTagById(@RequestBody Tag tag) {
        return tagService.updateTagById(tag);
    }


    @GetMapping("/listAllTag")
    @UrlLogBusiness("获取所有标签")
    public ResponseResult<List<TagVo>> allTagList() {
        return tagService.allTagList();
    }
}

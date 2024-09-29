package com.zanke.service;

import com.zanke.pojo.dto.TagListDto;
import com.zanke.pojo.entity.Tag;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.TagVo;
import com.zanke.util.ResponseResult;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface TagService {

    /**
     * 分页查询标签列表
     * @param pageNum
     * @param pageSize
     * @param tagListDto
     * @return
     */
    ResponseResult<PageVo<List<TagVo>>> pageTagList(int pageNum, int pageSize, TagListDto tagListDto);


    /**
     * 添加标签
     * @param tag
     * @return
     */
    ResponseResult<Void> addTag(Tag tag);


    /**
     * 根据标签id删除标签
     * @param id
     * @return
     */
    ResponseResult<Void> deleteTagById(Long id);


    /**
     * 根据标签id查询标签
     * @param id
     * @return
     */
    ResponseResult<TagVo> getTagInfoById(Long id);


    /**
     * 修改标签
     * @param tag
     * @return
     */
    ResponseResult<Void> updateTagById(Tag tag);


    /**
     * 查询所有标签(写文章用到)
     * @return
     */
    ResponseResult<List<TagVo>> allTagList();
}
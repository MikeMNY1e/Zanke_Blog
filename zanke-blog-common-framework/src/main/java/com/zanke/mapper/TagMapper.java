package com.zanke.mapper;

import com.zanke.pojo.dto.TagListDto;
import com.zanke.pojo.entity.Tag;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface TagMapper {

    /**
     * 查询标签列表
     * @param tagListDto
     * @return
     */
    List<Tag> selectTagList(TagListDto tagListDto);


    /**
     * 插入标签
     * @param tag
     * @return
     */
    int insertInt(Tag tag);


    /**
     * 修改标签
     * @param tag
     * @return
     */
    int updateTagById(Tag tag);


    /**
     * 根据id查询标签
     * @param id
     * @return
     */
    Tag selectTagById(Long id);


    /**
     * 根据文章id查询标签
     * @param id
     * @return
     */
    List<Tag> selectTagListByArticleId(Long id);


    /**
     * 根据id查询删除标签
     * @param tagIds
     * @return
     */
    int selectDisableTagCountByIds(List<Long> tagIds);
}

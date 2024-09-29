package com.zanke.mapper;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface ArticleTagMapper {


    /**
     * 插入文章标签关系
     * @return
     */
    int insertIntoArticleTag(Long id, List<Long> tagIdList);


    /**
     * 更新文章后删除文章不必要标签关系
     * @param id
     * @return
     */
    int deleteByArticleId(Long id);


    /**
     * 根据标签id删除文章标签关系
     * @param id
     * @return
     */
    int deleteByTagId(Long id);


    /**
     * 根据tagId查询文章数量
     * @param id
     * @return
     */
    int selectArticleIdByTagId(Long id);
}

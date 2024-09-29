package com.zanke.mapper;

import com.zanke.pojo.dto.article.ArticleListDto;
import com.zanke.pojo.entity.Article;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description Article相关数据库查询方法
 */
public interface ArticleMapper {

    /**
     * 根据分类id，分页查询已发布的文章
     * @return
     */
    List<Article> selectPublishedArticleListDESCByIsTopAndViewCount();


    /**
     * 根据文章id获取已发布文章详情
     * @param id
     * @return
     */
    Article selectPublishedArticleByIdDESCByViewCount(Long id);


    /**
     * 更新文章浏览量
     * @return
     */
    int updateViewCount(Long id, Long viewCount);


    /**
     * 添加文章
     * @param article
     * @return
     */
    int insert(Article article);


    /**
     * 根据标题或摘要查询文章列表
     * @param articleListDto
     * @return
     */
    List<Article> selectArticleListByTitleAndSummary(ArticleListDto articleListDto);


    /**
     * 更新文章
     * @param article
     * @return
     */
    int updateArticle(Article article);


    /**
     * 根据id删除文章
     * @param id
     * @return
     */
    int deleteArticleById(Long id);


    /**
     * 查询已发布文章的分类id
     * @return
     */
    List<Long> selectPublishedArticleCategoryId();


    /**
     * 根据id获取文章详情
     * @param id
     * @return
     */
    Article selectArticleForAdmin(Long id);
}

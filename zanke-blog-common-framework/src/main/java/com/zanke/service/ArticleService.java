package com.zanke.service;

import com.zanke.pojo.dto.article.AddArticleDto;
import com.zanke.pojo.dto.article.ArticleListDto;
import com.zanke.pojo.entity.Article;
import com.zanke.pojo.vo.article.ArticleDetailVo;
import com.zanke.pojo.vo.article.ArticleVo;
import com.zanke.pojo.vo.article.HotArticleVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description Article相关业务接口
 */
public interface ArticleService {

    /**
     * 获取已发布热门文章
     *
     * @return
     */
    ResponseResult<List<HotArticleVo>> getHotArticleList();


    /**
     * 根据分类id，分页获取已发布文章列表
     *
     * @param pageNum
     * @param pageSize
     * @param categoryId
     * @return
     */
    ResponseResult<PageVo<List<ArticleVo>>> getArticleListByCategoryId(int pageNum, int pageSize, Long categoryId);


    /**
     * 根据文章id获取已发布文章详情
     * @param id
     * @return
     */
    ResponseResult<ArticleDetailVo> getArticleDetailById(Long id);


    /**
     * 更新文章浏览量
     * @param id
     * @return
     */
    ResponseResult<Void> updateViewCount(Long id, HttpServletRequest request);


    /**
     * 添加文章
     * @param addArticleDto
     * @return
     */
    ResponseResult<Void> addArticle(AddArticleDto addArticleDto);


    /**
     * 分页获取所有文章列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    ResponseResult<PageVo<List<ArticleVo>>> getArticleListForAdmin(int pageNum, int pageSize, ArticleListDto articleListDto);


    /**
     * 根据文章id获取文章详情(后台用到)
     * @param id
     * @return
     */
    ResponseResult<Article> getArticleDetailForAdmin(Long id);


    /**
     * 更新文章
     * @param article
     * @return
     */
    ResponseResult<Void> updateArticle(Article article);


    /**
     * 删除文章
     * @param id
     * @return
     */
    ResponseResult<Void> deleteArticle(Long id);
}
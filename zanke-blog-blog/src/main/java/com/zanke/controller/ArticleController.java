package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.vo.article.ArticleDetailVo;
import com.zanke.pojo.vo.article.ArticleVo;
import com.zanke.pojo.vo.article.HotArticleVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.ArticleService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description Article相关接口
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;


    /**
     * 获取已发布热门文章列表
     * @return
     */
    @GetMapping("/hotArticleList")
    @UrlLogBusiness("获取已发布热门文章列表")
    public ResponseResult<List<HotArticleVo>> getHotArticle() {
        return articleService.getHotArticleList();
    }


    /**
     * 根据分类id，分页获取已发布文章列表
     * @param pageNum
     * @param pageSize
     * @param categoryId
     * @return
     */
    @GetMapping("/articleList")
    @UrlLogBusiness("根据分类id，分页获取已发布文章列表")
    public ResponseResult<PageVo<List<ArticleVo>>> getArticleList(int pageNum, int pageSize, Long categoryId) {
        return articleService.getArticleListByCategoryId(pageNum, pageSize, categoryId);
    }


    /**
     * 根据文章id获取文章详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @UrlLogBusiness("根据文章id获取文章详情")
    public ResponseResult<ArticleDetailVo> getArticleDetaioById(@PathVariable("id") Long id) {
        return articleService.getArticleDetailById(id);
    }


    /**
     * 更新文章浏览量
     * @param id
     * @return
     */
    @PutMapping("/updateViewCount/{id}")
    @UrlLogBusiness("更新文章浏览量")
    public ResponseResult<Void> updateViewCount(@PathVariable("id") Long id) {
        return articleService.updateViewCount(id);
    }
}

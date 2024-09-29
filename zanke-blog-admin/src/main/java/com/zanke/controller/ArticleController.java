package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.dto.article.AddArticleDto;
import com.zanke.pojo.dto.article.ArticleListDto;
import com.zanke.pojo.entity.Article;
import com.zanke.pojo.vo.article.ArticleVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.ArticleService;
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
@RequestMapping("/content/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;


    @PostMapping
    @UrlLogBusiness("添加文章")
    public ResponseResult<Void> addArticle(@RequestBody AddArticleDto article) {
        return articleService.addArticle(article);
    }


    @GetMapping("/list")
    @UrlLogBusiness("获取所有未删除文章列表")
    public ResponseResult<PageVo<List<ArticleVo>>> articleList(int pageNum, int pageSize, ArticleListDto articleListDto) {
        return articleService.getArticleListForAdmin(pageNum, pageSize, articleListDto);
    }


    @GetMapping("/{id}")
    @UrlLogBusiness("获取文章详情")
    public ResponseResult<Article> getArticleDetailById(@PathVariable Long id) {
        return articleService.getArticleDetailForAdmin(id);
    }


    @PutMapping
    @UrlLogBusiness("更新文章")
    public ResponseResult<Void> updateArticle(@RequestBody Article article) {
        return articleService.updateArticle(article);
    }


    @DeleteMapping("/{id}")
    @UrlLogBusiness("删除文章")
    public ResponseResult<Void> deleteArticle(@PathVariable Long id) {
        return articleService.deleteArticle(id);
    }
}

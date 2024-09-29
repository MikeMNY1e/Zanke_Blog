package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.entity.Comment;
import com.zanke.pojo.vo.CommentVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.CommentService;
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
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;


    /**
     * 获取评论列表
     * @param articleId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/commentList")
    @UrlLogBusiness("根据文章id获取评论列表")
    public ResponseResult<PageVo<List<CommentVo>>> getCommentListByArticleId(Long articleId, int pageNum, int pageSize) {
        return commentService.getCommentListByArticleId(articleId, pageNum, pageSize);
    }


    /**
     * 发表评论
     * @param comment
     * @return
     */
    @PostMapping
    @UrlLogBusiness("发表评论")
    public ResponseResult<Void> addOneComment(@RequestBody Comment comment) {
        return commentService.addOneComment(comment);
    }


    /**
     * 获取友链评论
     * @param articleId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/linkCommentList")
    @UrlLogBusiness("根据文章id获取友链评论")
    public ResponseResult<PageVo<List<CommentVo>>> getLinkCommentList(Long articleId, int pageNum, int pageSize) {
        return commentService.getLinkCommentList(articleId, pageNum, pageSize);
    }
}

package com.zanke.service;

import com.zanke.pojo.entity.Comment;
import com.zanke.pojo.vo.CommentVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.util.ResponseResult;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface CommentService {

    /**
     * 根据文章id获取评论列表
     * @param articleId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ResponseResult<PageVo<List<CommentVo>>> getCommentListByArticleId(Long articleId, int pageNum, int pageSize);


    /**
     * 添加评论
     * @param comment
     * @return
     */
    ResponseResult<Void> addOneComment(Comment comment);


    /**
     * 获取友链评论列表
     * @param articleId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ResponseResult<PageVo<List<CommentVo>>> getLinkCommentList(Long articleId, int pageNum, int pageSize);
}
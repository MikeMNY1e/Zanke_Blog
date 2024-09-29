package com.zanke.mapper;

import com.zanke.pojo.entity.Comment;

import java.util.List;
import java.util.Set;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface CommentMapper {

    /**
     * 根据文章id查询根评论列表
     * @param articleId
     * @return
     */
    List<Comment> selectRootCommentListByArticleIdDESCBYCreateTime(Long articleId, String type);


    /**
     * 根据 根评论id 查询评论列表
     * @param rootIds
     * @return
     */
    List<Comment> selectChildCommentListByRootIdDESCBYCreateTime(Set<Long> rootIds);


    /**
     * 插入评论
     * @param comment
     * @return
     */
    int insertOneComment(Comment comment);


    /**
     * 根据文章id删除评论
     * @param id
     * @return
     */
    int deleteByArticleId(Long id);
}

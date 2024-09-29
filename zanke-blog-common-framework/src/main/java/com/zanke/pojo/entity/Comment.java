package com.zanke.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评论表(Comment)实体类
 *
 * @author makejava
 * @since 2024-09-04 16:10:51
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Comment {

    private Long id;
/**
     * 评论类型（0代表文章评论，1代表友链评论）
     */
    private String type;
/**
     * 文章id
     */
    private Long articleId;
/**
     * 根评论id
     */
    private Long rootId;
/**
     * 评论内容
     */
    private String content;
/**
     * 所回复的目标评论的userid
     */
    private Long toCommentUserId;
/**
     * 回复目标评论id
     */
    private Long toCommentId;

    private Long createBy;

    private Date createTime;

    private Long updateBy;

    private Date updateTime;
/**
     * 删除标志（0代表未删除，1代表已删除）
     */
    private Integer delFlag;

    private String username;

    private String toCommentUserName;
}


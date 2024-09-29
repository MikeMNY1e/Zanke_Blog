package com.zanke.pojo.vo.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleVo {

    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 文章摘要
     */
    private String summary;

    private String categoryName;
    /**
     * 缩略图
     */
    private String thumbnail;
    /**
     * 访问量
     */
    private Long viewCount;

    private Date createTime;

}
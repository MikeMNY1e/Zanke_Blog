package com.zanke.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Date;
import java.io.Serializable;

/**
 * 分类表(Category)实体类
 *
 * @author makejava
 * @since 2024-09-01 14:00:09
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Category {

    private Long id;
/**
     * 分类名
     */
    private String name;
/**
     * 父分类id，如果没有父分类为-1
     */
    private Long pid;
/**
     * 描述
     */
    private String description;
/**
     * 状态0:正常,1禁用
     */
    private String status;

    private Long createBy;

    private Date createTime;

    private Long updateBy;

    private Date updateTime;
/**
     * 删除标志（0代表未删除，1代表已删除）
     */
    private Integer delFlag;

    @JsonIgnore
    private String metaDescription;
    @JsonIgnore
    private String metaKeywords;
}


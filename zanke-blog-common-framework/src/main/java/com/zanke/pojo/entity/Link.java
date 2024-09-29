package com.zanke.pojo.entity;

import lombok.*;

import java.util.Date;
import java.io.Serializable;

/**
 * 友链(Link)实体类
 *
 * @author makejava
 * @since 2024-09-03 15:29:19
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Link {

    private Long id;

    private String name;

    private String logo;

    private String description;
/**
     * 网站地址
     */
    private String address;
/**
     * 审核状态 (0代表审核通过，1代表审核未通过，2代表未审核)
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
}


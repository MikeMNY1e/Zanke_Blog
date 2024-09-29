package com.zanke.pojo.dto.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleListDto {

    private String title;
    private String summary;
}

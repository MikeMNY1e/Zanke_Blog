package com.zanke.pojo.vo.category;

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
public class CategoryVo {

    private Long id;
    private String name;
    private String description;
    private String status;
}

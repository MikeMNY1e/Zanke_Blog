package com.zanke.pojo.vo.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MenuTreeVo {

    private Long id;
    private String label;
    private Long parentId;
    private List<MenuTreeVo> children;
}

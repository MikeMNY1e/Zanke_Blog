package com.zanke.pojo.vo.role;

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
public class RoleUpdateVo {

    private List menus;
    private List<Long> checkedKeys;
}

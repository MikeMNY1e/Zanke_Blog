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
public class RoleVo {

    /**
     * 角色ID
     */
    private Long id;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色权限字符串
     */
    private String roleKey;
    /**
     * 显示顺序
     */
    private Integer roleSort;
    /**
     * 角色状态（0正常 1停用）
     */
    private String status;

    private String remark;

    private List<Long> menuIds;
}

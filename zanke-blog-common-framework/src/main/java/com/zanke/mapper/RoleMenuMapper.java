package com.zanke.mapper;

import com.zanke.pojo.dto.role.RoleStatusChangeDto;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface RoleMenuMapper {

    /**
     * 修改角色菜单那关系的状态
     * @param roleId
     */
    int updateStatus(RoleStatusChangeDto roleId);


    /**
     * 新增角色菜单关联关系
     * @param id
     * @param menuIds
     * @return
     */
    int insert(Long id, List<Long> menuIds, String status);


    /**
     * 删除角色菜单关联关系
     * @param id
     */
    int deleteByRoleId(Long id);


    /**
     * 根据角色id查询菜单id列表
     * @param id
     * @return
     */
    List<Long> selectMenuIdListByRoleId(Long id);


    /**
     * 根据菜单id查询角色数量(包括禁用单没删除的)
     * @param id
     * @return
     */
    int selectRoleCountByMenuId(Long id);
}

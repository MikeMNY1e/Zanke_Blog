package com.zanke.mapper;

import com.zanke.pojo.dto.role.RoleListDto;
import com.zanke.pojo.entity.Role;
import com.zanke.pojo.vo.role.RoleVo;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface RoleMapper {

    /**
     * 根据用户id查询角色
     * @param id
     * @return
     */
    List<String> selectRoleKeysByUserId(Long id);


    /**
     * 根据角色名和状态查询角色(后台)
     * @param roleListDto
     * @return
     */
    List<RoleVo> selectAllRoleByRoleNameAndStatus(RoleListDto roleListDto);


    /**
     * 修改角色状态
     * @param role
     * @return
     */
    int updateStatus(Role role);


    /**
     * 新增角色
     * @param role
     */
    int insert(Role role);


    /**
     * 根据角色id查询角色详情
     * @param id
     * @return
     */
    RoleVo selectRoleById(Long id);


    /**
     * 修改角色信息
     * @param role
     */
    int updateRole(Role role);


    /**
     * 根据id删除角色
     * @param id
     * @return
     */
    int deleteRole(Long id);


    /**
     * 根据角色id查询改用户未启用角色的数量
     * @param roleIds
     * @return
     */
    int selectDisableRoleCountByRoleId(List<Long> roleIds);
}

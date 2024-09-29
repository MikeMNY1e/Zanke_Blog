package com.zanke.service;

import com.zanke.pojo.dto.role.RoleListDto;
import com.zanke.pojo.dto.role.RoleStatusChangeDto;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.role.RoleVo;
import com.zanke.util.ResponseResult;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface RoleService {

    /**
     * 分页查询所有角色(后台)
     * @param pageNum
     * @param pageSize
     * @param roleListDto
     * @return
     */
    ResponseResult<PageVo<List<RoleVo>>> getAllRoleForAdmin(int pageNum, int pageSize, RoleListDto roleListDto);


    /**
     * 修改角色状态
     * @param roleStatusChangeDto
     * @return
     */
    ResponseResult<Void> changeStatus(RoleStatusChangeDto roleStatusChangeDto);


    /**
     * 新增角色
     * @param roleVo
     * @return
     */
    ResponseResult<Void> addRole(RoleVo roleVo);


    /**
     * 根据角色id获取角色详情
     * @param id
     * @return
     */
    ResponseResult<RoleVo> getRoleDetailById(Long id);


    /**
     * 修改角色
     * @param roleVo
     * @return
     */
    ResponseResult<Void> updateRole(RoleVo roleVo);


    /**
     * 删除角色
     * @param id
     * @return
     */
    ResponseResult<Void> deleteRole(Long id);


    /**
     * 获取所有角色
     * @return
     */
    ResponseResult<List<RoleVo>> listAllRole();
}

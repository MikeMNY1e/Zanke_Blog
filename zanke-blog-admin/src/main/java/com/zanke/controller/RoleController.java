package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.dto.role.RoleListDto;
import com.zanke.pojo.dto.role.RoleStatusChangeDto;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.role.RoleVo;
import com.zanke.service.RoleService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping("/system/role")
public class RoleController {

    @Resource
    private RoleService roleService;


    @GetMapping("/list")
    @UrlLogBusiness("获取所有角色列表")
    public ResponseResult<PageVo<List<RoleVo>>> list(int pageNum, int pageSize, RoleListDto roleListDto) {
        return roleService.getAllRoleForAdmin(pageNum, pageSize, roleListDto);
    }


    @PutMapping("/changeStatus")
    @UrlLogBusiness("修改角色状态")
    public ResponseResult<Void> changeStatus(@RequestBody RoleStatusChangeDto roleStatusChangeDto) {
        return roleService.changeStatus(roleStatusChangeDto);
    }


    @PostMapping
    @UrlLogBusiness("新增角色")
    public ResponseResult<Void> addRole(@RequestBody RoleVo roleVo) {
        return roleService.addRole(roleVo);
    }


    @GetMapping("/{id}")
    @UrlLogBusiness("获取角色详情")
    public ResponseResult<RoleVo> getRoleDetailById(@PathVariable Long id) {
        return roleService.getRoleDetailById(id);
    }


    @PutMapping
    @UrlLogBusiness("修改角色")
    public ResponseResult<Void> updateRole(@RequestBody RoleVo roleVo) {
        return roleService.updateRole(roleVo);
    }


    @DeleteMapping("/{id}")
    @UrlLogBusiness("删除角色")
    public ResponseResult<Void> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }


    @GetMapping("/listAllRole")
    @UrlLogBusiness("获取所有角色")
    public ResponseResult<List<RoleVo>> listAllRole() {
        return roleService.listAllRole();
    }
}
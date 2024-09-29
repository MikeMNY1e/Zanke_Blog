package com.zanke.service;

import com.zanke.pojo.dto.MenuListDto;
import com.zanke.pojo.entity.Menu;
import com.zanke.pojo.vo.role.RoleUpdateVo;
import com.zanke.pojo.vo.user.AdminUserInfoVo;
import com.zanke.pojo.vo.menu.MenuTreeVo;
import com.zanke.pojo.vo.menu.MenuVo;
import com.zanke.pojo.vo.menu.RoutersVo;
import com.zanke.util.ResponseResult;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface MenuService {

    /**
     * 根据用户id查询所有权限、角色等信息
     * @return
     */
    ResponseResult<AdminUserInfoVo> getInfoByUserId();


    /**
     * 根据用户id查询菜单
     * @return
     */
    ResponseResult<RoutersVo> selectRouterMenuTreeByUserId();


    /**
     * 查询所有菜单(后台)
     * @return
     */
    ResponseResult<List<MenuVo>> getAllMenuForAdmin(MenuListDto menuListDto);


    /**
     * 新增菜单
     * @param menu
     * @return
     */
    ResponseResult<Void> addMenu(Menu menu);


    /**
     * 根据id查询菜单信息
     * @param id
     * @return
     */
    ResponseResult<MenuVo> getMenuInfoById(Long id);


    /**
     * 修改菜单
     * @param menu
     * @return
     */
    ResponseResult<Void> updateMenu(Menu menu);


    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    ResponseResult<Void> deleteMenu(Long menuId);


    /**
     * 获取菜单树
     * @return
     */
    ResponseResult<List<MenuTreeVo>> getMenuTree();


    /**
     * 获取角色菜单树
     * @param id
     * @return
     */
    ResponseResult<RoleUpdateVo> getRoleMenuTree(Long id);
}

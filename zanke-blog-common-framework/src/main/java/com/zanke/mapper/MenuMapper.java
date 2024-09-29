package com.zanke.mapper;

import com.zanke.pojo.dto.MenuListDto;
import com.zanke.pojo.entity.Menu;
import com.zanke.pojo.vo.menu.MenuTreeVo;
import com.zanke.pojo.vo.menu.MenuVo;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface MenuMapper {

    /**
     * 根据用户id查询所有权限
     * @param id
     * @return
     */
    List<String> selectPermsByUserId(Long id);


    /**
     * 查询所有权限
     * @return
     */
    List<String> selectAllPerms();


    /**
     * 查询所有路由菜单
     * @return
     */
    List<MenuVo> selectAllRouterMenu(MenuListDto menuListDto);


    /**
     * 根据用户id查询路由菜单
     * @param id
     * @return
     */
    List<MenuVo> selectRouterMenuByUserId(Long id);


    /**
     * 根据菜单名称和状态查询菜单
     * @param menuListDto
     * @return
     */
    List<MenuVo> selectAllRouterMenuByMenuNameAndStatus(MenuListDto menuListDto);


    /**
     * 添加菜单
     * @param menu
     * @return
     */
    int insertIntoMenu(Menu menu);


    /**
     * 根据id查询菜单
     * @param id
     * @return
     */
    MenuVo selectMenuById(Long id);


    /**
     * 修改菜单
     * @param menu
     */
    int updateMenu(Menu menu);


    /**
     * 根据id查询子菜单(用于检查当前菜单是否有子菜单)
     * @param menuId
     * @return
     */
    List<Long> selectChildrenIdList(Long menuId);


    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    int deleteMenu(Long menuId);


    /**
     * 查询所有菜单树
     * @return
     */
    List<MenuTreeVo> selectMenuForTree();


    /**
     * 根据角色id查询菜单树
     * @param id
     * @return
     */
    List<MenuTreeVo> selectMenuForTreeByRoleId(Long id);


    /**
     * 根据菜单id查询该角色未启用的菜单数量
     * @param menuIds
     * @return
     */
    int selectDisableMenuCountByMenuId(List<Long> menuIds);
}

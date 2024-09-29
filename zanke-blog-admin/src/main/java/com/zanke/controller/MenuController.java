package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.dto.MenuListDto;
import com.zanke.pojo.entity.Menu;
import com.zanke.pojo.vo.menu.MenuTreeVo;
import com.zanke.pojo.vo.menu.MenuVo;
import com.zanke.pojo.vo.role.RoleUpdateVo;
import com.zanke.service.MenuService;
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
@RequestMapping("/system/menu")
public class MenuController {

    @Resource
    private MenuService menuService;


    @GetMapping("/list")
    @UrlLogBusiness("获取菜单列表")
    public ResponseResult<List<MenuVo>> list(MenuListDto menuListDto){
        return menuService.getAllMenuForAdmin(menuListDto);
    }


    @PostMapping
    @UrlLogBusiness("新增菜单")
    public ResponseResult<Void> addMenu(@RequestBody Menu menu){
        return menuService.addMenu(menu);
    }


    @GetMapping("/{id}")
    @UrlLogBusiness("获取菜单信息")
    public ResponseResult<MenuVo> getMenuInfoById(@PathVariable Long id){
        return menuService.getMenuInfoById(id);
    }


    @PutMapping
    @UrlLogBusiness("修改菜单")
    public ResponseResult<Void> updateMenu(@RequestBody Menu menu){
        return menuService.updateMenu(menu);
    }


    @DeleteMapping("/{menuId}")
    @UrlLogBusiness("删除菜单")
    public ResponseResult<Void> deleteMenu(@PathVariable Long menuId){
        return menuService.deleteMenu(menuId);
    }


    @GetMapping("/treeselect")
    @UrlLogBusiness("获取菜单树")
    public ResponseResult<List<MenuTreeVo>> getMenuTree() {
        return menuService.getMenuTree();
    }


    @GetMapping("/roleMenuTreeselect/{id}")
    @UrlLogBusiness("获取角色菜单树")
    public ResponseResult<RoleUpdateVo> getRoleMenuTree(@PathVariable Long id) {
        return menuService.getRoleMenuTree(id);
    }
}

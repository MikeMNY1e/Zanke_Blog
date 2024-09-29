package com.zanke.service.impl;

import com.zanke.exception.SystemException;
import com.zanke.mapper.MenuMapper;
import com.zanke.mapper.RoleMapper;
import com.zanke.mapper.RoleMenuMapper;
import com.zanke.pojo.dto.MenuListDto;
import com.zanke.pojo.entity.Menu;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.menu.MenuTreeVo;
import com.zanke.pojo.vo.menu.MenuVo;
import com.zanke.pojo.vo.menu.RoutersVo;
import com.zanke.pojo.vo.role.RoleUpdateVo;
import com.zanke.pojo.vo.user.AdminUserInfoVo;
import com.zanke.pojo.vo.user.UserInfoVo;
import com.zanke.service.MenuService;
import com.zanke.util.AuthenticationUtil;
import com.zanke.util.BeanCopyUtil;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Service("menuService")
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuMapper menuMapper;
    @Resource
    private RoleMapper roleMapper;
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    @Autowired
    private RedissonClient redisson;


    /**
     * 根据用户id查询权限、角色等信息
     *
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<AdminUserInfoVo> getInfoByUserId() {

        // 获取当前登录用户
        User user = AuthenticationUtil.getUserFromContextHolder();
        UserInfoVo userInfo = BeanCopyUtil.copyBean(user, UserInfoVo.class);

        Long id = user.getId();

        if (id == 1L) {
            // 如果id等于1，则是超级管理员，拥有所有权限，角色为"admin"
            List<String> allPerms = menuMapper.selectAllPerms();
            List<String> allRoles = new ArrayList<>();
            allRoles.add("admin");
            return ResponseResult.ok(new AdminUserInfoVo(allPerms, allRoles, userInfo));
        }
        // 查询权限
        List<String> permissionList = menuMapper.selectPermsByUserId(id);
        // 查询角色
        List<String> roleKeyList = roleMapper.selectRoleKeysByUserId(id);

        return ResponseResult.ok(new AdminUserInfoVo(permissionList, roleKeyList, userInfo));
    }


    /**
     * 根据用户id查询路由菜单
     * @return
     */
    @Override
    public ResponseResult<RoutersVo> selectRouterMenuTreeByUserId() {

        List<MenuVo> menuVoList;

        // 获取当前登录用户
        User user = AuthenticationUtil.getUserFromContextHolder();

        Long id = user.getId();

        if (id == 1L) {
            // 如果是超级管理员，返回所有菜单
            menuVoList = menuMapper.selectAllRouterMenu(new MenuListDto());
        } else {
            menuVoList = menuMapper.selectRouterMenuByUserId(id);
        }

        return ResponseResult.ok(new RoutersVo(buildMenuTreeForMenuVo(menuVoList, 0L)));
    }
    private List<MenuVo> buildMenuTreeForMenuVo(List<MenuVo> menuList, Long parentId) {
        return  menuList.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .peek(m -> m.setChildren(getChildrenMenu(m, menuList)))
                .toList();
    }
    private List<MenuVo> getChildrenMenu(MenuVo menu, List<MenuVo> menuList) {
        return menuList.stream()
                .filter(m -> m.getParentId().equals(menu.getId()))
                .peek(m -> m.setChildren(getChildrenMenu(m, menuList)))
                .toList();
    }


    /**
     * 获取所有菜单(后台)
     * @return
     */
    @Override
    public ResponseResult<List<MenuVo>> getAllMenuForAdmin(MenuListDto menuListDto) {

        if (menuListDto.getMenuName() != null && menuListDto.getMenuName().isEmpty()) {
            menuListDto.setMenuName(null);
        }
        if (menuListDto.getStatus() != null && menuListDto.getStatus().isEmpty()) {
            menuListDto.setStatus(null);
        }

        List<MenuVo> menuVoList = menuMapper.selectAllRouterMenuByMenuNameAndStatus(menuListDto);
        return ResponseResult.ok(menuVoList);
    }


    /**
     * 参数校验
     * @param menu
     */
    private void paramsCheck(Menu menu) {
        if ("M".equals(menu.getMenuType())) {
            if (!StringUtils.hasText(menu.getMenuName())) {
                throw new SystemException(ResultCodeEnum.MENU_NAME_EMPTY);
            }
            if (menu.getOrderNum() == null) {
                throw new SystemException(ResultCodeEnum.MENU_ORDER_NUM_EMPTY);
            }
            if (!StringUtils.hasText(menu.getPath())) {
                throw new SystemException(ResultCodeEnum.MENU_PATH_EMPTY);
            }
            if (menu.getIcon() == null) {
                throw new SystemException(ResultCodeEnum.MENU_ICON_EMPTY);
            }
            if (menu.getParentId() == null) {
                throw new SystemException(ResultCodeEnum.MENU_PARENT_EMPTY);
            }
        } else if ("C".equals(menu.getMenuType())) {
            if (!StringUtils.hasText(menu.getMenuName())) {
                throw new SystemException(ResultCodeEnum.MENU_NAME_EMPTY);
            }
            if (menu.getOrderNum() == null) {
                throw new SystemException(ResultCodeEnum.MENU_ORDER_NUM_EMPTY);
            }
            if (!StringUtils.hasText(menu.getPath())) {
                throw new SystemException(ResultCodeEnum.MENU_PATH_EMPTY);
            }
            if (menu.getIcon() == null) {
                throw new SystemException(ResultCodeEnum.MENU_ICON_EMPTY);
            }
            if (menu.getParentId() == null) {
                throw new SystemException(ResultCodeEnum.MENU_PARENT_EMPTY);
            }
            if (!StringUtils.hasText(menu.getComponent())) {
                throw new SystemException(ResultCodeEnum.MENU_COMPONENT_EMPTY);
            }
            if (!StringUtils.hasText(menu.getPerms())) {
                throw new SystemException(ResultCodeEnum.MENU_PERMS_EMPTY);
            }
        } else if ("F".equals(menu.getMenuType())) {
            if (!StringUtils.hasText(menu.getMenuName())) {
                throw new SystemException(ResultCodeEnum.MENU_NAME_EMPTY);
            }
            if (menu.getOrderNum() == null) {
                throw new SystemException(ResultCodeEnum.MENU_ORDER_NUM_EMPTY);
            }
            if (menu.getParentId() == null) {
                throw new SystemException(ResultCodeEnum.MENU_PARENT_EMPTY);
            }
            if (!StringUtils.hasText(menu.getPerms())) {
                throw new SystemException(ResultCodeEnum.MENU_PERMS_EMPTY);
            }
        } else {
            throw new SystemException(ResultCodeEnum.MENU_TYPE_ERROR);
        }
    }
    /**
     * 添加菜单
     * @param menu
     * @return
     */
    @Override
    public ResponseResult<Void> addMenu(Menu menu) {

        paramsCheck(menu);

        menu.setCreateBy(AuthenticationUtil.getUserFromContextHolder().getId());
        menu.setCreateTime(new Date());
        // 数据库添加菜单
        menuMapper.insertIntoMenu(menu);

        return ResponseResult.ok(null);
    }


    /**
     * 根据id查询菜单信息
     * @param id
     * @return
     */
    @Override
    public ResponseResult<MenuVo> getMenuInfoById(Long id) {
        return ResponseResult.ok(menuMapper.selectMenuById(id));
    }


    /**
     * 修改菜单
     * @param menu
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> updateMenu(Menu menu) {

        paramsCheck(menu);

        if (menu.getId().equals(menu.getParentId())) {
            throw new SystemException(ResultCodeEnum.MENU_PARENT_ERROR);
        }
        // 查询是否有子菜单
        List<Long> childrenIdList = menuMapper.selectChildrenIdList(menu.getId());
        if (!"0".equals(menu.getStatus()) && !childrenIdList.isEmpty()) {
            throw new SystemException(ResultCodeEnum.HAVE_CHILDREN);
        }

        RLock rLock = redisson.getLock(menu.getId() + "Lock");
        try {
            rLock.lock();

            // 如果改菜单下有角色，或者是初始化菜单，不可禁用
            if (!"0".equals(menu.getStatus()) &&
                    (roleMenuMapper.selectRoleCountByMenuId(menu.getId()) != 0 ||
                    menu.getId() <= 2099)) {
                throw new SystemException(ResultCodeEnum.MENU_ENABLE_ERROR);
            }

            menu.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
            menu.setUpdateTime(new Date());
            menuMapper.updateMenu(menu);

            // todo 修改子菜单状态
            // 不用修改子菜单状态为1，因为在创建菜单树的时候筛选掉了，返回的结果没有该菜单的子菜单
            // 不过在sql查perms和router时会查到子菜单，服务层查perms返回的也会有子菜单

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> deleteMenu(Long menuId) {

        // 查询是否有子菜单
        List<Long> childrenIdList = menuMapper.selectChildrenIdList(menuId);
        if (!childrenIdList.isEmpty()) {
            throw new SystemException(ResultCodeEnum.HAVE_CHILDREN);
        }

        if (menuId <= 2099) {
            throw new SystemException(ResultCodeEnum.CAN_NOT_DELETE);
        }

        RLock rLock = redisson.getLock(menuId + "Lock");
        try {
            rLock.lock();

            // 如果改菜单下有角色，不可删除
            if (roleMenuMapper.selectRoleCountByMenuId(menuId) != 0) {
                throw new SystemException(ResultCodeEnum.MENU_ENABLE_ERROR);
            }

            // 数据库删除删除
            menuMapper.deleteMenu(menuId);

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     *  获取菜单树
     * @return
     */
    @Override
    public ResponseResult<List<MenuTreeVo>> getMenuTree() {

        List<MenuTreeVo> menuTreeVoList = menuMapper.selectMenuForTree();
        List<MenuTreeVo> menuTree = buildMenuTree(menuTreeVoList, 0L);
        return ResponseResult.ok(menuTree);
    }

    private List<MenuTreeVo> buildMenuTree(List<MenuTreeVo> menuTreeVoList, Long parentId) {
        return  menuTreeVoList.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .peek(m -> m.setChildren(getChildrenMenu(m, menuTreeVoList)))
                .toList();
    }
    private List<MenuTreeVo> getChildrenMenu(MenuTreeVo menu, List<MenuTreeVo> menuTreeVoList) {
        return menuTreeVoList.stream()
                .filter(m -> m.getParentId().equals(menu.getId()))
                .peek(m -> m.setChildren(getChildrenMenu(m, menuTreeVoList)))
                .toList();
    }


    /**
     * 获取角色菜单树
     * @param id
     * @return
     */
    @Override
    public ResponseResult<RoleUpdateVo> getRoleMenuTree(Long id) {

        // 获取菜单树
        List<MenuTreeVo> menuTree = getMenuTree().getData();
        // 获取当前角色的菜单的id
        List<Long> checkedIdList = roleMenuMapper.selectMenuIdListByRoleId(id);

        return ResponseResult.ok(new RoleUpdateVo(menuTree, checkedIdList));
    }
}
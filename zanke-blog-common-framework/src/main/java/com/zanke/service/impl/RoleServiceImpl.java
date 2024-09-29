package com.zanke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zanke.exception.SystemException;
import com.zanke.mapper.MenuMapper;
import com.zanke.mapper.RoleMapper;
import com.zanke.mapper.RoleMenuMapper;
import com.zanke.mapper.UserRoleMapper;
import com.zanke.pojo.dto.role.RoleListDto;
import com.zanke.pojo.dto.role.RoleStatusChangeDto;
import com.zanke.pojo.entity.Role;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.role.RoleVo;
import com.zanke.service.RoleService;
import com.zanke.util.AuthenticationUtil;
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
@Service("roleService")
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private MenuMapper menuMapper;


    /**
     * 分页查询所有角色列表(后台)
     *
     * @param pageNum
     * @param pageSize
     * @param roleListDto
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<RoleVo>>> getAllRoleForAdmin(int pageNum, int pageSize, RoleListDto roleListDto) {

        if (roleListDto.getRoleName() != null && roleListDto.getRoleName().isEmpty()) {
            roleListDto.setRoleName(null);
        }
        if (roleListDto.getStatus() != null && roleListDto.getStatus().isEmpty()) {
            roleListDto.setStatus(null);
        }

        List<RoleVo> roleVoList;
        try {
            PageHelper.startPage(pageNum, pageSize);
            roleVoList = roleMapper.selectAllRoleByRoleNameAndStatus(roleListDto);

        } finally {
            PageHelper.clearPage();
        }
        PageInfo<RoleVo> roleVoPageInfo = new PageInfo<>(roleVoList);
        return ResponseResult.ok(new PageVo<>(roleVoPageInfo.getTotal(), roleVoList));
    }


    /**
     * 修改角色状态
     *
     * @param roleStatusChangeDto
     * @return
     */
    @Override
    @Transactional
    public ResponseResult<Void> changeStatus(RoleStatusChangeDto roleStatusChangeDto) {

        Role role = new Role();
        role.setId(roleStatusChangeDto.getRoleId());
        role.setStatus(roleStatusChangeDto.getStatus());
        role.setUpdateTime(new Date());
        role.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());

        RLock rLock = redisson.getLock(roleStatusChangeDto.getRoleId() + "Lock");
        try {
            rLock.lock();

            // 该角色下有用户则不可禁用
            if (!"0".equals(role.getStatus()) && userRoleMapper.selectUserCountByRoleId(roleStatusChangeDto.getRoleId()) != 0) {
                throw new SystemException(ResultCodeEnum.ROLE_ENABLE_ERROR);
            }

            roleMapper.updateStatus(role);
            // 修改该角色和菜单的关系的状态
            roleMenuMapper.updateStatus(roleStatusChangeDto);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    @Override
    @Transactional
    public ResponseResult<Void> addRole(RoleVo roleVo) {

        if (!StringUtils.hasText(roleVo.getRoleName())) {
            throw new SystemException(ResultCodeEnum.ROLE_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(roleVo.getRoleKey())) {
            throw new SystemException(ResultCodeEnum.ROLE_KEY_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(roleVo.getRemark())) {
            throw new SystemException(ResultCodeEnum.ROLE_REMARK_EMPTY_ERROR);
        }

        List<RLock> menuIdLockList = new ArrayList<>();
        for (Long id : roleVo.getMenuIds()) {
            RLock rLock = redisson.getLock(id + "Lock");
            menuIdLockList.add(rLock);
        }
        try {
            for (RLock rLock : menuIdLockList) {
                rLock.lock();
            }

            // 判断菜单是否可用
            if (!roleVo.getMenuIds().isEmpty() && menuMapper.selectDisableMenuCountByMenuId(roleVo.getMenuIds()) != 0) {
                throw new SystemException(ResultCodeEnum.MENU_ILLEGAL_ERROR);
            }

            // 封装角色信息
            Role role = new Role();
            role.setRoleName(roleVo.getRoleName());
            role.setRoleKey(roleVo.getRoleKey());
            role.setRoleSort(roleVo.getRoleSort());
            role.setStatus(roleVo.getStatus());
            role.setRemark(roleVo.getRemark());
            role.setCreateBy(AuthenticationUtil.getUserFromContextHolder().getId());
            role.setCreateTime(new Date());

            // 加入数据库
            roleMapper.insert(role);

            // 添加角色和菜单的关系
            if (roleVo.getMenuIds() != null && !roleVo.getMenuIds().isEmpty()) {
                roleMenuMapper.insert(role.getId(), roleVo.getMenuIds(), role.getStatus());
            }

        } finally {
            for (RLock rLock : menuIdLockList) {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 获取角色详情
     * @param id
     * @return
     */
    @Override
    public ResponseResult<RoleVo> getRoleDetailById(Long id) {

        RoleVo roleVo = roleMapper.selectRoleById(id);
        return ResponseResult.ok(roleVo);
    }


    /**
     * 修改角色信息
     * @param roleVo
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> updateRole(RoleVo roleVo) {

        if (!StringUtils.hasText(roleVo.getRoleName())) {
            throw new SystemException(ResultCodeEnum.ROLE_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(roleVo.getRoleKey())) {
            throw new SystemException(ResultCodeEnum.ROLE_KEY_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(roleVo.getRemark())) {
            throw new SystemException(ResultCodeEnum.ROLE_REMARK_EMPTY_ERROR);
        }

        List<RLock> menuIdLockList = new ArrayList<>();
        for (Long id : roleVo.getMenuIds()) {
            RLock rLock = redisson.getLock(id + "Lock");
            menuIdLockList.add(rLock);
        }
        RLock rLock1 = redisson.getLock(roleVo.getId() + "Lock");
        try {
            for (RLock rLock : menuIdLockList) {
                rLock.lock();
            }
            rLock1.lock();

            // 判断菜单是否可用
            if (!roleVo.getMenuIds().isEmpty() && menuMapper.selectDisableMenuCountByMenuId(roleVo.getMenuIds()) != 0) {
                throw new SystemException(ResultCodeEnum.MENU_ILLEGAL_ERROR);
            }

            // 该角色下有用户则不可禁用
            if (!"0".equals(roleVo.getStatus()) && userRoleMapper.selectUserCountByRoleId(roleVo.getId()) != 0) {
                throw new SystemException(ResultCodeEnum.ROLE_ENABLE_ERROR);
            }

            // 修改角色信息
            Role role = new Role();
            role.setId(roleVo.getId());
            role.setRoleName(roleVo.getRoleName());
            role.setRoleKey(roleVo.getRoleKey());
            role.setRoleSort(roleVo.getRoleSort());
            role.setStatus(roleVo.getStatus());
            role.setRemark(roleVo.getRemark());
            role.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
            role.setUpdateTime(new Date());

            roleMapper.updateRole(role);
            // 删除原本的菜单和角色的关系
            roleMenuMapper.deleteByRoleId(roleVo.getId());
            if (!roleVo.getMenuIds().isEmpty()) {
                // 添加角色和菜单的关系
                roleMenuMapper.insert(roleVo.getId(), roleVo.getMenuIds(), roleVo.getStatus());
            }

        } finally {
            for (RLock rLock : menuIdLockList) {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
            if (rLock1.isHeldByCurrentThread()) {
                rLock1.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 删除角色
     * @param id
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> deleteRole(Long id) {

        RLock rLock = redisson.getLock(id + "Lock");
        try {
            rLock.lock();

            // 该角色下有用户则不可删除
            if (userRoleMapper.selectUserCountByRoleId(id) != 0) {
                throw new SystemException(ResultCodeEnum.ROLE_ENABLE_ERROR);
            }

            // 逻辑删除角色
            roleMapper.deleteRole(id);
            // 删除该角色和菜单的关系
            roleMenuMapper.deleteByRoleId(id);

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 获取所有角色
     * @return
     */
    @Override
    public ResponseResult<List<RoleVo>> listAllRole() {

        List<RoleVo> roleVoList = roleMapper.selectAllRoleByRoleNameAndStatus(new RoleListDto(null, "0"));
        return ResponseResult.ok(roleVoList);
    }
}
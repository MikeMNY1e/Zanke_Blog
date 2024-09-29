package com.zanke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zanke.exception.SystemException;
import com.zanke.mapper.RoleMapper;
import com.zanke.mapper.UserManageMapper;
import com.zanke.mapper.UserMapper;
import com.zanke.mapper.UserRoleMapper;
import com.zanke.pojo.dto.role.RoleListDto;
import com.zanke.pojo.dto.user.UserListDto;
import com.zanke.pojo.dto.user.UserStatusChangeDto;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.*;
import com.zanke.pojo.vo.role.RoleVo;
import com.zanke.pojo.vo.user.AddUpdateUserVo;
import com.zanke.pojo.vo.user.UserUpdateVo;
import com.zanke.pojo.vo.user.UserVo;
import com.zanke.service.UserManageService;
import com.zanke.util.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Slf4j
@Service("userManageService")
public class UserManageServiceImpl implements UserManageService {

    @Resource
    private UserManageMapper userManageMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redisson;


    /**
     * 获取用户列表(后台)
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<UserVo>>> getAdminUserList(int pageNum, int pageSize, UserListDto userListDto) {


        if (userListDto.getUserName() != null && userListDto.getUserName().isEmpty()) {
            userListDto.setUserName(null);
        }
        if (userListDto.getPhonenumber() != null && userListDto.getPhonenumber().isEmpty()) {
            userListDto.setPhonenumber(null);
        }
        if (userListDto.getStatus() != null && userListDto.getStatus().isEmpty()) {
            userListDto.setStatus(null);
        }

        List<UserVo> userVoList;
        try {
            // 开启分页
            PageHelper.startPage(pageNum, pageSize);
            // 查询
            userVoList = userManageMapper.selectUserByUserNameAndPhonenumAndStatus(userListDto);
        } finally {
            PageHelper.clearPage();
        }

        // 封装分页数据
        PageInfo<UserVo> userPageInfo = new PageInfo<>(userVoList);

        return ResponseResult.ok(new PageVo<>(userPageInfo.getTotal(), userPageInfo.getList()));
    }


    /**
     * 新增用户
     * @param addUserVo
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> addUser(AddUpdateUserVo addUserVo) {

        if (!StringUtils.hasText(addUserVo.getUserName())) {
            throw new SystemException(ResultCodeEnum.USERNAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(addUserVo.getNickName())) {
            throw new SystemException(ResultCodeEnum.NICKNAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(addUserVo.getPassword())) {
            throw new SystemException(ResultCodeEnum.PASSWORD_EMPTY_ERROR);
        }

        // 如果缓存中没有用户名集合，则从数据库中获取所有用户名，并加入缓存
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyEnum.USERNAME_SET_KEY.getKey()))) {
            List<String> usernameList = userMapper.selectAllUsername();
            redisTemplate.opsForSet().add(RedisKeyEnum.USERNAME_SET_KEY.getKey(), usernameList.toArray());
        }
        // 如果缓存中没有昵称集合，则从数据库中获取所有昵称，并加入缓存
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyEnum.NICKNAME_SET_KEY.getKey()))) {
            List<String> nickNameList = userMapper.selectAllNickName();
            redisTemplate.opsForSet().add(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), nickNameList.toArray());
        }

        String username = addUserVo.getUserName();
        String nickName = addUserVo.getNickName();
        User user = new User();

        RLock rLock1 = redisson.getLock(username + "Lock");
        RLock rLock2 = redisson.getLock(nickName + "Lock");
        List<RLock> menuIdLockList = new ArrayList<>();
        for (Long id : addUserVo.getRoleIds()) {
            RLock rLock = redisson.getLock(id + "Lock");
            menuIdLockList.add(rLock);
        }
        try {
            rLock1.lock();
            rLock2.lock();
            for (RLock rLock : menuIdLockList) {
                rLock.lock();
            }

            // 判断用户名和昵称是否重复
            Boolean usernameFlag = redisTemplate.opsForSet().isMember(RedisKeyEnum.USERNAME_SET_KEY.getKey(), username);
            if (Boolean.TRUE.equals(usernameFlag)) {
                throw new SystemException(ResultCodeEnum.USERNAME_USED);
            }
            Boolean nickNameFlag = redisTemplate.opsForSet().isMember(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), nickName);
            if (Boolean.TRUE.equals(nickNameFlag)) {
                throw new SystemException(ResultCodeEnum.NICK_NAME_USED);
            }

            // 判断角色是否可用
            if (!addUserVo.getRoleIds().isEmpty() && roleMapper.selectDisableRoleCountByRoleId(addUserVo.getRoleIds()) != 0) {
                throw new SystemException(ResultCodeEnum.ROLE_ILLEGAL_ERROR);
            }

            user.setUsername(addUserVo.getUserName());
            user.setNickName(addUserVo.getNickName());
            user.setPassword(passwordEncoder.encode(addUserVo.getPassword()));
            user.setSex(addUserVo.getSex());
            user.setStatus(addUserVo.getStatus());
            user.setPhonenumber(addUserVo.getPhonenumber());
            user.setEmail(addUserVo.getEmail());
            user.setCreateBy(AuthenticationUtil.getUserFromContextHolder().getId());
            user.setCreateTime(new Date());
            user.setType("1");
            // 数据库添加用户
            userManageMapper.insert(user);

            // 用户名和昵称加入缓存set
            // 原子操作，防止加了其中一个另一个没加
            String luaScript = """
                    local function atomicAdd()
                        redis.call('sadd', KEYS[1], ARGV[1])
                        redis.call('sadd', KEYS[2], ARGV[2])
                    end
                    return atomicAdd()""";

            try {
                redisTemplate.execute(
                        new DefaultRedisScript<>(luaScript, Long.class),
                        Arrays.asList(RedisKeyEnum.USERNAME_SET_KEY.getKey(), RedisKeyEnum.NICKNAME_SET_KEY.getKey()),
                        username,
                        nickName);
            } catch (Exception e) {
                log.error("缓存写入注册用户信息异常", e);
            }

        } finally {
            if (rLock1.isHeldByCurrentThread()) {
                rLock1.unlock();
            }
            if (rLock2.isHeldByCurrentThread()) {
                rLock2.unlock();
            }
            for (RLock rLock : menuIdLockList) {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
        }

        // 添加用户和角色的关系
        List<Long> roleIds = addUserVo.getRoleIds();
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.insertUserRole(user.getId(), roleIds, user.getStatus());
        }

        return ResponseResult.ok(null);
    }


    /**
     * 删除用户
     * @param id
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> deleteUser(Long id) {

        if (id == 1L || id == AuthenticationUtil.getUserFromContextHolder().getId()) {
            throw new SystemException(ResultCodeEnum.CAN_NOT_DELETE);
        }

        RLock rLock = redisson.getLock(id + "Lock");
        try {
            rLock.lock();
            // 数据库逻辑删除用户
            userManageMapper.deleteUser(id);
            // 删除该用户和角色的关系
            userRoleMapper.deleteByUserId(id);
            // 查询用户名
            User user = userManageMapper.selectUserById(id);

            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {

                    // 删除缓存中的用户信息
                    redisTemplate.opsForHash().delete(RedisKeyEnum.LOGINUSER_USERNAME_HASH_KEY.getKey(), RedisKeyEnum.LOGINUSER_USERNAME_FIELD_PREFIX.getKey() + user.getUsername());
                    // 删除用户名缓存
                    redisTemplate.opsForSet().remove(RedisKeyEnum.USERNAME_SET_KEY.getKey(), user.getUsername());
                    // 删除昵称缓存
                    redisTemplate.opsForSet().remove(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), user.getNickName());
                    return null;
                }
            });

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 获取用户详情
     * @param id
     * @return
     */
    @Override
    public ResponseResult<UserUpdateVo> getUserDetailById(Long id) {

        // 获取所有角色
        List<RoleVo> roleVoList = roleMapper.selectAllRoleByRoleNameAndStatus(new RoleListDto(null, "0"));
        // 获取当前用户角色id
        List<Long> roleIdList = userRoleMapper.selectRoleIdListByUserId(id);
        // 获取用户详情
        User user = userManageMapper.selectUserById(id);
        AddUpdateUserVo addUserVo = BeanCopyUtil.copyBean(user, AddUpdateUserVo.class);

        return ResponseResult.ok(new UserUpdateVo(roleIdList, roleVoList, addUserVo));
    }


    /**
     * 修改用户信息
     * @param addUpdateUserVo
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> updateUser(AddUpdateUserVo addUpdateUserVo) {

        if (!"0".equals(addUpdateUserVo.getStatus())
                && (addUpdateUserVo.getId() == 1L
                || Objects.equals(addUpdateUserVo.getId(), AuthenticationUtil.getUserFromContextHolder().getId()))) {
            throw new SystemException(ResultCodeEnum.CAN_NOT_FORBIDDEN);
        }

        if (!StringUtils.hasText(addUpdateUserVo.getNickName())) {
            throw new SystemException(ResultCodeEnum.NICKNAME_EMPTY_ERROR);
        }

        // 如果缓存中没有昵称集合，则从数据库中获取所有昵称，并加入缓存
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyEnum.NICKNAME_SET_KEY.getKey()))) {
            List<String> nickNameList = userMapper.selectAllNickName();
            redisTemplate.opsForSet().add(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), nickNameList.toArray());
        }

        RLock rLock1 = redisson.getLock(addUpdateUserVo.getId() + "Lock");
        RLock rLock2 = redisson.getLock(addUpdateUserVo.getNickName() + "Lock");
        List<RLock> menuIdLockList = new ArrayList<>();
        for (Long id : addUpdateUserVo.getRoleIds()) {
            RLock rLock = redisson.getLock(id + "Lock");
            menuIdLockList.add(rLock);
        }
        try {
            rLock1.lock();
            rLock2.lock();
            for (RLock rLock : menuIdLockList) {
                rLock.lock();
            }

            // 判断角色是否可用
            if (!addUpdateUserVo.getRoleIds().isEmpty() && roleMapper.selectDisableRoleCountByRoleId(addUpdateUserVo.getRoleIds()) != 0) {
                throw new SystemException(ResultCodeEnum.ROLE_ILLEGAL_ERROR);
            }

            // 获取旧用户信息
            User oldUser = userManageMapper.selectUserById(addUpdateUserVo.getId());

            // 判断昵称是否重复
            if (!oldUser.getNickName().equals(addUpdateUserVo.getNickName())) {

                Boolean nickNameFlag = redisTemplate.opsForSet().isMember(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), addUpdateUserVo.getNickName());
                if (Boolean.TRUE.equals(nickNameFlag)) {
                    throw new SystemException(ResultCodeEnum.NICK_NAME_USED);
                }
            }

            User user = new User();
            user.setId(addUpdateUserVo.getId());
            user.setNickName(addUpdateUserVo.getNickName());
            user.setSex(addUpdateUserVo.getSex());
            user.setStatus(addUpdateUserVo.getStatus());
            user.setEmail(addUpdateUserVo.getEmail());
            user.setPhonenumber(addUpdateUserVo.getPhonenumber());
            user.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
            user.setUpdateTime(new Date());

            // 数据库修改用户信息
            userManageMapper.updateUser(user);
            // 删除该用户和角色的关系
            userRoleMapper.deleteByUserId(addUpdateUserVo.getId());
            if (!addUpdateUserVo.getRoleIds().isEmpty()) {
                // 添加角色和菜单的关系
                userRoleMapper.insertUserRole(addUpdateUserVo.getId(), addUpdateUserVo.getRoleIds(), addUpdateUserVo.getStatus());
            }

            // 缓存删除旧用户信息
            redisTemplate.opsForHash().delete(RedisKeyEnum.LOGINUSER_USERNAME_HASH_KEY.getKey(), RedisKeyEnum.LOGINUSER_USERNAME_FIELD_PREFIX.getKey() + oldUser.getUsername());

            if (!oldUser.getNickName().equals(addUpdateUserVo.getNickName())) {
                // 如果修改了昵称，则从缓存中删除旧的昵称，并加入新的昵称
                redisTemplate.opsForSet().remove(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), oldUser.getNickName());
                redisTemplate.opsForSet().add(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), addUpdateUserVo.getNickName());
            }

        } finally {
            if (rLock1.isHeldByCurrentThread()) {
                rLock1.unlock();
            }
            if (rLock2.isHeldByCurrentThread()) {
                rLock2.unlock();
            }
            for (RLock rLock : menuIdLockList) {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     *  修改用户状态
     * @param userStatusChangeDto
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> changeStatus(UserStatusChangeDto userStatusChangeDto) {

        if (!"0".equals(userStatusChangeDto.getStatus())
                && (userStatusChangeDto.getUserId() == 1L
                || Objects.equals(userStatusChangeDto.getUserId(), AuthenticationUtil.getUserFromContextHolder().getId()))) {
            throw new SystemException(ResultCodeEnum.CAN_NOT_FORBIDDEN);
        }

        User user = new User();
        user.setId(userStatusChangeDto.getUserId());
        user.setStatus(userStatusChangeDto.getStatus());
        user.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
        user.setUpdateTime(new Date());
        // 数据库修改用户状态
        userManageMapper.updateStatus(user);
        // 修改用户和角色的关系
        userRoleMapper.updateStatus(userStatusChangeDto);

        // 查询用户名
        String username = userManageMapper.selectUsernameById(user.getId());
        // 删除缓存
        redisTemplate.opsForHash().delete(RedisKeyEnum.LOGINUSER_USERNAME_HASH_KEY.getKey(), RedisKeyEnum.LOGINUSER_USERNAME_FIELD_PREFIX.getKey() + username);

        return ResponseResult.ok(null);
    }
}

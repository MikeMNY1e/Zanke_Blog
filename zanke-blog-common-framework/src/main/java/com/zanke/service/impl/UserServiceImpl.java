package com.zanke.service.impl;

import com.zanke.exception.SystemException;
import com.zanke.mapper.UserMapper;
import com.zanke.pojo.entity.LoginUser;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.UserInfoVo;
import com.zanke.service.UserService;
import com.zanke.util.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Slf4j
@Service("userService")
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redisson;

    @Resource
    PasswordEncoder passwordEncoder;


    /**
     * 获取用户信息
     * @return
     */
    @Override
    public ResponseResult<UserInfoVo> getUserInfo() {

        User user = AuthenticationUtil.getUserFromContextHolder();
        UserInfoVo userInfo = BeanCopyUtil.copyBean(user, UserInfoVo.class);
        return ResponseResult.ok(userInfo);
    }


    /**
     * 修改用户信息
     * @param newUser
     * @return
     */
    @Override
    public ResponseResult<Void> updateUserInfo(User newUser) {

        if (!StringUtils.hasText(newUser.getNickName())) {
            throw new SystemException(ResultCodeEnum.NICKNAME_EMPTY_ERROR);
        }
        // 如果缓存中没有昵称集合，则从数据库中获取所有昵称，并加入缓存
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyEnum.NICKNAME_SET_KEY.getKey()))) {
            List<String> nickNameList = userMapper.selectAllNickName();
            redisTemplate.opsForSet().add(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), nickNameList.toArray());
        }

        RLock rLock1 = redisson.getLock(newUser.getId() + "Lock");
        RLock rLock2 = redisson.getLock(newUser.getNickName() + "Lock");
        try {
            rLock1.lock();
            rLock2.lock();

            User user = AuthenticationUtil.getUserFromContextHolder();

            if (!user.getNickName().equals(newUser.getNickName())) {
                Boolean nickNameFlag = redisTemplate.opsForSet().isMember(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), newUser.getNickName());
                if (Boolean.TRUE.equals(nickNameFlag)) {
                    throw new SystemException(ResultCodeEnum.NICK_NAME_USED);
                }
            }

            // 数据库修改用户信息
            newUser.setUpdateTime(new Date());
            int affectedRows = userMapper.update(newUser);
            if (affectedRows > 0) {

                newUser.setId(user.getId());
                newUser.setUsername(user.getUsername());
                newUser.setPassword(user.getPassword());
                newUser.setType(user.getType());
                newUser.setStatus(user.getStatus());
                newUser.setPhonenumber(user.getPhonenumber());
                newUser.setCreateTime(user.getCreateTime());
                newUser.setUpdateTime(user.getUpdateTime());
                newUser.setDelFlag(user.getDelFlag());
                LoginUser loginUser = new LoginUser(newUser, AuthenticationUtil.getPermsFromContextHolder());
                // 修改缓存中的用户信息hash的field的value
                redisTemplate.opsForHash().put(RedisKeyEnum.LOGINUSER_USERNAME_HASH_KEY.getKey(), RedisKeyEnum.LOGINUSER_USERNAME_FIELD_PREFIX.getKey() + user.getUsername(), loginUser);

                if (!newUser.getNickName().equals(user.getNickName())) {
                    // 如果修改了昵称
                    // 新的昵称加入缓存
                    redisTemplate.opsForSet().add(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), newUser.getNickName());
                    // 从缓存中删除原本的昵称
                    redisTemplate.opsForSet().remove(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), user.getNickName());
                }

            } else {
                return ResponseResult.build(null, 666, "修改用户信息失败");
            }
        } finally {
            if (rLock1.isHeldByCurrentThread()) {
                rLock1.unlock();
            }
            if (rLock2.isHeldByCurrentThread()) {
                rLock2.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 添加用户
     * @param user
     * @return
     */
    @Override
    public ResponseResult<Void> register(User user) {

        //对数据进行非空判断
        if(!StringUtils.hasText(user.getUsername())){
            throw new SystemException(ResultCodeEnum.USERNAME_EMPTY_ERROR);
        }
        if(!StringUtils.hasText(user.getPassword())){
            throw new SystemException(ResultCodeEnum.PASSWORD_EMPTY_ERROR);
        }
        if(!StringUtils.hasText(user.getEmail())){
            throw new SystemException(ResultCodeEnum.EMAIL_EMPTY_ERROR);
        }
        if(!StringUtils.hasText(user.getNickName())){
            throw new SystemException(ResultCodeEnum.NICKNAME_EMPTY_ERROR);
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

        String username = user.getUsername();
        String nickName = user.getNickName();

        RLock rLock1 = redisson.getLock(username + "Lock");
        RLock rLock2 = redisson.getLock(nickName + "Lock");
        rLock1.lock();
        rLock2.lock();
        try {
            // 判断用户名和昵称是否重复
            Boolean usernameFlag = redisTemplate.opsForSet().isMember(RedisKeyEnum.USERNAME_SET_KEY.getKey(), username);
            if (Boolean.TRUE.equals(usernameFlag)) {
                throw new SystemException(ResultCodeEnum.USERNAME_USED);
            }
            Boolean nickNameFlag = redisTemplate.opsForSet().isMember(RedisKeyEnum.NICKNAME_SET_KEY.getKey(), nickName);
            if (Boolean.TRUE.equals(nickNameFlag)) {
                throw new SystemException(ResultCodeEnum.NICK_NAME_USED);
            }

            // 加入数据库
            user.setCreateTime(new Date());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userMapper.insert(user);

            // 用户名和昵称加入缓存set
            // 原子操作，防止加了其中一个另一个没加
            String luaScript = """
                    local function atomicAdd(usernameSetKey, nicknameSetKey, username, nickname)
                        redis.call('sadd', KEYS[1], ARGV[1])
                        redis.call('sadd', KEYS[2], ARGV[2])
                    end
                    return atomicAdd(KEYS[1], KEYS[2], ARGV[1], ARGV[2])""";

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
        }

        return ResponseResult.ok(null);
    }
}
package com.zanke.service.impl;

import com.zanke.exception.SystemException;
import com.zanke.mapper.MenuMapper;
import com.zanke.mapper.UserMapper;
import com.zanke.pojo.entity.LoginUser;
import com.zanke.pojo.entity.User;
import com.zanke.util.RedisKeyEnum;
import com.zanke.util.ResultCodeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Slf4j
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RedisTemplate<String, LoginUser> redisTemplate;
    @Resource
    private RedissonClient redisson;


    /**
     * 根据用户名查询用户
     * @param username
     * @return UserDetails实现类LoginUser
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String field = RedisKeyEnum.LOGINUSER_USERNAME_FIELD_PREFIX.getKey() + username;

        LoginUser loginUser;

        //  查询缓存
        loginUser = redisTemplate.<String, LoginUser>opsForHash().get(RedisKeyEnum.LOGINUSER_USERNAME_HASH_KEY.getKey(), field);

        if (loginUser == null) {

            // 加分布式锁
            RLock rLock = redisson.getLock(RedisKeyEnum.LOGINUSER_USERNAME_FIELD_PREFIX.getKey() + username + "Lock");
            try {
                rLock.lock();

                try {
                    // 再次查询缓存
                    loginUser = redisTemplate.<String, LoginUser>opsForHash().get(RedisKeyEnum.LOGINUSER_USERNAME_HASH_KEY.getKey(), field);
                } catch (Exception e) {
                    log.error("缓存查询登录用户信息异常", e);
                }

                if (loginUser == null) {
                    User user = null;
                    List<String> perms = null;
                    try {
                        //查数据库
                        user = userMapper.selectByUsername(username);
                        if (user != null && "1".equals(user.getType())) {
                            // 管理员则查询权限
                            if (user.getId() == 1L) {
                                perms = menuMapper.selectAllPerms();
                            } else {
                                perms = menuMapper.selectPermsByUserId(user.getId());
                            }
                        }

                    } catch (Exception e) {
                        log.error("登录用户信息查询数据库失败", e);
                    }

                    // 写入缓存
                    if (user != null) {
                        try {
                            loginUser = new LoginUser(user, perms);
                            redisTemplate.<String, LoginUser>opsForHash().put(RedisKeyEnum.LOGINUSER_USERNAME_HASH_KEY.getKey(), field, loginUser);
                        } catch (Exception e) {
                            log.error("登录用户信息写入缓存失败", e);
                        }
                    } else {
                        throw new SystemException(ResultCodeEnum.USER_NOT_EXIST);
                    }
                }

            } finally {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
        }

        return loginUser;
    }
}

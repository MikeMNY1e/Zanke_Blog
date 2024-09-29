package com.zanke.service.impl;

import com.zanke.exception.SystemException;
import com.zanke.pojo.entity.LoginUser;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.LoginUserInfoVo;
import com.zanke.pojo.vo.user.UserInfoVo;
import com.zanke.service.LoginService;
import com.zanke.util.BeanCopyUtil;
import com.zanke.util.JwtUtil;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Slf4j
@Service("blogLoginService")
public class LoginServiceImpl implements LoginService {

    @Resource
    private AuthenticationManager authenticationManager;


    /**
     * 用户登录
     * @param user
     * @return
     */
    @Override
    public ResponseResult<LoginUserInfoVo> login(User user, boolean isAdmin) {

        if (!StringUtils.hasText(user.getUsername())) {
            throw new SystemException(ResultCodeEnum.USERNAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new SystemException(ResultCodeEnum.PASSWORD_EMPTY_ERROR);
        }

        //  用户名密码封装
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        // 认证
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        if (Objects.isNull(authenticate)) {
            // 认证失败
            throw new SystemException(ResultCodeEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        // 获取用户信息
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();

        if (isAdmin) {
            // 如果是后台登录，需要校验是不是管理员
            if (!"1".equals(loginUser.getUser().getType())) {
                throw new SystemException(ResultCodeEnum.USER_NOT_ADMIN);
            }
        }

        // 根据 username 生成 token
        String token = JwtUtil.createJWT(loginUser.getUsername());

        UserInfoVo userInfo = BeanCopyUtil.copyBean(loginUser.getUser(), UserInfoVo.class);

        return ResponseResult.ok(new LoginUserInfoVo(token, userInfo));
    }
}

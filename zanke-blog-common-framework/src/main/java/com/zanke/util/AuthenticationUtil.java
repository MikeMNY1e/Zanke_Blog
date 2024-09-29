package com.zanke.util;

import com.zanke.pojo.entity.LoginUser;
import com.zanke.pojo.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public class AuthenticationUtil {

    /**
     * 获取当前登录用户User
     * @return
     */
    public static User getUserFromContextHolder() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getUser();
    }


    /**
     * 获取当前登录用户权限
     * @return
     */
    public static List<String> getPermsFromContextHolder() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getPermissions();
    }
}

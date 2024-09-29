package com.zanke.service.impl;

import com.zanke.util.AuthenticationUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Service("permission")
public class PermissionService {

    /**
     * 判断当前用户是否拥有该权限
     * @param permission
     * @return
     */
    public boolean hasPermission(String permission) {

        List<String> perms = AuthenticationUtil.getPermsFromContextHolder();
        return perms.contains(permission);
    }
}

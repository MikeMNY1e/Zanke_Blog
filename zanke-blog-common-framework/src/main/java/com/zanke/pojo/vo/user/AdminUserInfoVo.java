package com.zanke.pojo.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminUserInfoVo {

    private List<String> permissions;
    private List<String> roles;
    private UserInfoVo user;
}

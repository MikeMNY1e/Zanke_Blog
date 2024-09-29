package com.zanke.pojo.vo.user;

import com.zanke.pojo.vo.role.RoleVo;
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
public class UserUpdateVo {

    private List<Long> roleIds;
    private List<RoleVo> roles;
    private AddUpdateUserVo user;
}

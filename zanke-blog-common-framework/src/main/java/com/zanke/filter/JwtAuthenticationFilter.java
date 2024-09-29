package com.zanke.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zanke.pojo.entity.LoginUser;
import com.zanke.util.JwtUtil;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import com.zanke.util.WebUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Zanke
 * @version 1.0.0
 * @description jwt token认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //获取token
        String token = request.getHeader("token");

        //没有token直接放行，留给后面的过滤器认证
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }


        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (Exception e) {
            // token过期 或者 非法

            // 返回消息，让前端提示重新登录
            ResponseResult<Void> responseResult = ResponseResult.build(null, ResultCodeEnum.NEED_LOGIN);

            WebUtil.responseJson(response, responseResult);
            return;
        }

        String username = claims.getSubject();
        // 查询缓存
        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(username);

        // 将UserDetails实现类封装到Authentication中
        UsernamePasswordAuthenticationToken authenticationToken =
                             new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

        // 将Authentication存入SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}
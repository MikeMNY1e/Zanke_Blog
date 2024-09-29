package com.zanke.config;

import com.zanke.filter.JwtAuthenticationFilter;
import com.zanke.handler.AccessDeniedHandlerImpl;
import com.zanke.handler.AuthenticationEntryPointHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


/**
 * @author Zanke
 * @version 1.0.0
 * @description 配置SpringSecurity
 */
@Configuration
public class SecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Resource
    private AuthenticationEntryPointHandler authenticationEntryPointHandler;
    @Resource
    private AccessDeniedHandlerImpl accessDeniedHandlerImpl;


    /**
     * 配置加密
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 配置认证管理器
     * @param authenticationConfiguration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * 配置安全过滤器链
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)  //关闭csrf
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 设置会话创建策略为无状态
                                .maximumSessions(1) // 设置最大并发会话数为1
                                .maxSessionsPreventsLogin(false) // 超过最大会话数时是否阻止新的登录尝试
                )
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login").anonymous() // 登录接口匿名访问
//                        .requestMatchers("/logout").authenticated() // 登出接口需要认证
                        .requestMatchers("/comment").authenticated() // 发表评论接口需要认证
                        .requestMatchers("/user/userInfo").authenticated() // 获取用户信息接口需要认证
                        .requestMatchers("/getInfo").authenticated() // 获取用户信息接口需要认证
                        .requestMatchers("/getRouters").authenticated() // 获取路由信息接口需要认证
                        .requestMatchers("/system/menu").authenticated()
                        .requestMatchers("/system/menu/**").authenticated()
                        .requestMatchers("/system/role").authenticated()
                        .requestMatchers("/system/role/**").authenticated()
                        .requestMatchers("/system/user").authenticated()
                        .requestMatchers("/system/user/**").authenticated()
                        .requestMatchers("/content/article").authenticated()
                        .requestMatchers("/content/article/**").authenticated()
                        .requestMatchers("/content/category").authenticated()
                        .requestMatchers("/content/category/**").authenticated()
                        .requestMatchers("/content/link").authenticated()
                        .requestMatchers("/content/link/**").authenticated()
                        .requestMatchers("/content/tag").authenticated()
                        .requestMatchers("/content/tag/**").authenticated()
                        .anyRequest().permitAll()  //其他接口都不需要认证
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(accessDeniedHandlerImpl)   // 添加权限不足处理器
                        .authenticationEntryPoint(authenticationEntryPointHandler)) // 添加认证失败处理器
                .cors(cors -> cors.configurationSource(request -> {   //跨域访问配置
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.addAllowedOrigin("*");
                    corsConfiguration.addAllowedMethod("*");
                    corsConfiguration.addAllowedHeader("*");
                    return corsConfiguration;
                }))
                // 关闭默认注销
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}

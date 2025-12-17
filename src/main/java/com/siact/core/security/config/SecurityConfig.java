package com.siact.core.security.config;

import com.siact.core.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 配置Spring Security的HTTP请求安全策略
     *
     * @param http HttpSecurity对象，用于配置请求的安全控制策略
     * @throws Exception 抛出配置过程中的异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /* 禁用CSRF保护机制（适用于无状态API场景） */
        http
                .csrf().disable()
                /* 配置请求授权规则 */
                .authorizeRequests()
                /* 允许/login端点匿名访问 */
                //.antMatchers("/auth/login").permitAll()
                // 所有地址均可通过
                .antMatchers("/**").permitAll()
                /* 其他所有请求需要认证 */
                .anyRequest().authenticated()
                .and()
                /* 在默认认证过滤器前添加JWT认证过滤器
                 * 优先级高于用户名密码认证流程 */
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 创建并注册JWT认证过滤器Bean
     * <p>
     * 本方法用于在Spring应用上下文中配置JwtAuthenticationFilter的实例，该过滤器通常用于：
     * - 拦截HTTP请求
     * - 解析并验证请求头中的JWT令牌
     * - 将认证信息设置到SecurityContext中
     *
     * @return JwtAuthenticationFilter 实例，该Bean将被自动添加到Spring的过滤器链中。
     * 具体执行顺序可通过FilterRegistrationBean进行配置
     * @Bean 注解表明该方法将产生一个由Spring容器管理的Bean，Bean名称默认为方法名（jwtAuthenticationFilter）
     * 通常需要配合Spring Security配置类共同使用，确保过滤器被正确添加到安全过滤器链中
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * 创建并配置Spring Security密码编码器Bean
     * <p>
     * 实现说明：
     * 1. 采用BCrypt强哈希算法进行密码加密，符合Spring Security官方推荐方案
     * 2. 自动处理盐值生成逻辑，每个加密结果包含唯一随机盐（符合OWASP安全标准）
     * 3. 默认strength参数为10，提供2048次哈希迭代（平衡安全性与性能）
     * 4. 支持抗彩虹表攻击特性，相同明文每次加密产生不同哈希值
     *
     * @return BCryptPasswordEncoder 实例
     * 具备以下特征：
     * - 符合PasswordEncoder接口规范
     * - 自动处理盐值存储（哈希结果包含版本、强度参数和盐值）
     * - 内置安全机制防止时序攻击
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 初始化BCrypt加密器（根据NIST建议选择算法版本）
        return new BCryptPasswordEncoder();
    }

}

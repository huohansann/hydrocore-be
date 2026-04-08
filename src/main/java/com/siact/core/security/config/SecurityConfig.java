package com.siact.core.security.config;

import com.siact.core.security.filter.JwtAuthenticationFilter;
import com.siact.core.security.handler.AuthenticationEntryPointImpl;
import com.siact.core.security.handler.LogoutHandlerImpl;
import com.siact.core.security.handler.LogoutSuccessHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .logout()
                    .logoutUrl("/auth/logout")
                    .addLogoutHandler(logoutHandler())
                    .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                .authorizeRequests()
                    .antMatchers("/auth/login").permitAll()
                    .antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs").permitAll()
                    .antMatchers("/ws").permitAll()
                    .antMatchers("/favicon.ico").permitAll()
                    .antMatchers("/algorithm/*").permitAll()
                    .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), LogoutFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public AuthenticationEntryPointImpl authenticationEntryPoint() {
        return new AuthenticationEntryPointImpl();
    }

    @Bean
    public LogoutHandlerImpl logoutHandler() {
        return new LogoutHandlerImpl();
    }

    @Bean
    public LogoutSuccessHandlerImpl logoutSuccessHandler() {
        return new LogoutSuccessHandlerImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

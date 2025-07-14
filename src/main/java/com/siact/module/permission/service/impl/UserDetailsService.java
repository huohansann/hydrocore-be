package com.siact.module.permission.service.impl;

import com.siact.module.permission.cache.RedisUserCache;
import com.siact.module.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.Setter; // 引入 Lombok 的 Setter

@Service
public class UserDetailsService {

    @Autowired
    private UserService userService;

    @Setter // 使用 Lombok 自动生成 setter 方法
    private RedisUserCache userCache;

    public UserDetailsService( RedisUserCache userCache,UserService userService) {
        this.userCache = userCache;
        this.userService = userService;
    }


    public UserDetails loadUserByAccount(String account) throws UsernameNotFoundException {
//        UserDetails user = this.userCache.getUserFromCache(account);
//        if (user == null) {
            return getUserDetailsAndRedis(account);
//        }
//        return user;
    }

    /**
     * 从数据库中获取用户信息并缓存到 Redis
     *
     * @param account 账号
     * @return 用户信息
     */
    public UserDetails getUserDetailsAndRedis(String account) {
        UserDetails user;
        user = this.userService.loadUserByAccount(account);
        if (user==null) {
            throw new UsernameNotFoundException("用户名或密码错误!");
        }
        // this.userCache.putUserInCache(user);
        return user;
    }
}

package com.siact.module.permission.service;

import com.siact.module.permission.cache.RedisUserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.Setter; // 引入 Lombok 的 Setter

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Setter // 使用 Lombok 自动生成 setter 方法
    private RedisUserCache userCache;

    public UserDetailsServiceImpl( RedisUserCache userCache,UserService userServic) {
        this.userCache = userCache;
        this.userService = userServic;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UserDetails user = this.userCache.getUserFromCache(username);
//        if (user == null) {
            return getUserDetailsAndRedis(username);
//        }
//        return user;
    }

    /**
     * 从数据库中获取用户信息并缓存到 Redis
     *
     * @param username 用户名
     * @return 用户信息
     */
    public UserDetails getUserDetailsAndRedis(String username) {
        UserDetails user;
        user = this.userService.loadUserByUsername(username);
        if (user==null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        this.userCache.putUserInCache(user);
        return user;
    }
}

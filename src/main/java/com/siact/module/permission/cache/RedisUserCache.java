package com.siact.module.permission.cache;

import com.siact.module.permission.dto.UserLoginInfoDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RedisUserCache implements UserCache {

    private static final String CACHE_KEY_PREFIX = "siact_kiln_user_cache:";
    private final RedisTemplate<String, UserLoginInfoDTO> redisTemplate;

    public RedisUserCache(RedisTemplate<String, UserLoginInfoDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void putUserInCache(UserDetails user) {
        UserLoginInfoDTO userLoginInfoDTO = new UserLoginInfoDTO();
        userLoginInfoDTO.setUsername(user.getUsername());
        userLoginInfoDTO.setPassword(user.getPassword());
        userLoginInfoDTO.setAuthorities(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        String key = getKey(user.getUsername());
        redisTemplate.opsForValue().set(key, userLoginInfoDTO);
    }

    @Override
    public UserDetails getUserFromCache(String username) {
        String key = getKey(username);
        UserLoginInfoDTO userDetails = redisTemplate.opsForValue().get(key);
        return userDetails !=null ? userDetails.toUserDetails():null;
    }

    @Override
    public void removeUserFromCache(String username) {
        String key = getKey(username);
        redisTemplate.delete(key);
    }

    private String getKey(String username) {
        return CACHE_KEY_PREFIX + username;
    }
}

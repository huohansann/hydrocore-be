package com.siact.module.permission.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @desc:
 * @author: wr
 * @create: 2025-05-19 11:31
 */
@Data
@Accessors(chain = true)
public class UserLoginInfoDTO implements Serializable {
    private String username;
    private String password;
    private List<String> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    public UserDetails toUserDetails() {
        List<GrantedAuthority> collect = authorities!=null? authorities.stream().map(roleId -> new SimpleGrantedAuthority(roleId)).collect(Collectors.toList()) :  Collections.emptyList();
        return new User(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, collect);
    }
}
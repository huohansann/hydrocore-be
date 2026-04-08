package com.siact.module.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.siact.common.context.LoginContext;
import com.siact.common.utils.JwtUtil;
import com.siact.module.system.command.LoginCommand;
import com.siact.module.system.command.ModifyPasswordCommand;
import com.siact.module.system.dto.LoginUser;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.repository.SysUserRepository;
import com.siact.module.system.service.AuthService;
import com.siact.module.system.service.SysUserService;
import com.siact.module.system.vo.SysMenuTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepository;
    private final SysUserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public String login(LoginCommand command) {
        SysUserEntity user = userRepository.findByAccount(command.getAccount());
        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }
        if (!Boolean.TRUE.equals(user.getStatus())) {
            throw new RuntimeException("账号已被停用");
        }
        if (!BCrypt.checkpw(command.getPassword(), user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setAccount(user.getAccount());
        loginUser.setUsername(user.getUsername());

        return jwtUtil.generateToken(loginUser);
    }

    @Override
    public void logout(String token) {
        LoginUser currentUser = LoginContext.getUser();
        if (currentUser == null && token != null) {
            currentUser = jwtUtil.parseTokenAllowExpired(token);
        }
        if (currentUser != null) {
            jwtUtil.deleteToken(token);
            jwtUtil.deleteRefreshTokens(currentUser.getId());
        }
        LoginContext.clear();
    }

    @Override
    public void modifyPassword(String token, LoginUser currentUser, ModifyPasswordCommand command) {
        SysUserEntity user = userRepository.findByAccount(currentUser.getAccount());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!BCrypt.checkpw(command.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        SysUserEntity update = new SysUserEntity();
        update.setId(user.getId());
        update.setPassword(BCrypt.hashpw(command.getNewPassword()));
        userService.updateById(update);

        // 修改密码后强制重新登录，删除当前 token 和所有刷新窗口
        jwtUtil.deleteToken(token);
        jwtUtil.deleteRefreshTokens(currentUser.getId());
    }

    @Override
    public LoginUser getCurrentUser(LoginUser currentUser) {
        SysUserEntity user = userRepository.findByAccount(currentUser.getAccount());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setAccount(user.getAccount());
        loginUser.setUsername(user.getUsername());
        return loginUser;
    }

    @Override
    public List<SysMenuTreeVO> getCurrentUserMenus(LoginUser currentUser) {
        return userService.getUserMenus(currentUser.getId());
    }
}

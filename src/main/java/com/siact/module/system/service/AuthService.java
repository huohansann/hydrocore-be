package com.siact.module.system.service;

import com.siact.module.system.command.LoginCommand;
import com.siact.module.system.command.ModifyPasswordCommand;
import com.siact.module.system.dto.LoginUser;
import com.siact.module.system.vo.SysMenuTreeVO;

import java.util.List;

public interface AuthService {

    String login(LoginCommand command);

    void logout(String token);

    void modifyPassword(String token, LoginUser currentUser, ModifyPasswordCommand command);

    LoginUser getCurrentUser(LoginUser currentUser);

    List<SysMenuTreeVO> getCurrentUserMenus(LoginUser currentUser);
}

package com.siact.hydrocore.module.system.controller;

import com.siact.hydrocore.common.context.LoginContext;
import com.siact.hydrocore.module.system.command.LoginCommand;
import com.siact.hydrocore.module.system.command.ModifyPasswordCommand;
import com.siact.hydrocore.module.system.dto.LoginUser;
import com.siact.hydrocore.module.system.service.AuthService;
import com.siact.hydrocore.module.system.vo.SysMenuTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginCommand command) {
        return authService.login(command);
    }

    @ApiOperation("修改密码")
    @PostMapping("/modify-password")
    public void modifyPassword(@Valid @RequestBody ModifyPasswordCommand command, HttpServletRequest request) {
        String token = resolveToken(request);
        LoginUser currentUser = LoginContext.getUser();
        authService.modifyPassword(token, currentUser, command);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public LoginUser getCurrentUser() {
        LoginUser currentUser = LoginContext.getUser();
        return authService.getCurrentUser(currentUser);
    }

    @ApiOperation("获取当前用户菜单树")
    @GetMapping("/menus")
    public List<SysMenuTreeVO> getCurrentUserMenus() {
        LoginUser currentUser = LoginContext.getUser();
        return authService.getCurrentUserMenus(currentUser);
    }

    @ApiOperation("获取下载验证token")
    @GetMapping("/download/token")
    public String generateDownloadToken() {
        LoginUser currentUser = LoginContext.getUser();
        return authService.generateDownloadToken(currentUser);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

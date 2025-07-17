package com.siact.module.permission.controller;


import com.siact.common.result.R;
import com.siact.module.permission.dto.LoginRequest;
import com.siact.module.permission.dto.UserModifyPwdDTO;
import com.siact.module.permission.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @desc:
 * @author: wr
 * @create: 2025-05-10 16:26
 */
@Slf4j
@Api(tags = "权限管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public R login(@RequestBody LoginRequest request) {
        R r;
        try {
            r = R.data(authService.login(request));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            r = R.fail(e.getMessage());
        }
        return r;
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public R logout(HttpServletRequest request) {
        R r;
        try {
            r = R.data(authService.logout(request));
        }catch (Exception e){
            log.error(e.getMessage(), e);
            r = R.fail(e.getMessage());
        }
        return r;
    }

    @ApiOperation("修改密码")
    @PostMapping("/modifyPwd")
    public R modifyPwd(HttpServletRequest request,@RequestBody UserModifyPwdDTO modifyPwdDTO) {
        R r;
        try {
            authService.modifyPwd(request, modifyPwdDTO);
            return R.data(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            r = R.fail(e.getMessage());
        }
        return r;
    }


}
package com.siact.module.permission.service;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.permission.dto.LoginRequest;
import com.siact.module.permission.dto.UserModifyPwdDTO;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {

    /**
     * 登录
     * @param request
     * @return
     */
    JSONObject login(LoginRequest request);

    /**
     * 登出
     * @param request
     * @return
     */
    boolean logout(HttpServletRequest request);

    /**
     * 修改密码
     * @param modifyPwdDTO
     */
    void modifyPwd(HttpServletRequest request, UserModifyPwdDTO modifyPwdDTO);
}

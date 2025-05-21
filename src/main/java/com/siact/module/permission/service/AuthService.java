package com.siact.module.permission.service;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.permission.dto.LoginRequest;

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
}

package com.siact.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.permission.dto.UserTokenDTO;

/**
 * 获取用户登录信息util
 */
public class LoginUntil {

    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(String user) {
        currentUser.set(user);
    }

    public static UserTokenDTO getCurrentUser() {
        return JSONObject.parseObject(currentUser.get(), UserTokenDTO.class);
    }

    public static void clear() {
        currentUser.remove();
    }

}

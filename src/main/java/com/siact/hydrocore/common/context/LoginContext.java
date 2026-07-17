package com.siact.hydrocore.common.context;

import com.siact.hydrocore.module.system.dto.LoginUser;

public class LoginContext {
    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getId() : null;
    }

    public static String getAccount() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getAccount() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}

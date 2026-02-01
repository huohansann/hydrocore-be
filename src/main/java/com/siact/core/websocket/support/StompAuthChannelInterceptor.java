package com.siact.core.websocket.support;

import com.alibaba.fastjson2.JSON;
import com.siact.common.constant.HttpStatus;
import com.siact.common.exception.StompAuthException;
import com.siact.common.utils.JwtUtil;
import com.siact.common.utils.LoginUntil;
import com.siact.module.permission.dto.UserTokenDTO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-08 11:19
 * @className : StompAuthChannelInterceptor
 * @description : stomp connect 鉴权拦截器
 */
@RequiredArgsConstructor
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil util;

    @Nullable
    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (Objects.isNull(accessor)) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) throw new StompAuthException(HttpStatus.UNAUTHORIZED, "Authorization header missing");

            String token = authorization.substring(7);
            UserTokenDTO user = parseToken(token);

            if (Objects.isNull(user)) throw new StompAuthException(401, "Invalid token");
            LoginUntil.setCurrentUser(JSON.toJSONString(user));
        }
        return message;
    }

    private UserTokenDTO parseToken(String token) {
        if (Objects.isNull(token)) return null;
        try {
            return util.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }
}

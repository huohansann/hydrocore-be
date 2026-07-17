package com.siact.hydrocore.core.websocket.support;

import com.siact.hydrocore.common.context.LoginContext;
import com.siact.hydrocore.common.constant.HttpStatus;
import com.siact.hydrocore.common.exception.StompAuthException;
import com.siact.hydrocore.common.utils.JwtUtil;
import com.siact.hydrocore.module.system.dto.LoginUser;
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

@RequiredArgsConstructor
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    @Nullable
    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (Objects.isNull(accessor)) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new StompAuthException(HttpStatus.UNAUTHORIZED, "Authorization header missing");
            }

            String token = authorization.substring(7);
            LoginUser user = parseToken(token);

            if (Objects.isNull(user)) {
                throw new StompAuthException(401, "Invalid token");
            }
            LoginContext.setUser(user);
        }
        return message;
    }

    private LoginUser parseToken(String token) {
        if (Objects.isNull(token)) return null;
        try {
            if (!jwtUtil.isTokenValid(token)) return null;
            return jwtUtil.parseToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}

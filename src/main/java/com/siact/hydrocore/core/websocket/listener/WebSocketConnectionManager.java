package com.siact.hydrocore.core.websocket.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-07 11:16
 * @className : WebSocketConnectionManager
 * @description : WebSocket 连接管理
 */
@Slf4j
@Component
public class WebSocketConnectionManager {
    // 存储用户会话信息：userId -> sessionId
    private final ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>();

    public @EventListener void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String userId = headers.getFirstNativeHeader("userId");

        if (StringUtils.isNotBlank(sessionId) && StringUtils.isNotBlank(userId)) {
            userSessions.put(userId, sessionId);
            log.info("用户 {} 已连接, sessionId: {}", userId, sessionId);
        }
    }

    public @EventListener void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();

        // 根据 sessionId 找到并移除用户
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
        log.info("用户断开连接, sessionId: {}", sessionId);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String userId) {
        return userSessions.containsKey(userId);
    }
}

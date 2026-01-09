package com.siact.core.websocket.support;

import com.siact.common.exception.StompAuthException;
import com.siact.common.utils.JacksonUtils;
import com.siact.common.utils.MapUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-08 11:58
 * @className : StompErrorHandler
 * @description : WebSocket 异常处理器
 */
@Component
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    @Nullable
    @Override
    public Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, Throwable ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof StompAuthException) {
            StompAuthException sae = (StompAuthException) cause;
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
            accessor.setContentType(MediaType.APPLICATION_JSON);
            accessor.setLeaveMutable(true);

            // 构建错误响应体
            Map<String, ? extends Serializable> responseBody = MapUtils.of("code", sae.getCode(), "message", sae.getMessage(), "timestamp", System.currentTimeMillis());

            try {
                return MessageBuilder.createMessage(JacksonUtils.toJsonBytes(responseBody), accessor.getMessageHeaders());
            } catch (Exception e) {
                String message = String.format("{\"code\":%d,\"message\":\"%s\"}", sae.getCode(), sae.getMessage());
                return MessageBuilder.createMessage(message.getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
            }
        }
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }
}

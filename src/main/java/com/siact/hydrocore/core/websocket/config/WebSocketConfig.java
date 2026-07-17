package com.siact.hydrocore.core.websocket.config;

import com.siact.hydrocore.core.websocket.support.StompAuthChannelInterceptor;
import com.siact.hydrocore.core.websocket.support.StompErrorHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-07 11:12
 * @className : WebSocketConfig
 * @description : WebSocket 配置类
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompAuthChannelInterceptor interceptor;
    private final StompErrorHandler handler;

    @Override
    public void configureMessageBroker(@NotNull MessageBrokerRegistry registry) {
        // 客户端订阅前缀
        registry.enableSimpleBroker(
                "/topic", // 广播
                "/queue" // 点对点
        );
        // 客户端消息发送前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 点对点用户前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NotNull StompEndpointRegistry registry) {
        // 注册 WebSocket 端点, 允许跨域
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        // 注册错误处理器
        registry.setErrorHandler(handler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // stomp 鉴权
        registration.interceptors(interceptor);
    }
}

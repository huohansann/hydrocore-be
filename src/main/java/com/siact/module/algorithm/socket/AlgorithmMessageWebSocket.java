package com.siact.module.algorithm.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-07 11:26
 * @className : AlgorithmMessageWebSocket
 * @description : 算法消息通知
 */
@RequiredArgsConstructor
@Component
public class AlgorithmMessageWebSocket {
    private final SimpMessagingTemplate template;

    public void intelliUpdate() {
        template.convertAndSend("/topic/intelli-update", "智控算法输出已更新");
    }
}

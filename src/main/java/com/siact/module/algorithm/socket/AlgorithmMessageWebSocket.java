package com.siact.module.algorithm.socket;

import com.siact.core.alarm.KictonePlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-07 11:26
 * @className : AlgorithmMessageWebSocket
 * @description : 算法消息通知
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AlgorithmMessageWebSocket {

    private final KictonePlayer kictonePlayer;

    public void intelliUpdate() {
        intelliUpdate("status");
    }

    public void intelliUpdate(String notifyType) {
        playNotify(notifyType);
    }

    @Async
    public void playNotify(String notifyType) {
        try {
            kictonePlayer.play(KictonePlayer.ToneType.ALARM, 10_000);
        } catch (Exception e) {
            log.error("播放通知音失败: {}", e.getMessage(), e);
        }
    }
}

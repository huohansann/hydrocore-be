package com.siact.module.algorithm.socket;

import com.siact.common.config.KilnProperty;
import com.siact.common.utils.SshUtils;
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

    private static final String NOTIFY_SCRIPT = "bash /home/software/microservice-docker-jars/kic-notify.sh";

    private final KilnProperty property;

    public void intelliUpdate() {
        intelliUpdate("status");
    }

    public void intelliUpdate(String notifyType) {
        playNotify(notifyType);
    }

    @Async
    public void playNotify(String notifyType) {
        try {
            KilnProperty.Algorithm.Ssh ssh = property.getAlgorithm().getSsh();
            String command = NOTIFY_SCRIPT + " " + notifyType;
            SshUtils.execute(
                    ssh.getHost(), ssh.getPort(),
                    ssh.getUsername(), ssh.getPassword(),
                    ssh.getPrivateKeyPath(), ssh.getTimeout(),
                    command
            );
        } catch (Exception e) {
            log.error("播放通知音失败: {}", e.getMessage(), e);
        }
    }
}
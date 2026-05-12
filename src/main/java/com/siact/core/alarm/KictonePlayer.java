package com.siact.core.alarm;

import com.siact.common.config.KilnProperty;
import com.siact.common.utils.SshUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class KictonePlayer {

    private static final String KICTONE_CMD = "kictone";

    private final KilnProperty.Algorithm.Ssh ssh;
    private final AtomicBoolean playing = new AtomicBoolean(false);

    public KictonePlayer(KilnProperty property) {
        this.ssh = property.getAlgorithm().getSsh();
    }

    public void startLoop(ToneType type) {
        String command = KICTONE_CMD + " start " + type.name().toLowerCase();
        try {
            SshUtils.execute(
                    ssh.getHost(), ssh.getPort(),
                    ssh.getUsername(), ssh.getPassword(),
                    ssh.getPrivateKeyPath(), ssh.getTimeout(),
                    command
            );
            playing.set(true);
            log.info("已启动远程响铃: {}", type);
        } catch (Exception e) {
            log.error("启动远程响铃失败: {}", e.getMessage());
        }
    }

    public void stop() {
        String command = KICTONE_CMD + " stop";
        try {
            SshUtils.execute(
                    ssh.getHost(), ssh.getPort(),
                    ssh.getUsername(), ssh.getPassword(),
                    ssh.getPrivateKeyPath(), ssh.getTimeout(),
                    command
            );
            playing.set(false);
            log.info("已停止远程响铃");
        } catch (Exception e) {
            log.error("停止远程响铃失败: {}", e.getMessage());
        }
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public enum ToneType {
        NOTICE, STATUS, WARNING, ALARM, CRITICAL
    }
}
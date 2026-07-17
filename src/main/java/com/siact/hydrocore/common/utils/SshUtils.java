package com.siact.hydrocore.common.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siact.hydrocore.common.exception.BizException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public final class SshUtils {

    private SshUtils() {
    }

    public static SshResult execute(String host, int port, String username,
                                     String password, String privateKeyPath,
                                     int timeout, String command) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            if (privateKeyPath != null && !privateKeyPath.isEmpty()) {
                jsch.addIdentity(privateKeyPath);
            }

            session = jsch.getSession(username, host, port);
            if (privateKeyPath == null || privateKeyPath.isEmpty()) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(timeout);
            session.connect(timeout);

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            channel.connect(timeout);

            // 等待命令执行完成
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            String stdoutStr = readStream(stdout);
            String stderrStr = readStream(stderr);

            SshResult result = new SshResult();
            result.setStdout(stdoutStr.trim());
            result.setStderr(stderrStr.trim());
            result.setExitCode(channel.getExitStatus());

            if (result.getExitCode() != 0) {
                log.error("SSH 命令执行失败, exitCode: {}, stderr: {}, command: {}",
                        result.getExitCode(), result.getStderr(), command);
                throw new BizException("SSH 命令执行失败: " + result.getStderr());
            }

            return result;
        } catch (BizException e) {
            throw e;
        } catch (JSchException e) {
            log.error("SSH 连接失败: {}", e.getMessage(), e);
            throw new BizException("SSH 连接失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("SSH 执行异常: {}", e.getMessage(), e);
            throw new BizException("SSH 执行异常: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    @Getter
    @Setter
    public static class SshResult {
        private String stdout;
        private String stderr;
        private int exitCode;
    }

    private static String readStream(InputStream is) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

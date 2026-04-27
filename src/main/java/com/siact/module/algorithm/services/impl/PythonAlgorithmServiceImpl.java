package com.siact.module.algorithm.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.siact.common.config.KilnProperty;
import com.siact.common.exception.BizException;
import com.siact.common.utils.JacksonUtils;
import com.siact.common.utils.SshUtils;
import com.siact.module.algorithm.services.PythonAlgorithmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PythonAlgorithmServiceImpl implements PythonAlgorithmService {

    private final KilnProperty property;

    @Override
    public <T> T execute(String scriptName, Map<String, String> params, Class<T> returnType) {
        SshUtils.SshResult result = doExecute(scriptName, params);
        return JacksonUtils.fromJson(getLastLine(result.getStdout()), returnType);
    }

    @Override
    public <T> T execute(String scriptName, Map<String, String> params, TypeReference<T> typeRef) {
        SshUtils.SshResult result = doExecute(scriptName, params);
        return JacksonUtils.fromJson(getLastLine(result.getStdout()), typeRef);
    }

    private String getLastLine(String stdout) {
        if (StringUtils.isBlank(stdout)) {
            return stdout;
        }
        String trimmed = stdout.trim();
        int lastNewline = trimmed.lastIndexOf('\n');
        if (lastNewline >= 0) {
            return trimmed.substring(lastNewline + 1);
        }
        return trimmed;
    }

    private SshUtils.SshResult doExecute(String scriptName, Map<String, String> params) {
        KilnProperty.Algorithm.Ssh sshConfig = getSshConfig();
        String command = buildCommand(sshConfig, scriptName, params);

        log.info("调用 Python 算法: script={}, params={}", scriptName, params);
        SshUtils.SshResult result = SshUtils.execute(
                sshConfig.getHost(),
                sshConfig.getPort(),
                sshConfig.getUsername(),
                sshConfig.getPassword(),
                sshConfig.getPrivateKeyPath(),
                sshConfig.getTimeout(),
                command
        );
        log.info("Python 算法返回: script={}, stdout={}", scriptName, result.getStdout());
        return result;
    }

    private KilnProperty.Algorithm.Ssh getSshConfig() {
        KilnProperty.Algorithm algorithm = property.getAlgorithm();
        if (algorithm == null || algorithm.getSsh() == null) {
            throw new BizException("未配置 Python 算法 SSH 连接信息");
        }
        KilnProperty.Algorithm.Ssh ssh = algorithm.getSsh();
        if (StringUtils.isBlank(ssh.getHost()) || StringUtils.isBlank(ssh.getUsername())) {
            throw new BizException("Python 算法 SSH 配置不完整: 缺少 host 或 username");
        }
        if (StringUtils.isBlank(ssh.getPassword()) && StringUtils.isBlank(ssh.getPrivateKeyPath())) {
            throw new BizException("Python 算法 SSH 配置不完整: 需要配置 password 或 privateKeyPath");
        }
        return ssh;
    }

    private String buildCommand(KilnProperty.Algorithm.Ssh sshConfig, String scriptName, Map<String, String> params) {
        // 构建 python 脚本命令
        StringBuilder scriptCmd = new StringBuilder();
        scriptCmd.append(sshConfig.getPythonPath());
        scriptCmd.append(" ").append(sshConfig.getScriptBasePath());
        if (!sshConfig.getScriptBasePath().endsWith("/")) {
            scriptCmd.append("/");
        }
        scriptCmd.append(scriptName);

        if (params != null && !params.isEmpty()) {
            String args = params.values().stream()
                    .map(v -> "'" + v.replace("'", "'\\''") + "'")
                    .collect(Collectors.joining(" "));
            scriptCmd.append(" ").append(args);
        }

        // 如果配置了 conda 环境，用 bash -lc 包裹命令
        if (StringUtils.isNotBlank(sshConfig.getCondaEnv())) {
            String escapedScript = scriptCmd.toString().replace("\"", "\\\"");
            return "bash -lc \"conda activate " + sshConfig.getCondaEnv() + " && " + escapedScript + "\"";
        }

        return scriptCmd.toString();
    }
}

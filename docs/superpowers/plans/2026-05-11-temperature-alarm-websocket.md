# 温度超限告警 WebSocket 推送功能 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现温度超限实时监控告警：定时轮询 DCS 数据，超限时服务器响铃并通过 WebSocket 推送告警消息，前端确认后停止响铃。

**Architecture:** 独立定时任务轮询检查，内存状态管理响铃，STOMP WebSocket 广播告警。响铃功能抽象为独立 `SoundPlayer` 组件，支持多种音频格式（系统蜂鸣、WAV 文件）。

**Tech Stack:** Java 8, Spring Boot, STOMP WebSocket (`SimpMessagingTemplate`), `javax.sound.sampled` (WAV 播放), `java.awt.Toolkit` (系统蜂鸣), Redis 分布式锁

**Compile command:** `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`

---

## File Structure

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `src/main/java/com/siact/module/system/constants/SysConfigCodeConstants.java` | 新增告警轮询周期常量 |
| Create | `src/main/java/com/siact/module/base/vo/AlarmPointVO.java` | 单个超限点位信息 |
| Create | `src/main/java/com/siact/module/base/vo/TemperatureAlarmVO.java` | 告警推送消息 |
| Create | `src/main/java/com/siact/core/alarm/SoundPlayer.java` | 独立响铃组件，支持多种格式 |
| Create | `src/main/java/com/siact/module/base/service/TemperatureAlarmService.java` | 告警服务接口 |
| Create | `src/main/java/com/siact/module/base/service/impl/TemperatureAlarmServiceImpl.java` | 告警服务实现 |
| Create | `src/main/java/com/siact/module/base/controller/TemperatureAlarmController.java` | 确认停止响铃接口 |
| Create | `src/main/java/com/siact/module/base/task/TemperatureAlarmTask.java` | 定时检查任务 |

---

### Task 1: 常量 + VO 类 + SoundPlayer

**Files:**
- Modify: `src/main/java/com/siact/module/system/constants/SysConfigCodeConstants.java`
- Create: `src/main/java/com/siact/module/base/vo/AlarmPointVO.java`
- Create: `src/main/java/com/siact/module/base/vo/TemperatureAlarmVO.java`
- Create: `src/main/java/com/siact/core/alarm/SoundPlayer.java`

- [ ] **Step 1: 在 SysConfigCodeConstants 新增常量**

在 `SysConfigCodeConstants.java` 的 `LEVEL_CONTROL_DATACODES` 行之后添加：

```java
  public static final String TEMPERATURE_ALARM_CYCLE = "temperature_alarm_cycle";
```

- [ ] **Step 2: 创建 AlarmPointVO**

创建 `src/main/java/com/siact/module/base/vo/AlarmPointVO.java`：

```java
package com.siact.module.base.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmPointVO {
    private String pointName;
    private String dataCode;
    private BigDecimal currentValue;
    private String limitType;
    private BigDecimal limitValue;
}
```

- [ ] **Step 3: 创建 TemperatureAlarmVO**

创建 `src/main/java/com/siact/module/base/vo/TemperatureAlarmVO.java`：

```java
package com.siact.module.base.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureAlarmVO {
    private boolean alarmed;
    private String message;
    private List<AlarmPointVO> points;
}
```

- [ ] **Step 4: 创建 SoundPlayer**

创建 `src/main/java/com/siact/core/alarm/SoundPlayer.java`：

```java
package com.siact.core.alarm;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SoundPlayer {

    private final AtomicBoolean playing = new AtomicBoolean(false);
    private Thread playerThread;

    public enum SoundType {
        SYSTEM_BEEP,
        WAV_FILE
    }

    private final SoundType type;
    private final String wavFilePath;
    private final long intervalMs;

    private SoundPlayer(SoundType type, String wavFilePath, long intervalMs) {
        this.type = type;
        this.wavFilePath = wavFilePath;
        this.intervalMs = intervalMs;
    }

    public static SoundPlayer systemBeep(long intervalMs) {
        return new SoundPlayer(SoundType.SYSTEM_BEEP, null, intervalMs);
    }

    public static SoundPlayer wavFile(String filePath, long intervalMs) {
        return new SoundPlayer(SoundType.WAV_FILE, filePath, intervalMs);
    }

    public void start() {
        if (playing.compareAndSet(false, true)) {
            playerThread = new Thread(this::playLoop, "sound-player");
            playerThread.setDaemon(true);
            playerThread.start();
        }
    }

    public void stop() {
        playing.set(false);
    }

    public boolean isPlaying() {
        return playing.get();
    }

    private void playLoop() {
        while (playing.get()) {
            try {
                switch (type) {
                    case SYSTEM_BEEP:
                        playBeep();
                        break;
                    case WAV_FILE:
                        playWav();
                        break;
                }
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void playBeep() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            log.warn("系统蜂鸣失败: {}", e.getMessage());
        }
    }

    private void playWav() {
        File file = new File(wavFilePath);
        if (!file.exists()) {
            log.warn("音频文件不存在: {}", wavFilePath);
            return;
        }
        try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            Thread.sleep(clip.getMicrosecondLength() / 1000);
            clip.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            log.warn("播放 WAV 文件失败: {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 5: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/siact/module/system/constants/SysConfigCodeConstants.java src/main/java/com/siact/module/base/vo/AlarmPointVO.java src/main/java/com/siact/module/base/vo/TemperatureAlarmVO.java src/main/java/com/siact/core/alarm/SoundPlayer.java
git commit -m "feat(alarm): 新增告警 VO、常量和独立 SoundPlayer 响铃组件"
```

---

### Task 2: TemperatureAlarmService 接口 + 实现

**Files:**
- Create: `src/main/java/com/siact/module/base/service/TemperatureAlarmService.java`
- Create: `src/main/java/com/siact/module/base/service/impl/TemperatureAlarmServiceImpl.java`

- [ ] **Step 1: 创建 TemperatureAlarmService 接口**

创建 `src/main/java/com/siact/module/base/service/TemperatureAlarmService.java`：

```java
package com.siact.module.base.service;

import com.siact.module.base.vo.TemperatureAlarmVO;

public interface TemperatureAlarmService {
    TemperatureAlarmVO checkAndAlarm();
    void stopRinging();
}
```

- [ ] **Step 2: 创建 TemperatureAlarmServiceImpl**

创建 `src/main/java/com/siact/module/base/service/impl/TemperatureAlarmServiceImpl.java`：

```java
package com.siact.module.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.core.alarm.SoundPlayer;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.TemperatureAlarmService;
import com.siact.module.base.vo.AlarmPointVO;
import com.siact.module.base.vo.TemperatureAlarmVO;
import com.siact.module.system.constants.SysConfigCodeConstants;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.service.SysConfigService;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TemperatureAlarmServiceImpl implements TemperatureAlarmService {

    private static final String WS_DESTINATION = "/topic/temperature-alarm";
    private static final long RING_INTERVAL_MS = 2000;

    private final SysConfigService sysConfigService;
    private final ControlIntervalConfigService configService;
    private final DataService dataService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SoundPlayer soundPlayer;

    public TemperatureAlarmServiceImpl(SysConfigService sysConfigService,
                                       ControlIntervalConfigService configService,
                                       DataService dataService,
                                       SimpMessagingTemplate messagingTemplate) {
        this.sysConfigService = sysConfigService;
        this.configService = configService;
        this.dataService = dataService;
        this.messagingTemplate = messagingTemplate;
        this.soundPlayer = SoundPlayer.systemBeep(RING_INTERVAL_MS);
    }

    @Override
    public TemperatureAlarmVO checkAndAlarm() {
        // 1. 读取 control_target_points 配置
        SysConfigDTO config = sysConfigService.getByCode(SysConfigCodeConstants.CONTROL_TARGET_POINTS);
        if (config == null || config.getData() == null) {
            log.warn("温度告警：未找到 control_target_points 配置");
            return buildNormalVO();
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cptData = (Map<String, Map<String, Object>>) config.getData();
        if (cptData.isEmpty()) {
            return buildNormalVO();
        }

        // 2. 提取 dataCode 列表
        List<String> dataCodes = cptData.values().stream()
                .map(v -> MapUtils.getString(v, "code"))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (dataCodes.isEmpty()) {
            return buildNormalVO();
        }

        // 3. 查询告警限值
        List<ControlIntervalConfigDTO> configList = configService.selectListByDataCodeList(dataCodes);
        Map<String, ControlIntervalConfigDTO> configMap = configList.stream()
                .collect(Collectors.toMap(ControlIntervalConfigDTO::getDataCode, c -> c, (v1, v2) -> v1));

        // 4. 查询 DCS 实时值
        JSONObject dcsValues;
        try {
            dcsValues = dataService.queryRealValue(String.join(",", dataCodes));
        } catch (Exception e) {
            log.error("温度告警：查询 DCS 实时值失败: {}", e.getMessage());
            return buildNormalVO();
        }

        // 5. 逐点比较
        List<AlarmPointVO> alarmedPoints = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : cptData.entrySet()) {
            String pointName = entry.getKey();
            String dataCode = MapUtils.getString(entry.getValue(), "code");
            if (StringUtils.isBlank(dataCode)) continue;

            ControlIntervalConfigDTO alarmConfig = configMap.get(dataCode);
            if (alarmConfig == null) continue;

            BigDecimal dcsVal = dcsValues == null ? null : dcsValues.getBigDecimal(dataCode);
            if (dcsVal == null) continue;

            String upAlarmStr = alarmConfig.getUpAlarm();
            String lowAlarmStr = alarmConfig.getLowAlarm();

            if (StringUtils.isNotBlank(upAlarmStr) && dcsVal.compareTo(new BigDecimal(upAlarmStr)) > 0) {
                alarmedPoints.add(AlarmPointVO.builder()
                        .pointName(pointName)
                        .dataCode(dataCode)
                        .currentValue(dcsVal)
                        .limitType("上告警限")
                        .limitValue(new BigDecimal(upAlarmStr))
                        .build());
            } else if (StringUtils.isNotBlank(lowAlarmStr) && dcsVal.compareTo(new BigDecimal(lowAlarmStr)) < 0) {
                alarmedPoints.add(AlarmPointVO.builder()
                        .pointName(pointName)
                        .dataCode(dataCode)
                        .currentValue(dcsVal)
                        .limitType("下告警限")
                        .limitValue(new BigDecimal(lowAlarmStr))
                        .build());
            }
        }

        // 6. 处理结果
        TemperatureAlarmVO vo;
        if (!alarmedPoints.isEmpty()) {
            String names = alarmedPoints.stream().map(AlarmPointVO::getPointName).collect(Collectors.joining("、"));
            vo = TemperatureAlarmVO.builder()
                    .alarmed(true)
                    .message(names + " 点位温度超限，请检查")
                    .points(alarmedPoints)
                    .build();
            // 启动响铃
            if (!soundPlayer.isPlaying()) {
                soundPlayer.start();
                log.info("温度超限告警：已启动响铃");
            }
        } else {
            vo = buildNormalVO();
        }

        // 7. WebSocket 推送
        try {
            messagingTemplate.convertAndSend(WS_DESTINATION, vo);
        } catch (Exception e) {
            log.error("温度告警 WebSocket 推送失败: {}", e.getMessage());
        }

        return vo;
    }

    @Override
    public void stopRinging() {
        if (soundPlayer.isPlaying()) {
            soundPlayer.stop();
            log.info("温度告警：已停止响铃");
        }
    }

    private TemperatureAlarmVO buildNormalVO() {
        return TemperatureAlarmVO.builder()
                .alarmed(false)
                .message("")
                .points(Collections.emptyList())
                .build();
    }
}
```

- [ ] **Step 3: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/siact/module/base/service/TemperatureAlarmService.java src/main/java/com/siact/module/base/service/impl/TemperatureAlarmServiceImpl.java
git commit -m "feat(alarm): 实现温度超限告警检查与 WebSocket 推送逻辑"
```

---

### Task 3: TemperatureAlarmController

**Files:**
- Create: `src/main/java/com/siact/module/base/controller/TemperatureAlarmController.java`

- [ ] **Step 1: 创建 Controller**

创建 `src/main/java/com/siact/module/base/controller/TemperatureAlarmController.java`：

```java
package com.siact.module.base.controller;

import com.siact.module.base.service.TemperatureAlarmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "温度告警")
@RestController
@RequestMapping("/alarm/temperature")
public class TemperatureAlarmController {

    private final TemperatureAlarmService alarmService;

    public TemperatureAlarmController(TemperatureAlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @ApiOperation("确认告警，停止响铃")
    @PostMapping("/confirm")
    public Boolean confirm() {
        alarmService.stopRinging();
        return true;
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/base/controller/TemperatureAlarmController.java
git commit -m "feat(alarm): 新增温度告警确认接口"
```

---

### Task 4: TemperatureAlarmTask 定时任务

**Files:**
- Create: `src/main/java/com/siact/module/base/task/TemperatureAlarmTask.java`

- [ ] **Step 1: 创建定时任务**

创建 `src/main/java/com/siact/module/base/task/TemperatureAlarmTask.java`：

```java
package com.siact.module.base.task;

import com.siact.common.redis.RedisService;
import com.siact.module.base.service.TemperatureAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class TemperatureAlarmTask {

    private static final String LOCK_KEY = "lock:temperature_alarm";
    private static final long LOCK_TIMEOUT = 120;

    private final TemperatureAlarmService alarmService;
    private final RedisService redis;

    public TemperatureAlarmTask(TemperatureAlarmService alarmService, RedisService redis) {
        this.alarmService = alarmService;
        this.redis = redis;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkTemperatureAlarm() {
        String lockValue = UUID.randomUUID().toString();
        if (!redis.tryLock(LOCK_KEY, lockValue, LOCK_TIMEOUT)) {
            log.info("温度告警任务正在执行，跳过本次触发");
            return;
        }
        try {
            alarmService.checkAndAlarm();
        } catch (Exception e) {
            log.error("温度告警检查异常: {}", e.getMessage(), e);
        } finally {
            redis.unlock(LOCK_KEY, lockValue);
        }
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/base/task/TemperatureAlarmTask.java
git commit -m "feat(alarm): 新增温度超限告警定时检查任务"
```

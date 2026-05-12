# IndustrialTonePlayer 响铃组件设计文档

**日期**: 2026-05-12
**状态**: 待实现

## 背景

当前 `SoundPlayer` 使用 `javax.sound.sampled.SourceDataLine` 播放正弦波音效，在 WSL 等环境下 ALSA 设备不支持该格式导致播放失败。需要替换为跨平台可靠的音频方案。

## 方案

使用 **LWJGL OpenAL** 替代 `javax.sound.sampled`。OpenAL 通过 native 库直接操作音频硬件，跨平台可靠（Linux/Windows/WSL/macOS）。

## 变更范围

### 1. pom.xml 新增 LWJGL 依赖

```xml
<properties>
    <lwjgl.version>3.3.6</lwjgl.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-bom</artifactId>
            <version>${lwjgl.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

新增依赖：
- `org.lwjgl:lwjgl` + `natives-windows` + `natives-linux`
- `org.lwjgl:lwjgl-openal` + `natives-windows` + `natives-linux`

### 2. 新建 IndustrialTonePlayer

**文件**: `com.siact.core.alarm.IndustrialTonePlayer`

核心组件：

- **ToneType 枚举** — 5 种音效等级，每个定义 `ToneStep[]` pattern 和默认时长
  - `NOTICE`（~2s）: 900/1200Hz 交替
  - `STATUS`（~3s）: 800/1100/1400Hz 三连
  - `WARNING`（~4s）: 1200Hz 双击 + 900Hz 长音
  - `ALARM`（~6s）: 1600/1000Hz 快速交替 + 800Hz 尾音
  - `CRITICAL`（~9s）: 1800/700Hz 紧急交替 + 650Hz 长音

- **ToneStep** — 单步定义（频率、时长ms、音量），频率 0 表示静默

- **Config** — 配置项：设备名称、主音量、轮询间隔、停止等待时间

- **OpenAlRuntime** — OpenAL 设备/上下文生命周期管理（AutoCloseable）

- **Session 机制** — `activeSessionId` 控制，`stop()` 递增 sessionId 使旧播放自动退出

API：
- `play(ToneType)` — 同步播放一个默认时长
- `play(ToneType, long durationMillis)` — 同步播放指定时长
- `startLoop(ToneType)` — 异步循环播放，直到 `stop()`
- `startLoop(ToneType, long durationMillis)` — 异步播放指定时长后自动停止
- `stop()` — 停止当前播放
- `isPlaying()` — 是否正在播放
- `close()` — 释放资源

PCM 生成：
- 采样率 48000Hz，立体声 16-bit
- 每步带 6ms 淡入淡出消除爆音
- `alBufferData` + `AL_LOOPING = AL_TRUE` 实现无缝循环

### 3. 新建 IndustrialTonePlayerTest

**文件**: `com.siact.core.alarm.IndustrialTonePlayerTest`

交互式测试程序，支持命令行操作：
- 1-5: 同步播放各等级
- 7-9: 异步循环播放各等级
- 10-12: 异步定时播放
- 14: 停止
- q: 退出

### 4. 变更 TemperatureAlarmServiceImpl

将 `SoundPlayer` 替换为 `IndustrialTonePlayer`：
- `SoundPlayer.create()` → `new IndustrialTonePlayer()`
- `soundPlayer.startLoop(SoundPlayer.AlarmLevel.ALARM, RING_INTERVAL_MS)` → `player.startLoop(IndustrialTonePlayer.ToneType.ALARM)`
- `soundPlayer.stop()` 不变

### 5. 清理旧文件

- 删除 `SoundPlayer.java`（替换为 `IndustrialTonePlayer`）
- 删除 `SoundPlayerTest.java`（替换为 `IndustrialTonePlayerTest`）
- 删除 `AudioDetector.java`（调试用工具）

## 不变的部分

- 音效模式参数（频率、时长、音量）与 bash 脚本一致
- `TemperatureAlarmService` 接口不变
- `TemperatureAlarmController` 不变
- `TemperatureAlarmTask` 不变
- 告警 VO（`AlarmPointVO`、`TemperatureAlarmVO`）不变
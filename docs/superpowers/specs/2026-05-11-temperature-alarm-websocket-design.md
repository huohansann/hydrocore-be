# 温度超限告警 WebSocket 推送功能设计文档

**日期**: 2026-05-11
**状态**: 待实现

## 背景

需要对窑炉温度进行实时监控告警。当配置的三个温度点位的 DCS 实际值超过 `control_interval_config` 中设定的上/下告警限值时，后端触发服务器响铃，并通过 WebSocket 推送告警消息给前端弹框提示。用户确认后停止响铃。

## 需求

1. 独立定时任务轮询 DCS 实时数据，检查温度是否超限
2. 超限时：服务器端响铃 + WebSocket 广播告警消息
3. 前端弹框提示超限点位信息，用户点击确认后调用 REST 接口停止响铃
4. 轮询间隔通过 sysconfig 配置，默认 60 秒
5. 多个点位同时超限时合并为一条消息推送
6. 任一在线用户确认即停止响铃

## 架构

**方案：独立定时任务 + 内存状态管理**

### 数据流

```
TemperatureAlarmTask (@Scheduled)
  → 读取 control_target_points 配置（监控点位列表）
  → 查询 control_interval_config（上/下告警限）
  → 调用 DataService.queryRealValue（DCS 实时值）
  → 比较实时值与告警限
  → 超限：启动响铃 + WebSocket 推送 /topic/temperature-alarm
  → 无超限：WebSocket 推送正常状态

前端收到 alarmed=true → 弹框提示 → 用户点击确认
  → POST /alarm/temperature/confirm → 后端停止响铃
```

## 变更范围

### 1. 新建 TemperatureAlarmTask

**文件**: `com.siact.module.base.task.TemperatureAlarmTask`

- `@Scheduled(fixedDelay)` 定时任务
- 使用 Redis 分布式锁（与现有任务模式一致）
- 轮询间隔通过 `SysConfigService` 读取 `temperature_alarm_cycle` 配置（秒），默认 60
- 调用 `TemperatureAlarmService.checkAndAlarm()`

### 2. 新建 TemperatureAlarmService

**接口文件**: `com.siact.module.base.service.TemperatureAlarmService`

方法：
- `TemperatureAlarmVO checkAndAlarm()` — 检查超限并触发告警
- `void stopRinging()` — 停止响铃

### 3. 新建 TemperatureAlarmServiceImpl

**文件**: `com.siact.module.base.service.impl.TemperatureAlarmServiceImpl`

核心逻辑：

1. 读取 `control_target_points` 配置，获取监控点位列表
2. 提取 dataCode 列表
3. 查询 `control_interval_config`，获取各点位的 upAlarm / lowAlarm
4. 调用 `DataService.queryRealValue(dataCodes)` 获取实时值
5. 逐点比较：`DCS值 > upAlarm` 或 `DCS值 < lowAlarm` 则标记为超限
6. 超限处理：
   - 如当前未在响铃状态，启动响铃线程
   - 通过 `SimpMessagingTemplate.convertAndSend("/topic/temperature-alarm", vo)` 广播
7. 无超限：推送 `alarmed=false` 消息

响铃控制：
- `AtomicBoolean ringing` 管理响铃状态
- `startRinging()`：设置 flag=true，新线程循环 `Toolkit.beep()` + `Thread.sleep(2000)`
- `stopRinging()`：设置 flag=false，线程自然退出

### 4. 新建 TemperatureAlarmVO

**文件**: `com.siact.module.base.vo.TemperatureAlarmVO`

```json
{
  "alarmed": true,
  "message": "TE202、TE213 点位温度超限，请检查",
  "points": [
    {
      "pointName": "TE202",
      "dataCode": "PGY02037...",
      "currentValue": 1520.5,
      "limitType": "上告警限",
      "limitValue": 1500.0
    }
  ]
}
```

内嵌 `AlarmPointVO`：
- `pointName: String`
- `dataCode: String`
- `currentValue: BigDecimal`
- `limitType: String`（"上告警限" / "下告警限"）
- `limitValue: BigDecimal`

### 5. 新建 TemperatureAlarmController

**文件**: `com.siact.module.base.controller.TemperatureAlarmController`

```
POST /alarm/temperature/confirm → alarmService.stopRinging()
```

无请求参数，返回 `Boolean`（由 ResponseBodyAdvice 统一包装）。

### 6. 变更 SysConfigCodeConstants

新增常量：
- `TEMPERATURE_ALARM_CYCLE = "temperature_alarm_cycle"` — 轮询间隔（秒）

## 响铃实现

使用 `java.awt.Toolkit.getDefaultToolkit().beep()` 在服务器端播放系统铃声。无额外依赖。

## 不变的部分

- `WebSocketConfig.java` — 已有 STOMP 配置，注入 `SimpMessagingTemplate` 即可使用
- `DataService` — 复用 `queryRealValue`
- `ControlIntervalConfigService` — 复用 `selectListByDataCodeList`
- 前端 WebSocket 连接 — 已通过 `/ws` 端点 + JWT 认证连接

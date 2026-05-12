package com.siact.module.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.common.config.KilnProperty;
import com.siact.core.alarm.KictonePlayer;
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

    private final SysConfigService sysConfigService;
    private final ControlIntervalConfigService configService;
    private final DataService dataService;
    private final SimpMessagingTemplate messagingTemplate;
    private final KictonePlayer tonePlayer;

    public TemperatureAlarmServiceImpl(SysConfigService sysConfigService,
                                       ControlIntervalConfigService configService,
                                       DataService dataService,
                                       SimpMessagingTemplate messagingTemplate,
                                       KilnProperty kilnProperty) {
        this.sysConfigService = sysConfigService;
        this.configService = configService;
        this.dataService = dataService;
        this.messagingTemplate = messagingTemplate;
        this.tonePlayer = new KictonePlayer(kilnProperty);
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
            if (!tonePlayer.isPlaying()) {
                tonePlayer.startLoop(KictonePlayer.ToneType.ALARM);
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
        if (tonePlayer.isPlaying()) {
            tonePlayer.stop();
            log.info("温度告警：已停止响铃");
        }
    }

    @Override
    public void playTone(String toneType) {
        KictonePlayer.ToneType type = KictonePlayer.ToneType.valueOf(toneType.toUpperCase());
        tonePlayer.startLoop(type);
        log.info("手动测试音频播放: {}", type);
    }

    @Override
    public void stopTone() {
        tonePlayer.stop();
        log.info("手动停止音频播放");
    }

    @Override
    public boolean isTonePlaying() {
        return tonePlayer.isPlaying();
    }

    private TemperatureAlarmVO buildNormalVO() {
        return TemperatureAlarmVO.builder()
                .alarmed(false)
                .message("")
                .points(Collections.emptyList())
                .build();
    }
}
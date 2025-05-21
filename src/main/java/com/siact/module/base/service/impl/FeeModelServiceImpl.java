package com.siact.module.base.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.dto.FeeModelDTO;
import com.siact.module.base.dto.FeeModelDataDTO;
import com.siact.module.base.service.FeeModelService;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.TplVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeeModelServiceImpl implements FeeModelService {
    @Autowired
    private TplService tplService;

    @Override
    public List<FeeModelDataDTO> getFeeModel(String cityCode, String energyCode, String feeModelStyle) {
        // 1:获取tpl当中配置的费价模型
        TplVO feeModelTpl = tplService.selectTplByCode("feeModelTpl");
        String tplContent = feeModelTpl.getTplContent();
        if (ObjectUtils.isEmpty(tplContent)) {
            log.error("未配置费价模型");
            throw new RuntimeException("未配置费价模型");
        }
        List<FeeModelDTO> feeModelDTOList = JSONArray.parseArray(tplContent, FeeModelDTO.class);
        if (ObjectUtils.isEmpty(feeModelDTOList)) {
            log.error("未配置费价模型");
            throw new RuntimeException("未配置费价模型");
        }

        // 获取当前匹配到的费价模型  如果未传dataCode,返回默认数据
        String finalCityCode = StringUtils.isBlank(cityCode) ? "default" : cityCode;
        FeeModelDTO curCityFeeModel = feeModelDTOList.stream().filter(
                o -> o.getCityCode().equals(finalCityCode) && o.getEnergyCode().equals(energyCode)
        ).findFirst().orElse(null);

        if (ObjectUtils.isEmpty(curCityFeeModel)||ObjectUtils.isEmpty(curCityFeeModel.getFeeModelData())) {
            log.error("未配置城市{}的费价模型", cityCode);
            throw new RuntimeException("未配置城市" + cityCode + "的费价模型");
        }

        List<FeeModelDataDTO> allFeeModelDataList = curCityFeeModel.getFeeModelData().stream().filter(o -> o.getStyle().equals(feeModelStyle)).collect(Collectors.toList());
        // 这里能匹配到多个费价模型  可能是单一 可能是分时
        if ("electricity-3".equals(feeModelStyle)) {
            // 如果是单一电价 电价只会有一个 需要设置开始结束时间  开始:00:00 结束:00:00
            FeeModelDataDTO feeModelDataDTO = allFeeModelDataList.get(0);
            feeModelDataDTO.setStart("00:00");
            feeModelDataDTO.setEnd("00:00");
            allFeeModelDataList = Collections.singletonList(feeModelDataDTO);
        }

        return allFeeModelDataList;
    }

    @Override
    public Map<String, FeeModelDataDTO> getFeeModelGroupByMinTs(String cityCode, String energyCode, String feeModelStyle) {
        List<FeeModelDataDTO> allFeeModelDataList = getFeeModel(cityCode, energyCode, feeModelStyle);
        // 解析费价模型  从 00:00 ~ 00:00
        Map<String, FeeModelDataDTO> feeModelMap = new HashMap<>();
        for (FeeModelDataDTO feeModelDataDTO : allFeeModelDataList) {
            String[] startTime = feeModelDataDTO.getStart().split(":");
            int startHour = Integer.parseInt(startTime[0]);
            int startMin = Integer.parseInt(startTime[1]);

            String endTimeStr = feeModelDataDTO.getEnd();
            String[] endTime = endTimeStr.split(":");
            int endHour = Integer.parseInt(endTime[0]);
            int endMin = Integer.parseInt(endTime[1]);

            // 费价模型只有当天的时间区间,因此LocalDate可以设置为当天,需要判断结束时间是否等于开始时间，如果等于，则将结束时间设置为第二天
            LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(startHour, startMin,0)); // 00:00
            LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(endHour, endMin,0));   // 00:00（表示第二天凌晨）
            if (endTimeStr.equals("00:00")){
                // 如果结束时间等于开始时间，则将结束时间设置为第二天
                endDateTime = endDateTime.plusDays(1);
            }

            Map<String, String> timeRangeMap = TimeUtil.splitDateTimeInRange(startDateTime, endDateTime, 15, "MIN", "HH:mm", true);
            for (Map.Entry<String, String> entry : timeRangeMap.entrySet()) {
                // k:开始时间 v:当前费价
                feeModelMap.put(entry.getKey(), feeModelDataDTO);
            }
        }
        return feeModelMap;
    }

    @Override
    public Map<String, BigDecimal> getFeeModelFeeGroupByMinTs(String cityCode, String energyCode, String feeModelStyle) {
        List<FeeModelDataDTO> allFeeModelDataList = getFeeModel(cityCode, energyCode, feeModelStyle);
        // 解析费价模型  从 00:00 ~ 00:00
        Map<String, BigDecimal> feeModelMap = new HashMap<>();
        for (FeeModelDataDTO feeModelDataDTO : allFeeModelDataList) {
            String[] startTime = feeModelDataDTO.getStart().split(":");
            int startHour = Integer.parseInt(startTime[0]);
            int startMin = Integer.parseInt(startTime[1]);

            String endTimeStr = feeModelDataDTO.getEnd();
            String[] endTime = endTimeStr.split(":");
            int endHour = Integer.parseInt(endTime[0]);
            int endMin = Integer.parseInt(endTime[1]);

            LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(startHour, startMin,0)); // 00:00
            LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(endHour, endMin,0));   // 00:00（表示第二天凌晨）
            if (endTimeStr.equals("00:00")){
                // 如果结束时间等于开始时间，则将结束时间设置为第二天
                endDateTime = endDateTime.plusDays(1);
            }

            Map<String, String> timeRangeMap = TimeUtil.splitDateTimeInRange(startDateTime, endDateTime, 15, "MIN", "HH:mm", true);
            for (Map.Entry<String, String> entry : timeRangeMap.entrySet()) {
                // k:开始时间 v:当前费价
                feeModelMap.put(entry.getKey(), feeModelDataDTO.getDataVal());
            }
        }
        return feeModelMap;
    }

}


package com.siact.module.monitoring.service;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.monitoring.dto.ModelConditionDto;

import java.util.List;

public interface MonitoringService {
    List<JSONObject> getProductionInfo(String tplCode);

    List<JSONObject> getEnvironmentInfo(String tplCode);

    com.alibaba.fastjson.JSONObject getModelData(ModelConditionDto modelConditionDto);
}

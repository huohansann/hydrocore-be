package com.siact.module.monitoring.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.siact.common.constant.ConstantSymbol;
import com.siact.module.base.service.TplService;
import com.siact.module.monitoring.dto.ModelConditionDto;
import com.siact.module.monitoring.service.MonitoringService;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MonitoringServiceImpl implements MonitoringService {

    @Autowired
    private TplService tplService;

    @Autowired
    private DataService dataService;
    @Override
    public List<JSONObject> getProductionInfo(String tplCode) {
        List<JSONObject> jsonObjects = tplService.getListByCode(tplCode, JSONObject.class);
        return jsonObjects;
    }

    @Override
    public List<JSONObject> getEnvironmentInfo(String tplCode) {
        List<JSONObject> jsonObjects = tplService.getListByCode(tplCode, JSONObject.class);
        return jsonObjects;
    }

    @Override
    public com.alibaba.fastjson.JSONObject getModelData(ModelConditionDto modelConditionDto) {
        return dataService.queryRealValue(String.join(ConstantSymbol.COMMA, modelConditionDto.getDataCodes()));
    }

}

package com.siact.module.monitoring.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.siact.common.constant.ConstantSymbol;
import com.siact.module.base.service.TplService;
import com.siact.module.monitoring.dto.ModelConditionDto;
import com.siact.module.monitoring.service.MonitoringService;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class MonitoringServiceImpl implements MonitoringService {

    @Autowired
    private TplService tplService;

    @Autowired
    private DataService dataService;

    @Value("${forecast.test-mode.enabled:false}")
    private boolean testModeEnabled;

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
    public JSONObject getModelData(ModelConditionDto modelConditionDto) {
        if (testModeEnabled) {
            return getMockModelData(modelConditionDto);
        }

        com.alibaba.fastjson.JSONObject jsonObject = dataService.queryRealValue(String.join(ConstantSymbol.COMMA, modelConditionDto.getDataCodes()));

        JSONObject resultObj = new JSONObject();
        for (String dataCode : modelConditionDto.getDataCodes()) {
            BigDecimal dataVal = null;
            if (jsonObject != null && jsonObject.containsKey(dataCode)) {
                dataVal = jsonObject.getBigDecimal(dataCode);
            }
            resultObj.put(dataCode, dataVal);
        }

        return resultObj;
    }

    /**
     * @Author: HouBo
     * @Date: 2026/5/8 9:24
     * @Description: 开发环境自用, 从json搞点数据看看
     */
    private JSONObject getMockModelData(ModelConditionDto dto) {
        log.info("测试模式开启，从 JSON 文件读取 mock 数据");
        try {
            ClassPathResource resource = new ClassPathResource("testJson/monitoring/getModelData.json");
            String jsonContent = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            JSONObject mockData = JSONObject.parseObject(jsonContent);

            JSONObject resultObj = new JSONObject();
            for (String dataCode : dto.getDataCodes()) {
                resultObj.put(dataCode, mockData.get(dataCode));
            }
            return resultObj;
        } catch (Exception e) {
            log.error("读取测试 JSON 文件失败，降级为正常模式", e);
            return doQueryRealValue(dto);
        }
    }

    private JSONObject doQueryRealValue(ModelConditionDto dto) {
        com.alibaba.fastjson.JSONObject jsonObject = dataService.queryRealValue(String.join(ConstantSymbol.COMMA, dto.getDataCodes()));
        JSONObject resultObj = new JSONObject();
        for (String dataCode : dto.getDataCodes()) {
            BigDecimal dataVal = null;
            if (jsonObject != null && jsonObject.containsKey(dataCode)) {
                dataVal = jsonObject.getBigDecimal(dataCode);
            }
            resultObj.put(dataCode, dataVal);
        }
        return resultObj;
    }

}

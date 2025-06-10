package com.siact.module.predicted.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.predicted.dto.PredictedDataDTO;
import com.siact.module.predicted.entity.PredictedDataEntity;

import java.util.List;
import java.util.Map;

public interface PredictedDataService extends IService<PredictedDataEntity> {

    void handleMqttMessage(String topic, String message);

    Map<Integer, List<PredictedDataDTO>> getPredictedDataByTypes(List<String> dataCodeList, List<Integer> predictedTypeList, String startTime, String endTime);


    Map<Integer, Map<String,List<PredictedDataDTO>>> getPredictedDataByTypesCoverBtStep(List<String> dataCodeList, List<Integer> predictedTypeList, String startTime, String endTime);

    List<JSONObject> getAllTypeList();

}

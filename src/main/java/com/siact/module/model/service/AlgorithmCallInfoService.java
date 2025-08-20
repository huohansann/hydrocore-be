package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.entity.AlgorithmCallInfoEntity;

import java.util.LinkedHashMap;

public interface AlgorithmCallInfoService extends IService<AlgorithmCallInfoEntity> {

    void handleCallBackModelInfo(LinkedHashMap<String, Object> params);

    Long addAlgorithmCallInfo(String type, Long modelId, String reqTime, String reqJson, String resTime, String resJson);

    /**
     * 删除早于time的数据
     * @param string
     */
    void deleteBeforeTime(String string);
}

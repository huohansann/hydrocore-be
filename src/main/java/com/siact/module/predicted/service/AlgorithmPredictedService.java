package com.siact.module.predicted.service;

public interface AlgorithmPredictedService {
    /**
     * 算法预测
     */
    void algorithmInference();

    /**
     * 删除早于time的call_info调用记录
     * time格式：yyyy-MM-dd HH:mm:ss,不传默认上月
     * @param time
     */
    void deleteAlgorithmCallInfoBeforeTime(String time);
}

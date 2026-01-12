package com.siact.module.predicted.controller;

import com.alibaba.fastjson2.JSONObject;
import com.siact.common.R;
import com.siact.module.algorithm.services.IntelligentDataService;
import com.siact.module.predicted.service.AlgorithmPredictedService;
import com.siact.module.predicted.service.PredictedDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "预测数据")
@RequiredArgsConstructor
@RestController
@RequestMapping("/predicted")
public class PredictedDataController {
    private final PredictedDataService predictedDataService;
    private final AlgorithmPredictedService algorithmPredictedService;
    private final IntelligentDataService intelligentDataService;

    @ApiOperation(value = "查询模板列表")
    @GetMapping("/handleMessage")
    public R handleMessageData(String topic, String message) {
        predictedDataService.handleMqttMessage(topic, message);
        return R.success();
    }


    @ApiOperation(value = "获取预测设置步长")
    @GetMapping("/typeList")
    public R<List<JSONObject>> getAllTypeList() {
        return R.success(predictedDataService.getAllTypeList());
    }

    @ApiOperation(value = "删除早于time的call_info调用记录")
    @GetMapping("/deleteAlgorithmCallInfo")
    public R deleteAlgorithmCallInfoBeforeTime(String time) {
        algorithmPredictedService.deleteAlgorithmCallInfoBeforeTime(time);
        return R.success();
    }

    @ApiOperation(value = "调用算法生成预测数据")
    @GetMapping("/algorithmInference")
    public R algorithmInference() {
        algorithmPredictedService.algorithmInference();
        return R.success();
    }

    @ApiOperation(value = "调用算法获取智能计算值")
    @GetMapping("/getIntelligentComputing")
    public R getIntelligentComputing() {
        intelligentDataService.callIntelligentInterface();
        return R.success();
    }

    @ApiOperation(value = "调用算法获取智能计算值")
    @GetMapping("/initIntelligentComputingCreateTime")
    public R initIntelligentComputingCreateTime(Boolean isForce) {
        algorithmPredictedService.initIntelligentComputingCreateTime(isForce);
        return R.success();
    }

}

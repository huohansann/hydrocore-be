package com.siact.module.predicted.controller;

import com.siact.common.R;
import com.siact.module.predicted.service.PredictedDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "预测数据")
@RestController
@RequestMapping("/predicted")
public class PredictedDataController {

    @Autowired
    private PredictedDataService predictedDataService;

    @ApiOperation(value = "查询模板列表")
    @GetMapping("/handleMessage")
    public R handleMessageData(String topic, String message) {
        predictedDataService.handleMqttMessage(topic, message);
        return R.success();
    }
}

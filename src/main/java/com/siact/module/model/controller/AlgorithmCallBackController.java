package com.siact.module.model.controller;


import com.alibaba.fastjson2.JSON;
import com.siact.common.R;
import com.siact.module.model.service.AlgorithmCallInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Api(tags = "算法回调接口")
@RestController
@Validated
@RequestMapping("/algorithm")
@Slf4j
public class AlgorithmCallBackController {

    @Autowired
    private AlgorithmCallInfoService algoCallInfoService;

    @ApiOperation(value = "获取模型信息")
    @PostMapping("/modelInfo")
    public R<Map<String, String>> getModelInfo(@RequestBody LinkedHashMap<String, Object> params) {
        algoCallInfoService.handleCallBackModelInfo(params);
        return R.success();
    }


    @ApiOperation(value = "获取预测数据")
    @PostMapping("/prediction")
    public R<Map<String, String>> prediction(@RequestBody LinkedHashMap<String, Object> params) {
        // 获取参数模板
        log.info("获取预测数据：{}", JSON.toJSONString(params));
        return R.success();
    }

}

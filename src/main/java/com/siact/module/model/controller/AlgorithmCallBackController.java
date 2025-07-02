package com.siact.module.model.controller;


import com.alibaba.fastjson2.JSON;
import com.siact.common.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

    @ApiOperation(value = "获取模型信息")
    @PostMapping("/modelInfo")
    public R<Map<String, String>> getModelInfo(@RequestBody LinkedHashMap<String, Object> params) {
        // 获取参数模板
        log.info("获取模型信息：{}", JSON.toJSONString(params));

        // TODO 需要根据模型name和时间戳 拼接出解析出模型文件目录/年月日  并上传到minio当中去
//        algorithm.modelBasePath

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

package com.siact.hydrocore.module.base.controller;

import com.siact.hydrocore.common.R;
import com.siact.hydrocore.module.base.service.FrontendCacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "炉子基本信息配置")
@RestController
@RequestMapping("/cache")
public class FrontendCacheController {

    @Autowired
    private FrontendCacheService frontendCacheService;

    /**
     * 设置配置
     *
     * @param userId
     * @param key
     * @param value
     * @return
     */
    @GetMapping("/setConfig")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "key", value = "配置键", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "value", value = "配置值", required = true, paramType = "query", dataType = "string")
    })
    public R setConfig(String userId, String key, String value) {

        frontendCacheService.setConfig(userId, key, value);
        return R.success();
    }

    /**
     * 获取配置
     *
     * @param userId
     * @param keys
     * @return
     */
    @GetMapping("/getConfig")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "keys", value = "配置键", required = true, paramType = "query", dataType = "string")
    })
    public R<Map<String, String>> getConfig(String userId, String keys) {
        return R.data(frontendCacheService.getConfig(userId, keys));
    }

    /**
     * 删除配置
     *
     * @param userId
     * @param keys
     * @return
     */
    @GetMapping("/deleteConfig")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "keys", value = "配置键", required = true, paramType = "query", dataType = "string")
    })
    public R deleteConfig(String userId, String keys) {
        frontendCacheService.deleteConfig(userId, keys);
        return R.success();
    }


}

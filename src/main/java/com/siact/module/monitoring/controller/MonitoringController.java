package com.siact.module.monitoring.controller;

import com.siact.common.R;
import com.siact.module.monitoring.dto.ModelConditionDto;
import com.siact.module.monitoring.service.MonitoringService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "窑炉监测")
@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    /**
     * 生产信息: 后端根据模板组合装值
     *
     * @param tplCode
     */
    @GetMapping("/production")
    @ApiOperation("生产信息")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "tplCode", value = "模板编号", required = true, defaultValue = "productInfo")
    )
    public R production(String tplCode) {
        return R.success(monitoringService.getProductionInfo(tplCode));
    }

    /**
     * 环境监测
     *
     * @param tplCode
     */
    @GetMapping("/environment")
    @ApiOperation("环境监测")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "tplCode", value = "模板编号", required = true, defaultValue = "environmentMonitoring")
    )
    public R environment(String tplCode) {
        return R.success(monitoringService.getEnvironmentInfo(tplCode));
    }

    /**
     * 根据dataCode查询数据
     *
     * @param modelConditionDto
     * @return R
     */
    @PostMapping("/getModelData")
    @ApiOperation("根据dataCode查询数据")
    public R getModelData(@RequestBody ModelConditionDto modelConditionDto) {
        return R.success(monitoringService.getModelData(modelConditionDto));
    }
}

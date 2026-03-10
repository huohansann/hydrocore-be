package com.siact.module.forecast.controller;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.siact.common.R;
import com.siact.module.base.model.AppConfigJsonNode;
import com.siact.module.base.service.AppConfigService;
import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.query.TempForecastQuery;
import com.siact.module.forecast.service.ForecastKilnService;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;
import com.siact.module.forecast.vo.LineChartVO;
import com.siact.module.forecast.vo.TempForecastShowVO;
import com.siact.module.forecast.vo.TempForecastVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 10:28
 */
@Api(tags = "窑炉预测")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/forecast")
public class ForecastKilnController {
    private final ForecastKilnService service;
    private final AppConfigService acService;

    @ApiOperationSupport(order = 1)
    @ApiOperation("查询窑炉预测对应的菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "tpl", value = "模板名称", required = true, dataType = "String", paramType = "path", example = "kilnControl")
    })
    @GetMapping("/queryForecastKilnMenu/{tpl}")
    public R<List<ForecastKilnMenuVO>> queryForecastKilnMenu(@PathVariable("tpl") String tplCode) {
        R r;
        try {
            r = R.data(service.queryForecastKilnMenu(tplCode));
        } catch (Exception e) {
            log.error("查询窑炉预测对应的菜单失败", e);
            r = R.fail(e.getMessage());
        }
        return r;
    }

    @ApiOperationSupport(order = 2)
    @ApiOperation("查询窑炉温度数据(实时+预测, 窑炉预测)")
    public @PostMapping("/queryTemperature") TempForecastVO queryTemperature(@RequestBody @Validated TempForecastQuery query) {
        return service.queryTemperature(query);
    }

    @ApiOperation("查询温度预测点位列表")
    public @GetMapping("/queryTempShowList/{code}") List<TempForecastShowVO> queryTemperatureShowList(@PathVariable String code) {
        AppConfigJsonNode node = acService.queryValueByAcKey(code);
        return node.asList(TempForecastShowVO.class);
    }

    @ApiOperationSupport(order = 2)
    @ApiOperation("查询窑炉属性数据(实时+预测，窑炉预测)")
    @PostMapping("/queryForecastInfo")
    public R<List<LineChartVO>> queryForecastInfo(@RequestBody @Validated ForecastKilnParamsDTO dto) {
        R r;
        try {
            r = R.data(service.queryForecastInfo(dto));
        } catch (Exception e) {
            log.error("查询属性的实时数据失败", e);
            r = R.fail(e.getMessage());
        }
        return r;
    }


    @ApiOperationSupport(order = 3)
    @ApiOperation("查询窑炉属性数据(预测，窑炉控制-MC温度趋势预测)")
    @PostMapping("/queryKilnForecastInfo")
    public R<List<LineChartVO>> queryKilnForecastInfo(@RequestBody @Validated ForecastKilnParamsDTO dto) {
        R r;
        try {
            r = R.data(service.queryKilnForecastInfo(dto));
        } catch (Exception e) {
            log.error("查询属性的实时数据失败", e);
            r = R.fail(e.getMessage());
        }
        return r;
    }
}

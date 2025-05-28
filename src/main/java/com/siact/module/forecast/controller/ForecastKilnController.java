package com.siact.module.forecast.controller;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.siact.common.R;
import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.service.ForecastKilnService;
import com.siact.module.forecast.vo.KilnForecastLineChartVO;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;
import com.siact.module.forecast.vo.LineChartVO;
import com.siact.sec.dto.CommonChartParamsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@RestController
@RequestMapping("/forecast")
public class ForecastKilnController {

    @Autowired
    private ForecastKilnService forecastKilnService;

    @ApiOperationSupport(order = 1)
    @ApiOperation("查询窑炉预测对应的菜单")
    @GetMapping("/queryForecastKilnMenu/{tpl}")
    public R<List<ForecastKilnMenuVO>> queryForecastKilnMenu(@PathVariable("tpl")String tpl) {
        R r;
        try {
            r = R.data(forecastKilnService.queryForecastKilnMenu(tpl));
        } catch (Exception e) {
            log.error("查询窑炉预测对应的菜单失败", e);
            r = R.fail(e.getMessage());
        }
        return r;
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation("查询窑炉属性数据(实时+预测)")
    @PostMapping("/queryForecastInfo")
    public R<List<LineChartVO>> queryForecastInfo(@RequestBody @Validated ForecastKilnParamsDTO dto) {
        R r;
        try {
            r = R.data(forecastKilnService.queryForecastInfo(dto));
        } catch (Exception e) {
            log.error("查询属性的实时数据失败", e);
            r = R.fail(e.getMessage());
        }
        return r;
    }


    @ApiOperationSupport(order = 3)
    @ApiOperation("查询窑炉属性数据(预测)")
    @PostMapping("/queryKilnForecastInfo")
    public R<List<LineChartVO>> queryKilnForecastInfo(@RequestBody @Validated ForecastKilnParamsDTO dto) {
        R r;
        try {
            r = R.data(forecastKilnService.queryKilnForecastInfo(dto));
        } catch (Exception e) {
            log.error("查询属性的实时数据失败", e);
            r = R.fail(e.getMessage());
        }
        return r;
    }
}
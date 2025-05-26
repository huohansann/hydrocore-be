package com.siact.module.forecast.controller;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.siact.common.R;
import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.service.ForecastKilnService;
import com.siact.module.forecast.vo.ForecastKilnLineChartVO;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 10:28
 */
@Api(tags = "窑炉预测")
@Validated
@RestController
@Slf4j
@RequestMapping("/kiln")
public class ForecastKilnController {

    @Autowired
    private ForecastKilnService forecastKilnService;

    @ApiOperationSupport(order = 1)
    @ApiOperation("查询窑炉预测对应的菜单")
    @ApiImplicitParam(name = "tpl", value = "模板", required = true)
    @GetMapping("/queryForecastKilnMenu")
    public R<List<ForecastKilnMenuVO>> queryForecastKilnMenu(String tpl) {
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
    @ApiOperation("查询属性的实时数据")
    @PostMapping("/queryForecastInfo")
    public R<ForecastKilnLineChartVO> queryForecastInfo(@RequestBody @Validated ForecastKilnParamsDTO dto) {
        R r;
        try {
            r = R.data(forecastKilnService.queryForecastInfo(dto));
        } catch (Exception e) {
            log.error("查询属性的实时数据失败", e);
            r = R.fail(e.getMessage());
        }
        return r;
    }

}
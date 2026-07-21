package com.siact.hydrocore.sec.controller;

import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.sec.dto.CommonChartResultDto;
import com.siact.hydrocore.sec.service.DataChartService;
import com.siact.hydrocore.sec.vo.CommonChartParamsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "Data query")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataChartService dataChartService;

    @ApiOperation("Query common chart data")
    @PostMapping("/queryCommonChartData")
    public ApiResponse<CommonChartResultDto> queryCommonChartData(@Valid @RequestBody CommonChartParamsVo vo) {
        return ApiResponse.success(dataChartService.queryCommonChartData(vo));
    }
}

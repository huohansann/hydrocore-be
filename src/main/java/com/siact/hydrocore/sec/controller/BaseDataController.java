package com.siact.hydrocore.sec.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.module.base.dto.ColumnChartDTO;
import com.siact.hydrocore.sec.dto.AttributeBetweenValVO;
import com.siact.hydrocore.sec.dto.AttributeIntervalValParamsDto;
import com.siact.hydrocore.sec.dto.CumulativeDataDTO;
import com.siact.hydrocore.sec.dto.IntervalDataDto;
import com.siact.hydrocore.sec.dto.IntervalValParamsDto;
import com.siact.hydrocore.sec.dto.RealTimeDTO;
import com.siact.hydrocore.sec.sevice.BaseDataService;
import com.siact.hydrocore.sec.vo.CloumChartParmsVO;
import com.siact.hydrocore.sec.vo.CumulativeDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-04-17 11:00
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/baseData")
@Api(tags = "基础数据服务")
public class BaseDataController {

    @Autowired
    private BaseDataService baseDataService;

    @ApiOperationSupport(order = 1)
    @ApiOperation("查询属性的实时数据")
    @PostMapping("/queryRealTimeInfo")
    public ApiResponse<JSONObject> queryRealTimeInfo(@RequestBody  @Validated RealTimeDTO dto) {
        try {
            return ApiResponse.success(baseDataService.queryRealTimeInfo(dto.getDataCode()));
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 查询某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param vo
     * @return
     */
    @ApiOperationSupport(order = 2)
    @ApiOperation("查询某个时间段的量")
    @PostMapping("/queryBetweenVal")
    public ApiResponse<JSONObject> queryBetweenVal(@RequestBody @Validated AttributeBetweenValVO vo) {
        try {
            return ApiResponse.success(baseDataService.queryBetweenVal(vo));
        }  catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 查询属性等时间间隔的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dto
     * @return
     */
    @ApiOperationSupport(order = 3)
    @ApiOperation("查询属性等时间间隔的量")
    @PostMapping("/queryAttributeIntervalVal")
    public ApiResponse<List<IntervalDataDto>> queryAttributeIntervalVal(@RequestBody @Validated AttributeIntervalValParamsDto dto) {
        try {
            return ApiResponse.success(baseDataService.queryAttributeIntervalVal(dto));
        } catch (Exception e) {
            log.error("查询等时间间隔的量出错-->", e);
            return ApiResponse.success(new ArrayList<>());
        }
    }

    @ApiOperationSupport(order = 4)
    @ApiOperation("查询柱状/折线图信息")
    @PostMapping("/queryColumnChartInfo")
    public ApiResponse<ColumnChartDTO> queryColumnChartInfo(@RequestBody CloumChartParmsVO cloumChartParmsVO) {
        try {
            return ApiResponse.success(baseDataService.getColumnChartInfo(cloumChartParmsVO));
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    @ApiOperationSupport(order = 5)
    @PostMapping("queryCumulativeData")
    @ApiOperation("系统累计数据(同步-环比)")
    public ApiResponse<List<CumulativeDataDTO>> queryCumulativeData(@RequestBody CumulativeDataVO vo){
        List<CumulativeDataDTO> data = baseDataService.queryCumulativeData(vo);
        return ApiResponse.success(data);
    }

    @ApiOperationSupport(order = 5)
    @PostMapping("parseAttributeParams")
    @ApiOperation("参数转换")
    public ApiResponse<IntervalValParamsDto> parseAttributeParams(@RequestBody IntervalValParamsDto vo){
        IntervalValParamsDto data = baseDataService.parseAttributeParams(vo);
        return ApiResponse.success(data);
    }
}

package com.siact.sec.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.siact.common.R;
import com.siact.module.base.dto.ColumnChartDTO;
import com.siact.sec.dto.AttributeBetweenValVO;
import com.siact.sec.dto.AttributeIntervalValParamsDto;
import com.siact.sec.dto.CumulativeDataDTO;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.dto.RealTimeDTO;
import com.siact.sec.sevice.BaseDataService;
import com.siact.sec.vo.CloumChartParmsVO;
import com.siact.sec.vo.CumulativeDataVO;
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
    public R<JSONObject> queryRealTimeInfo(@RequestBody  @Validated RealTimeDTO dto) {
        R r;
        try {
            r = R.data(baseDataService.queryRealTimeInfo(dto.getDataCode()));
        } catch (Exception e) {
            r = R.fail(e.getMessage());
        }
        return r;
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
    public R<JSONObject> queryBetweenVal(@RequestBody @Validated AttributeBetweenValVO vo) {
        R r;
        try {
            r = R.data(baseDataService.queryBetweenVal(vo));
        }  catch (Exception e) {
            r = R.fail(e.getMessage());
        }
        return r;
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
    public List<IntervalDataDto> queryAttributeIntervalVal(@RequestBody @Validated AttributeIntervalValParamsDto dto) {
        try {
            return baseDataService.queryAttributeIntervalVal(dto);
        } catch (Exception e) {
            log.error("查询等时间间隔的量出错-->", e);
            return new ArrayList<>();
        }
    }

    @ApiOperationSupport(order = 4)
    @ApiOperation("查询柱状/折线图信息")
    @PostMapping("/queryColumnChartInfo")
    public R<ColumnChartDTO> queryColumnChartInfo(@RequestBody CloumChartParmsVO cloumChartParmsVO) {
        R r;
        try {
            r = R.data(baseDataService.getColumnChartInfo(cloumChartParmsVO));
        } catch (Exception e) {
            r = R.fail(e.getMessage());
        }
        return r;
    }

    @ApiOperationSupport(order = 5)
    @PostMapping("queryCumulativeData")
    @ApiOperation("系统累计数据(同步-环比)")
    public R<List<CumulativeDataDTO>> queryCumulativeData(@RequestBody CumulativeDataVO vo){
        List<CumulativeDataDTO> data = baseDataService.queryCumulativeData(vo);
        return R.data(data);
    }

    @ApiOperationSupport(order = 5)
    @PostMapping("parseAttributeParams")
    @ApiOperation("参数转换")
    public R<IntervalValParamsDto> parseAttributeParams(@RequestBody IntervalValParamsDto vo){
        IntervalValParamsDto data = baseDataService.parseAttributeParams(vo);
        return R.data(data);
    }
}
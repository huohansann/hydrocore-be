package com.siact.hydrocore.sec.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.sec.dto.CommonChartResultDto;
import com.siact.hydrocore.sec.dto.CumulativeDataDTO;
import com.siact.hydrocore.sec.dto.IntervalDataDto;
import com.siact.hydrocore.sec.dto.IntervalNoteValParamsDto;
import com.siact.hydrocore.sec.dto.IntervalValParamsDto;
import com.siact.hydrocore.sec.sevice.DataService;
import com.siact.hydrocore.sec.vo.CommonChartParamsVo;
import com.siact.hydrocore.sec.vo.CumulativeDataVO;
import com.siact.hydrocore.sec.vo.ExportCommonChartParamsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@Api(tags = "数字孪生查询数据")
@RequestMapping("/api/data")
public class DataController {


    @Resource
    private DataService dataService;


    @ApiOperation(value = "(量)查询柱状图、折线图等图表数据")
    @ApiOperationSupport(order = 1)
    @PostMapping("/queryCommonChartData")
    public ApiResponse<CommonChartResultDto> queryCommonChartData(@Valid @RequestBody CommonChartParamsVo vo) {
        try {
            return ApiResponse.success(dataService.queryCommonChartData(vo));
        } catch (Exception e) {
            log.error("查询图表数据出错--->", e);
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    @PostMapping("/exportIntervalInstantData")
    @ApiOperation("导出(量)查询柱状图、折线图等图表数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codes", value = "编码codes", paramType = "query", required = true, dataType =
                    "string"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", paramType = "query", required = true, dataType =
                    "string"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", paramType = "query", required = true, dataType =
                    "string"),
            @ApiImplicitParam(name = "ts", value = "时间间隔", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "tsUnit", value = "步长单位", paramType = "query", required = true, dataType =
                    "string"),
            @ApiImplicitParam(name = "propNames", value = "属性名", paramType = "query", required = true, dataType =
                    "string"),
            @ApiImplicitParam(name = "fileName", value = "文件名", paramType = "query", required = true, dataType =
                    "string")
    })
    public void exportIntervalInstantData(HttpServletResponse response,
                                          @RequestBody @Validated ExportCommonChartParamsVO vo) {
        try {
            dataService.exportIntervalInstantData(response, vo);
        } catch (Exception e) {
            log.error("导出(量)查询柱状图、折线图等图表数据出错---->", e);
        }
    }


    /**
     * 查询某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dataCodes 数字孪生编码codes
     * @param startTime 开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime   结束时间 yyyy-MM-dd HH:mm:ss
     * @param calcType  计算类型 AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     * @return {dataCode:value}
     */
    @ApiOperation(value = "查询某个时间段的量")
    @GetMapping("/queryBetweenVal")
    public ApiResponse<JSONObject> queryBetweenVal(String dataCodes, String startTime, String endTime, String calcType) {
        if (StringUtils.isEmpty(dataCodes) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            throw new RuntimeException("查询某个时间段的量参数校验不通过");
        }

        return ApiResponse.success(dataService.queryBetweenVal(dataCodes, startTime, endTime, calcType));
    }


    /**
     * 查询等时间间隔的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dto
     * @return
     */
    @ApiOperation(value = "查询等时间间隔的量")
    @PostMapping("/queryIntervalVal")
    public ApiResponse<List<IntervalDataDto>> queryIntervalVal(@RequestBody @Validated IntervalValParamsDto dto) {

        try {
            return ApiResponse.success(dataService.queryIntervalVal(dto));
        } catch (Exception e) {
            log.error("查询等时间间隔的量出错-->", e);
        }

        return ApiResponse.success(new ArrayList<>());
    }


    /**
     * 查询实时值（最后一包数据）
     *
     * @param dataCodes
     * @return
     */
    @ApiOperation(value = "查询实时值（最后一包数据）")
    @GetMapping("/queryRealValue")
    public ApiResponse<JSONObject> queryRealValue(String dataCodes) {
        return ApiResponse.success(dataService.queryRealValue(dataCodes));
    }

    /**
     * 查询节点下属性某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dataCode       数字孪生编码code
     * @param propModelCodes 属性模型短码,示例值([ "EP1" ])
     * @param startTime      开始时间 yyyy-MM-dd hh:mm:ss
     * @param endTime        结束时间 yyyy-MM-dd hh:mm:ss
     * @param calcType       计算类型 AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     * @return {dataCode:value}
     */
    @ApiOperation(value = "查询节点下属性某个时间段的量")
    @GetMapping("/queryNoteBetweenVal")
    public ApiResponse<JSONObject> queryNoteBetweenVal(String dataCode, String propModelCodes, String startTime, String endTime,
                                          String calcType) {
        try {
            return ApiResponse.success(dataService.queryNoteBetweenVal(dataCode, propModelCodes, startTime, endTime, calcType));
        } catch (Exception e) {
            log.error("查询节点下属性某个时间段的量出错-->", e);
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 查询节点下属性等时间间隔的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dto
     * @return
     */
    @ApiOperation(value = "查询节点下属性等时间间隔的量")
    @PostMapping("/queryNoteIntervalVal")
    public ApiResponse<List<IntervalDataDto>> queryNoteIntervalVal(@RequestBody @Validated IntervalNoteValParamsDto dto) {
        try {
            return ApiResponse.success(dataService.queryNoteIntervalVal(dto));
        } catch (Exception e) {
            log.error("查询等时间间隔的量出错-->", e);
            return ApiResponse.success(new ArrayList<>());
        }
    }

    @PostMapping("queryCumulativeData")
    @ApiOperation("系统累计数据(同步-环比)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codes", value = "编码codes", paramType = "query", required = true, dataType =
                    "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", paramType = "query", required = true, dataType =
                    "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", paramType = "query", required = true, dataType =
                    "String"),
            @ApiImplicitParam(name = "yoy", value = "是否同比(默认false，不进行比较)", paramType = "query", dataType = "boolean"),
            @ApiImplicitParam(name = "qoq", value = "是否环比(默认false，不进行比较)", paramType = "query", dataType = "boolean"),
    })
    public ApiResponse<List<CumulativeDataDTO>> queryCumulativeData(@RequestBody CumulativeDataVO vo) {
        List<CumulativeDataDTO> data = dataService.queryCumulativeData(vo);
        return ApiResponse.success(data);
    }
}

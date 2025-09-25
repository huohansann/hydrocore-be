package com.siact.module.base.controller;

import com.siact.common.R;
import com.siact.module.base.dto.ControlIntervalConfigChartDTO;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.vo.ControlIntervalConfigVO;
import com.siact.module.base.vo.HistoryConfigChartQueryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "控制区间配置")
@RestController
@RequestMapping("/cic")
public class ControlIntervalConfigController {

    @Autowired
    private ControlIntervalConfigService configService;

    @ApiOperation("查询控制区间配置列表")
    @PostMapping("/list")
    public R list(@RequestBody ControlIntervalConfigVO configVO) {
        List<ControlIntervalConfigDTO> list = configService.selectListByCondition(configVO);
        return R.data(list);
    }

    @ApiOperation("查询控制区间配置图表")
    @PostMapping("/chart")
    public R<ControlIntervalConfigChartDTO> chart(@RequestBody ControlIntervalConfigVO configVO) {
        ControlIntervalConfigChartDTO chartDTO = configService.chart(configVO);
        return R.data(chartDTO);
    }

//    @ApiOperation("新增")
//    @PostMapping("/add")
//    public R add(@RequestBody ControlIntervalConfigDTO configDTO) {
//        configService.add(configDTO);
//        return R.success();
//    }

    @ApiOperation("编辑")
    @PostMapping("/update")
    public R update(@RequestBody List<ControlIntervalConfigDTO> configDTOs) {
        configService.updateConfig(configDTOs);
        return R.success();
    }

    @ApiOperation("详情")
    @PostMapping("/get")
    public R get(@RequestBody ControlIntervalConfigVO configVO) {
        ControlIntervalConfigDTO configDTO = configService.get(configVO);
        return R.data(configDTO);
    }


//    @ApiOperation("根据时间查询测点数据")
//    @PostMapping("/queryBetweenVal")
//    public R queryBetweenVal(@RequestBody ControlIntervalConfigVO configVO) {
//        return R.data(configService.selectListByConditionNew(configVO));
//    }

//    @ApiOperation("编辑并保存历史记录")
//    @PostMapping("/updateAndSaveHis")
//    public R updateAndSaveHis(@RequestBody ControlIntervalConfigDTO configDTO) {
//        configService.updateAndSaveHis(configDTO);
//        return R.success();
//    }

    @ApiOperation("查询历史记录图表")
    @PostMapping("/queryHistoryConfigChart")
    public R queryHistoryConfigChart(@RequestBody HistoryConfigChartQueryVO queryVO) {
        return R.data(configService.queryHistoryConfigChart(queryVO.getDataCodeList(), queryVO.getStartTime(), queryVO.getEndTime(), queryVO.getTs(), queryVO.getTsUnit(), queryVO.getFormatVal()));
    }
}

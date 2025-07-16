package com.siact.module.process.controller;

import com.siact.common.result.R;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.dto.ProcessLogPageDTO;
import com.siact.module.process.dto.ProcessLogQueryDTO;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.vo.ProcessLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工艺日志Controller
 */
@Api(tags = "工艺日志管理")
@RestController
@Validated
@RequestMapping("/process/log")
public class ProcessLogController {
    @Autowired
    private IProcessLogService processLogService;

    @ApiOperation("分页查询工艺日志")
    @PostMapping("/page")
    public R<PageVO<ProcessLogEntity>> page(@RequestBody ProcessLogPageDTO queryDTO) {
        return R.data(processLogService.pageQuery(queryDTO));
    }

    @ApiOperation("查询全部工艺日志")
    @PostMapping("/list")
    public R<List<ProcessLogVO>> list(@RequestBody ProcessLogQueryDTO queryDTO) {
        return R.data(processLogService.listAll(queryDTO));
    }

    @ApiOperation("查询单条工艺日志")
    @GetMapping("getById")
    public R<ProcessLogVO> get(@RequestParam(value = "id") Long id) {
        return R.data(processLogService.getById(id));
    }

    @ApiOperation("新增工艺日志")
    @PostMapping("/add")
    public R add(@RequestBody @Validated ProcessLogDTO dto) {
        processLogService.add(dto);
        return R.data(true);
    }

    @ApiOperation("修改工艺日志")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody ProcessLogDTO dto) {
        return R.data(processLogService.update(dto));
    }

    @ApiOperation("根据Id删除工艺日志")
    @DeleteMapping("/delete")
    public R<Boolean> delete(@RequestParam(value = "id") Long id) {
        return R.data(processLogService.delete(id));
    }

    @ApiOperation("根据idList批量删除工艺日志")
    @PostMapping("/deleteBatch")
    public R<Boolean> deleteBatch(@RequestBody List<Long> idList) {
        return R.data(processLogService.deleteBatch(idList));
    }

    @ApiOperation("根据期日查找工艺日志")
    @GetMapping("/queryByDate")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "queryDate", value = "查询日期,格式yyyy-MM-dd HH:mm:ss", required = true)
    )
    public R<ProcessLogVO> queryByDate(String queryDate) {
        return R.data(processLogService.queryByDate(queryDate));
    }

    @ApiOperation("根据期起止日期查找工艺日志")
    @GetMapping("/queryByDateRange")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始日期,格式yyyy-MM-dd HH:mm:ss", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束日期,格式yyyy-MM-dd HH:mm:ss", required = true)
    })
    public R<Map<String,List<ProcessLogVO>>> queryByDateRange(String startTime, String endTime) {
        return R.data(processLogService.queryByDateRange(startTime,endTime));
    }
} 
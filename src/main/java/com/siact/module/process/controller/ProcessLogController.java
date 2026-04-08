package com.siact.module.process.controller;

import com.siact.common.result.R;
import com.siact.common.vo.PageVO;
import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.dto.ProcessLogPageDTO;
import com.siact.module.process.dto.ProcessLogQueryDTO;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.vo.ProcessLogVO;
import com.siact.sec.utils.IntervalTimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
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
        PageVO<ProcessLogEntity> pageVo = processLogService.pageQuery(queryDTO);

        List<ProcessLogEntity> recordsList = pageVo.getRecords();
        for (ProcessLogEntity logEntity : recordsList) {
            formatRtnTime(logEntity);
        }
        return R.data(pageVo);
    }

    @ApiOperation("查询全部工艺日志")
    @PostMapping("/list")
    public R<List<ProcessLogVO>> list(@RequestBody ProcessLogQueryDTO queryDTO) {
        List<ProcessLogVO> dataList = processLogService.listAll(queryDTO);

        for (ProcessLogVO logVO : dataList) {
            formatRtnTime(logVO);
        }
        return R.data(dataList);
    }

    @ApiOperation("查询单条工艺日志")
    @GetMapping("getById")
    public R<ProcessLogVO> get(@RequestParam(value = "id") Long id) {
        ProcessLogVO logVO = processLogService.getById(id);
        formatRtnTime(logVO);
        return R.data(logVO);
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
        ProcessLogVO data = processLogService.queryByDate(queryDate);
        formatRtnTime(data);
        return R.data(data);
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

    @ApiOperation("获取工艺日志配置")
    @GetMapping("/getProcessConfig")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "配置类型", required = true)
    })
    public R getProcessConfig(String type) {
        return R.data(processLogService.getProcessConfig(type));
    }

    /**
     * 转换返回的时间测试
     * @param record
     */
    private static void formatRtnTime(ProcessLogVO record) {
        if (ObjectUtils.isEmpty(record)) {
            return;
        }
        record.setStartTime(record.getStartTime() == null ? null : IntervalTimeUtil.dateFormat(record.getStartTime(), "yyyy-MM-dd HH:mm"));
        record.setEndTime(record.getEndTime() == null ? null : IntervalTimeUtil.dateFormat(record.getEndTime(), "yyyy-MM-dd HH:mm"));
        record.setOperationDate(record.getOperationDate() == null ? null : IntervalTimeUtil.dateFormat(record.getOperationDate(), "yyyy-MM-dd"));
    }

    /**
     * 转换返回的时间测试
     * @param record
     */
    private static void formatRtnTime(ProcessLogEntity record) {
        if (ObjectUtils.isEmpty(record)) {
            return;
        }
        record.setStartTime(record.getStartTime() == null ? null : IntervalTimeUtil.dateFormat(record.getStartTime(), "yyyy-MM-dd HH:mm"));
        record.setEndTime(record.getEndTime() == null ? null : IntervalTimeUtil.dateFormat(record.getEndTime(), "yyyy-MM-dd HH:mm"));
        record.setOperationDate(record.getOperationDate() == null ? null : IntervalTimeUtil.dateFormat(record.getOperationDate(), "yyyy-MM-dd"));
    }
} 
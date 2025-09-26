package com.siact.module.snapshot.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.siact.common.R;
import com.siact.common.constant.ConstantTime;
import com.siact.module.snapshot.dto.SnapshotChartQueryDTO;
import com.siact.module.snapshot.service.SnapshotPublicService;
import com.siact.module.snapshot.vo.SnapshotChartVO;
import com.siact.sec.utils.IntervalTimeUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 快照控制器
 *
 * @author Roo
 * @date 2025-09-22
 */
@RestController
@RequestMapping("/snapshot")
public class SnapshotController {

    @Autowired
    private SnapshotPublicService snapshotPublicService;

    /**
     * 查询图表
     */
    @ApiOperation("查询图表")
    @PostMapping("/chart")
    public R<SnapshotChartVO> queryChart(@RequestBody SnapshotChartQueryDTO queryDTO) {
        return R.success(snapshotPublicService.queryChart(queryDTO));
    }

    /**
     * 初始化快照数据
     */
    @ApiOperation("初始化快照数据")
    @GetMapping("/initTask")
    public R initTask(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        // 格式化时间 将秒置为 00
        startTime = IntervalTimeUtil.dateFormat(startTime, ConstantTime.DATE_TIME_MM_00);
        endTime = IntervalTimeUtil.dateFormat(endTime, ConstantTime.DATE_TIME_MM_00);

        // 开始结束日期之间的所有时间
        List<DateTime> timeList =
                DateUtil.rangeToList(DateUtil.parse(startTime), DateUtil.parse(endTime), DateField.MINUTE,1);

        if (ObjectUtils.isEmpty(timeList)) {
            return R.fail("timeList不能为空");
        }

        for (DateTime dateTime : timeList) {
            System.out.println("时间:------"+dateTime.toString(ConstantTime.DATE_TIME_MM_00));
//            snapshotPublicService.execSnapshotTask(dateTime.toString(ConstantTime.DATE_TIME_MM_00));
        }
        return R.success();
    }

}
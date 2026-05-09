package com.siact.module.level.controller;

import com.siact.module.level.query.LevelPredictCurveQuery;
import com.siact.module.level.service.LevelPredictService;
import com.siact.module.level.vo.LevelPredictCurveVO;
import com.siact.module.level.vo.LevelRealtimeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "液位数据查询")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/level/data")
public class LevelDataController {

    private final LevelPredictService predictService;

    @ApiOperation("获取实时数据（液位+开度+模式+状态）")
    @GetMapping("/realtime")
    public LevelRealtimeVO getRealtimeData() {
        return predictService.getRealtimeData();
    }

    @ApiOperation("液位预测曲线查询")
    @PostMapping("/predict")
    public LevelPredictCurveVO queryPredictCurve(@RequestBody @Validated LevelPredictCurveQuery query) {
        return predictService.queryPredictCurve(query);
    }
}

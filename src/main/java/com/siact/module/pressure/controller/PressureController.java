package com.siact.module.pressure.controller;

import com.siact.common.R;
import com.siact.module.pressure.dto.PressureDto;
import com.siact.module.pressure.dto.PressureQuery;
import com.siact.module.pressure.entity.PressureControlConfigEntity;
import com.siact.module.pressure.service.PressureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: HouBo
 * @Date: 2026/5/8 9:34
 * @Description: 窑压控制
 */
@Api(tags = "窑压控制")
@RestController
@RequestMapping("/pressure")
public class PressureController {

    @Resource
    private PressureService pressureService;


    @PostMapping("/getModelData")
    @ApiOperation("根据dataCode查询数据")
    public R getModelData(@RequestBody PressureDto pressureDto) {
        return R.success(pressureService.getModelData(pressureDto));
    }

    @GetMapping("/listAll")
    @ApiOperation("查询所有窑压控制参数")
    public R listAll() {
        return R.success(pressureService.selectAll());
    }

    @PostMapping("/updateAll")
    @ApiOperation("批量修改窑压控制参数")
    public R updateAll(@RequestBody List<PressureControlConfigEntity> list) {
        return R.success(pressureService.updateAll(list));
    }

    @PostMapping("/queryHistory")
    @ApiOperation("查询窑压历史数据")
    public R queryHistory(@RequestBody PressureQuery query) {
        return R.success(pressureService.queryHistory(query));
    }
}

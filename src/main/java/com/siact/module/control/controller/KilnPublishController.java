package com.siact.module.control.controller;

import com.siact.common.R;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.ControlSettingWindDTO;
import com.siact.module.control.service.KilnPublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "窑炉控制下发")
@RestController
@RequestMapping("/control")
public class KilnPublishController {

    @Autowired
    private KilnPublishService kilnPublishService;

    @ApiOperation("查询天然气控制设定值")
    @GetMapping("/gas/setting")
    public R<List<ControlSettingGasDTO>> getKilnGasControlSetting() {
        return R.data(kilnPublishService.getKilnGasControlSetting());
    }

    @ApiOperation("天然气人工调整值下发(手动)")
    @PostMapping("/gas/publish")
    public R gasPublish(@RequestBody List<ControlSettingGasDTO> list) {
        Boolean result = kilnPublishService.publishGas(list);
        return result ? R.success() : R.fail("下发失败!");
    }

    @ApiOperation("查询助燃风控制设定值")
    @GetMapping("/wind/setting")
    public R<List<ControlSettingWindDTO>> getKilnWindControlSetting() {
        return R.data(kilnPublishService.getKilnWindControlSetting());
    }

    @ApiOperation("助燃风风气比下发(手动)")
    @PostMapping("/wind/publish")
    public R windPublish(@RequestBody List<ControlSettingWindDTO> list) {
        Boolean result = kilnPublishService.publishWind(list);
        return result ? R.success() : R.fail("下发失败!");
    }

//    @ApiOperation("设定值下发(手动)")
//    @PostMapping("/publish")
//    public R publish(@RequestBody List<KilnInfoDistributeDTO> list) {
//        return kilnPublishService.publish(list);
//    }


    @ApiOperation("天然气自动下发")
    @PostMapping("/gas/autoPublish")
    public R gasAutoPublish() {
        return kilnPublishService.gasAutoPublish();
    }
}

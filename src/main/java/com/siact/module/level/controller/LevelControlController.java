package com.siact.module.level.controller;

import com.siact.module.level.dto.LevelControlConfigDTO;
import com.siact.module.level.service.LevelControlConfigService;
import com.siact.module.level.vo.LevelControlConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "液位控制配置")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/level/control")
public class LevelControlController {

    private final LevelControlConfigService configService;

    @ApiOperation("获取控制配置")
    @GetMapping("/config")
    public LevelControlConfigVO getConfig() {
        return configService.getConfig();
    }

    @ApiOperation("保存/更新控制配置")
    @PutMapping("/config")
    public void saveConfig(@RequestBody @Validated LevelControlConfigDTO dto) {
        configService.saveConfig(dto);
    }

    @ApiOperation("切换控制模式")
    @PutMapping("/mode/{mode}")
    public void switchMode(@PathVariable String mode) {
        configService.switchMode(mode);
    }
}

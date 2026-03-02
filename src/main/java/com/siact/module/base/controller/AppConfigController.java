package com.siact.module.base.controller;

import com.siact.module.base.command.AppConfigCreateCommand;
import com.siact.module.base.service.AppConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 16:18
 * @className : AppConfigController
 * @description : 系统配置控制器
 */
@Api(tags = "功能配置管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/config")
public class AppConfigController {
    private final AppConfigService service;

    @ApiOperation("添加配置项")
    public @PostMapping("/add") Boolean create(@RequestBody AppConfigCreateCommand command) {
        return service.create(command);
    }
}

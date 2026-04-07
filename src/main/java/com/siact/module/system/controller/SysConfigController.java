package com.siact.module.system.controller;

import com.siact.common.exception.BizException;
import com.siact.module.system.command.SysConfigCreateCommand;
import com.siact.module.system.command.SysConfigUpdateCommand;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.dto.SysConfigItemDTO;
import com.siact.module.system.enums.SysConfigModuleEnum;
import com.siact.module.system.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统选项配置控制器
 *
 * @author siact
 */
@Api(tags = "系统选项配置")
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysconfig")
public class SysConfigController {

    private final SysConfigService service;

    // ========== 单配置 CRUD ==========

    @ApiOperation("获取配置")
    @GetMapping("/{scCode}")
    public SysConfigDTO getByCode(@PathVariable String scCode) {
        SysConfigDTO dto = service.getByCode(scCode);
        if (dto == null) {
            throw new BizException("配置不存在");
        }
        return dto;
    }

    @ApiOperation("创建配置")
    @PostMapping
    public Boolean create(@Validated @RequestBody SysConfigCreateCommand command) {
        return service.create(command);
    }

    @ApiOperation("更新配置")
    @PutMapping("/{scCode}")
    public Boolean update(@PathVariable String scCode, @Validated @RequestBody SysConfigUpdateCommand command) {
        return service.update(scCode, command);
    }

    @ApiOperation("删除配置")
    @DeleteMapping("/{scCode}")
    public Boolean delete(@PathVariable String scCode) {
        return service.deleteByCode(scCode);
    }

    // ========== 批量查询 ==========

    @ApiOperation("按模块查询配置列表")
    @GetMapping("/module/{module}")
    public List<SysConfigDTO> listByModule(@PathVariable SysConfigModuleEnum module) {
        return service.listByModule(module);
    }

    @ApiOperation("按编码列表批量查询")
    @PostMapping("/batch")
    public Map<String, SysConfigDTO> batchGet(@RequestBody List<String> scCodes) {
        return service.batchGet(scCodes);
    }

    // ========== 配置项管理 ==========

    @ApiOperation("获取单个配置项")
    @GetMapping("/{scCode}/path/{scPath}")
    public SysConfigItemDTO getItem(@PathVariable String scCode, @PathVariable String scPath) {
        SysConfigItemDTO dto = service.getItem(scCode, scPath);
        if (dto == null) {
            throw new BizException("配置项不存在");
        }
        return dto;
    }

    @ApiOperation("更新单个配置项")
    @PatchMapping("/{scCode}/path/{scPath}")
    public Boolean updateItem(@PathVariable String scCode, @PathVariable String scPath,
                              @RequestParam String value, @RequestParam Integer version) {
        return service.updateItem(scCode, scPath, value, version);
    }

    @ApiOperation("删除单个配置项")
    @DeleteMapping("/{scCode}/path/{scPath}")
    public Boolean deleteItem(@PathVariable String scCode, @PathVariable String scPath) {
        return service.deleteItem(scCode, scPath);
    }

    // ========== 全量刷新 ==========

    @ApiOperation("全量刷新配置")
    @PostMapping("/{scCode}/refresh")
    public Boolean refresh(@PathVariable String scCode, @Validated @RequestBody SysConfigUpdateCommand command) {
        return service.refresh(scCode, command);
    }
}

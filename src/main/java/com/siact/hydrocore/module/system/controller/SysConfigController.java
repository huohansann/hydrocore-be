package com.siact.hydrocore.module.system.controller;

import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.exception.BizException;
import com.siact.hydrocore.module.system.command.SysConfigCreateCommand;
import com.siact.hydrocore.module.system.command.SysConfigUpdateCommand;
import com.siact.hydrocore.module.system.dto.SysConfigDTO;
import com.siact.hydrocore.module.system.dto.SysConfigItemDTO;
import com.siact.hydrocore.module.system.enums.SysConfigModuleEnum;
import com.siact.hydrocore.module.system.service.SysConfigService;
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
    public ApiResponse<SysConfigDTO> getByCode(@PathVariable String scCode) {
        SysConfigDTO dto = service.getByCode(scCode);
        if (dto == null) {
            throw new BizException("配置不存在");
        }
        return ApiResponse.success(dto);
    }

    @ApiOperation("创建配置")
    @PostMapping
    public ApiResponse<Boolean> create(@Validated @RequestBody SysConfigCreateCommand command) {
        return ApiResponse.success(service.create(command));
    }

    @ApiOperation("更新配置")
    @PutMapping("/{scCode}")
    public ApiResponse<Boolean> update(@PathVariable String scCode, @Validated @RequestBody SysConfigUpdateCommand command) {
        return ApiResponse.success(service.update(scCode, command));
    }

    @ApiOperation("删除配置")
    @DeleteMapping("/{scCode}")
    public ApiResponse<Boolean> delete(@PathVariable String scCode) {
        return ApiResponse.success(service.deleteByCode(scCode));
    }

    // ========== 批量查询 ==========

    @ApiOperation("按模块查询配置列表")
    @GetMapping("/module/{module}")
    public ApiResponse<List<SysConfigDTO>> listByModule(@PathVariable SysConfigModuleEnum module) {
        return ApiResponse.success(service.listByModule(module));
    }

    @ApiOperation("按编码列表批量查询")
    @PostMapping("/batch")
    public ApiResponse<Map<String, SysConfigDTO>> batchGet(@RequestBody List<String> scCodes) {
        return ApiResponse.success(service.batchGet(scCodes));
    }

    // ========== 配置项管理 ==========

    @ApiOperation("获取单个配置项")
    @GetMapping("/{scCode}/path/{scPath}")
    public ApiResponse<SysConfigItemDTO> getItem(@PathVariable String scCode, @PathVariable String scPath) {
        SysConfigItemDTO dto = service.getItem(scCode, scPath);
        if (dto == null) {
            throw new BizException("配置项不存在");
        }
        return ApiResponse.success(dto);
    }

    @ApiOperation("更新单个配置项")
    @PatchMapping("/{scCode}/path/{scPath}")
    public ApiResponse<Boolean> updateItem(@PathVariable String scCode, @PathVariable String scPath,
                              @RequestParam String value, @RequestParam Integer version) {
        return ApiResponse.success(service.updateItem(scCode, scPath, value, version));
    }

    @ApiOperation("删除单个配置项")
    @DeleteMapping("/{scCode}/path/{scPath}")
    public ApiResponse<Boolean> deleteItem(@PathVariable String scCode, @PathVariable String scPath) {
        return ApiResponse.success(service.deleteItem(scCode, scPath));
    }

    // ========== 全量刷新 ==========

    @ApiOperation("全量刷新配置")
    @PostMapping("/{scCode}/refresh")
    public ApiResponse<Boolean> refresh(@PathVariable String scCode, @Validated @RequestBody SysConfigUpdateCommand command) {
        return ApiResponse.success(service.refresh(scCode, command));
    }
}

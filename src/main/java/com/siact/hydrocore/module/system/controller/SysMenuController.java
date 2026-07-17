package com.siact.hydrocore.module.system.controller;

import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.system.command.SysMenuCreateCommand;
import com.siact.hydrocore.module.system.command.SysMenuUpdateCommand;
import com.siact.hydrocore.module.system.query.SysMenuQuery;
import com.siact.hydrocore.module.system.service.SysMenuService;
import com.siact.hydrocore.module.system.vo.SysMenuTreeVO;
import com.siact.hydrocore.module.system.vo.SysMenuVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "系统菜单管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysmenu")
public class SysMenuController {
    private final SysMenuService service;

    @ApiOperation("分页列表")
    @PostMapping("/list")
    public ApiResponse<PageVO<SysMenuVO>> list(@RequestBody SysMenuQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @ApiOperation("菜单树")
    @GetMapping("/tree")
    public ApiResponse<List<SysMenuTreeVO>> tree() {
        return ApiResponse.success(service.tree());
    }

    @ApiOperation("新增菜单")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody SysMenuCreateCommand command) {
        return ApiResponse.success(service.create(command));
    }

    @ApiOperation("编辑菜单")
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody SysMenuUpdateCommand command) {
        return ApiResponse.success(service.update(command));
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.success(service.delete(id));
    }
}

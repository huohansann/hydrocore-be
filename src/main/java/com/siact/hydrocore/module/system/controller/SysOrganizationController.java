package com.siact.hydrocore.module.system.controller;

import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.system.command.SysOrganizationCreateCommand;
import com.siact.hydrocore.module.system.command.SysOrganizationUpdateCommand;
import com.siact.hydrocore.module.system.query.SysOrganizationQuery;
import com.siact.hydrocore.module.system.service.SysOrganizationService;
import com.siact.hydrocore.module.system.vo.SysOrganizationTreeVO;
import com.siact.hydrocore.module.system.vo.SysOrganizationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "系统组织管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysorg")
public class SysOrganizationController {
    private final SysOrganizationService service;

    @ApiOperation("分页列表")
    @PostMapping("/list")
    public ApiResponse<PageVO<SysOrganizationVO>> list(@RequestBody SysOrganizationQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @ApiOperation("组织树")
    @GetMapping("/tree")
    public ApiResponse<List<SysOrganizationTreeVO>> tree() {
        return ApiResponse.success(service.tree());
    }

    @ApiOperation("新增组织")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody SysOrganizationCreateCommand command) {
        return ApiResponse.success(service.create(command));
    }

    @ApiOperation("编辑组织")
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody SysOrganizationUpdateCommand command) {
        return ApiResponse.success(service.update(command));
    }

    @ApiOperation("删除组织")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.success(service.delete(id));
    }
}

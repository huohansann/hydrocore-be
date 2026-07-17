package com.siact.hydrocore.module.system.controller;

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
    public PageVO<SysOrganizationVO> list(@RequestBody SysOrganizationQuery query) {
        return service.list(query);
    }

    @ApiOperation("组织树")
    @GetMapping("/tree")
    public List<SysOrganizationTreeVO> tree() {
        return service.tree();
    }

    @ApiOperation("新增组织")
    @PostMapping
    public Boolean create(@RequestBody SysOrganizationCreateCommand command) {
        return service.create(command);
    }

    @ApiOperation("编辑组织")
    @PutMapping
    public Boolean update(@RequestBody SysOrganizationUpdateCommand command) {
        return service.update(command);
    }

    @ApiOperation("删除组织")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }
}

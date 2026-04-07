package com.siact.module.system.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysRoleCreateCommand;
import com.siact.module.system.command.SysRoleUpdateCommand;
import com.siact.module.system.query.SysRoleQuery;
import com.siact.module.system.service.SysRoleService;
import com.siact.module.system.vo.SysRoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "系统角色管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysrole")
public class SysRoleController {
    private final SysRoleService service;

    @ApiOperation("分页列表")
    @PostMapping("/list")
    public PageVO<SysRoleVO> list(@RequestBody SysRoleQuery query) {
        return service.list(query);
    }

    @ApiOperation("新增角色")
    @PostMapping
    public Boolean create(@RequestBody SysRoleCreateCommand command) {
        return service.create(command);
    }

    @ApiOperation("编辑角色")
    @PutMapping
    public Boolean update(@RequestBody SysRoleUpdateCommand command) {
        return service.update(command);
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @ApiOperation("分配菜单权限")
    @PutMapping("/{id}/menus")
    public Boolean assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        service.assignMenus(id, menuIds);
        return true;
    }

    @ApiOperation("获取角色菜单ID列表")
    @GetMapping("/{id}/menus")
    public List<Long> getMenuIds(@PathVariable Long id) {
        return service.getMenuIds(id);
    }
}

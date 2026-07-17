package com.siact.hydrocore.module.system.controller;

import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.system.command.AssignUserRoleCommand;
import com.siact.hydrocore.module.system.command.ResetPasswordCommand;
import com.siact.hydrocore.module.system.command.SysUserCreateCommand;
import com.siact.hydrocore.module.system.command.SysUserUpdateCommand;
import com.siact.hydrocore.module.system.query.SysUserQuery;
import com.siact.hydrocore.module.system.service.SysUserService;
import com.siact.hydrocore.module.system.vo.SysMenuTreeVO;
import com.siact.hydrocore.module.system.vo.SysUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "系统用户管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysuser")
public class SysUserController {
    private final SysUserService service;

    @ApiOperation("分页列表")
    @PostMapping("/list")
    public PageVO<SysUserVO> list(@RequestBody SysUserQuery query) {
        return service.list(query);
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Boolean create(@RequestBody SysUserCreateCommand command) {
        return service.create(command);
    }

    @ApiOperation("编辑用户")
    @PutMapping
    public Boolean update(@RequestBody SysUserUpdateCommand command) {
        return service.update(command);
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @ApiOperation("重置密码")
    @PutMapping("/{id}/reset-password")
    public Boolean resetPassword(@PathVariable Long id, @RequestBody ResetPasswordCommand command) {
        return service.resetPassword(id, command);
    }

    @ApiOperation("分配角色")
    @PutMapping("/{id}/roles")
    public Boolean assignRoles(@PathVariable Long id, @RequestBody AssignUserRoleCommand command) {
        service.assignRoles(id, command.getRoleIds());
        return true;
    }

    @ApiOperation("获取用户角色ID列表")
    @GetMapping("/{id}/roles")
    public List<Long> getRoleIds(@PathVariable Long id) {
        return service.getRoleIds(id);
    }

    @ApiOperation("获取用户菜单树")
    @GetMapping("/{id}/menus")
    public List<SysMenuTreeVO> getUserMenus(@PathVariable Long id) {
        return service.getUserMenus(id);
    }
}

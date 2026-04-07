package com.siact.module.system.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.service.SysMenuService;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
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
    public PageVO<SysMenuVO> list(@RequestBody SysMenuQuery query) {
        return service.list(query);
    }

    @ApiOperation("菜单树")
    @GetMapping("/tree")
    public List<SysMenuTreeVO> tree() {
        return service.tree();
    }

    @ApiOperation("新增菜单")
    @PostMapping
    public Boolean create(@RequestBody SysMenuCreateCommand command) {
        return service.create(command);
    }

    @ApiOperation("编辑菜单")
    @PutMapping
    public Boolean update(@RequestBody SysMenuUpdateCommand command) {
        return service.update(command);
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }
}

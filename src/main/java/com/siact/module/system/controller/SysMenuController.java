package com.siact.module.system.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuDeleteCommand;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.service.SysMenuService;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:27
 * @className : SysMenuController
 * @description : 系统菜单控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysmenu")
public class SysMenuController {
    private final SysMenuService service;

    public @PostMapping("/list") PageVO<SysMenuVO> list(@RequestBody SysMenuQuery query) {
        return service.list(query);
    }

    public @GetMapping("/tree") List<SysMenuTreeVO> tree() {
        return service.tree();
    }

    public @PostMapping("/add") Boolean create(@RequestBody SysMenuCreateCommand command) {
        return service.create(command);
    }

    public @DeleteMapping("/delete") Boolean delete(@RequestBody List<SysMenuDeleteCommand> commands) {
        return service.delete(commands);
    }
}

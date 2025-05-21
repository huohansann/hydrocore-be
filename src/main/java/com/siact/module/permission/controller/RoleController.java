package com.siact.module.permission.controller;

import com.siact.common.result.R;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.dto.RoleDTO;
import com.siact.module.permission.entity.RoleEntity;
import com.siact.module.permission.service.RoleService;
import com.siact.module.permission.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 角色Controller
 *
 * @author example
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleController {
    
    @Resource
    private RoleService roleService;
    
    @ApiOperation("新增角色")
    @PostMapping("save")
    public R<Boolean> save(@RequestBody @Validated RoleDTO request) {
        boolean result = roleService.saveRole(request);
        return R.data(result);
    }
    
    @ApiOperation("修改角色")
    @PostMapping("update")
    public R<Boolean> update(@RequestBody @Validated RoleDTO request) {
        boolean result = roleService.updateRole(request);
        return R.data(result);
    }
    
    @ApiOperation("删除角色")
    @ApiImplicitParam(name = "id", value = "角色ID", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        boolean result = roleService.deleteRole(id);
        return R.data(result);
    }
    
    @ApiOperation("分页查询角色")
    @PostMapping("/page")
    public R<PageVO<RoleEntity>> page(@RequestBody PageDTO request) {
        PageVO<RoleEntity> page = roleService.pageRole(request);
        return R.data(page);
    }
    
    @ApiOperation("获取角色详情")
    @ApiImplicitParam(name = "id", value = "角色ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/{id}")
    public R<RoleEntity> getById(@PathVariable Long id) {
        RoleEntity role = roleService.getRoleById(id);
        return R.data(role);
    }
    
    @ApiOperation("获取所有角色列表")
    @GetMapping("/list")
    public R<List<RoleEntity>> list() {
        List<RoleEntity> list = roleService.listAllRoles();
        return R.data(list);
    }
    
    @ApiOperation("分配菜单权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "menuIds", value = "菜单ID列表", required = true, dataType = "List")
    })
    @PostMapping("/assign/menus/{roleId}")
    public R<Boolean> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        boolean result = roleService.assignMenus(roleId, menuIds);
        return R.data(result);
    }
} 
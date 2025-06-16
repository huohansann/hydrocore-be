package com.siact.module.permission.controller;

import com.siact.common.result.R;
import com.siact.module.permission.dto.MenuDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.entity.MenuEntity;
import com.siact.module.permission.service.MenuService;
import com.siact.module.permission.vo.MenuVO;
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
 * 菜单Controller
 *
 * @author example
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/menu")
public class MenuController {
    
    @Resource
    private MenuService menuService;
    
    @ApiOperation("新增菜单")
    @PostMapping("save")
    public R<Boolean> save(@RequestBody @Validated MenuDTO request) {
        boolean result = menuService.saveMenu(request);
        return R.data(result);
    }
    
    @ApiOperation("修改菜单")
    @PostMapping("update")
    public R<Boolean> update(@RequestBody @Validated MenuDTO request) {
        boolean result = menuService.updateMenu(request);
        if (!result) {
            return R.fail("修改失败，请检查父级菜单设置是否合理");
        }
        return R.data(result);
    }
    
    @ApiOperation("删除菜单")
    @ApiImplicitParam(name = "id", value = "菜单ID", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        boolean result = menuService.deleteMenu(id);
        if (!result) {
            return R.fail("删除失败，该菜单可能存在子菜单");
        }
        return R.data(true);
    }
    
    @ApiOperation("获取菜单树")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "parentId", value = "父级菜单ID", required = false, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "modelShow", value = "父级菜单ID", required = false, dataType = "Long", paramType = "path")

    })
    @GetMapping("/tree")
    public R<List<MenuVO>> tree(Long parentId, Integer modelShow) {
        List<MenuVO> tree = menuService.getMenuTree(parentId,modelShow);
        return R.data(tree);
    }
    
    @ApiOperation("分页查询菜单")
    @GetMapping("/page")
    public R<PageVO<MenuEntity>> page(@Validated PageDTO request) {
        PageVO<MenuEntity> page = menuService.pageMenu(request);
        return R.data(page);
    }
    
    @ApiOperation("获取菜单详情")
    @ApiImplicitParam(name = "id", value = "菜单ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/{id}")
    public R<MenuEntity> getById(@PathVariable Long id) {
        MenuEntity menu = menuService.getMenuById(id);
        return R.data(menu);
    }
    
    @ApiOperation("根据角色ID列表获取菜单树")
    @ApiImplicitParam(name = "roleIds", value = "角色ID列表", required = true, dataType = "List", paramType = "body")
    @PostMapping("/role/tree")
    public R<List<MenuVO>> getMenuTreeByRoleIds(@RequestBody List<Long> roleIds) {
        List<MenuVO> tree = menuService.getMenusByRoleIds(roleIds);
        return R.data(tree);
    }

    @ApiOperation("根据角色ID列表获取菜单id集合")
    @ApiImplicitParam(name = "roleIds", value = "角色ID列表", required = true, dataType = "List", paramType = "body")
    @PostMapping("/role/getMenuIdsByRoleIds")
    public R<List<Long>> getMenuIdsByRoleIds(@RequestBody List<Long> roleIds) {
        List<Long> menuIds = menuService.getMenuIdsByRoleIds(roleIds);
        return R.data(menuIds);
    }
} 
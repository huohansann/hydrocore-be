package com.siact.module.permission.controller;

import com.siact.common.result.R;
import com.siact.module.permission.dto.OrganizationDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.entity.OrganizationEntity;
import com.siact.module.permission.service.OrganizationService;
import com.siact.module.permission.vo.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
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
 * 部门Controller
 *
 * @author example
 */
@Api(tags = "部门管理")
@RestController
@RequestMapping("/organization")
public class OrganizationController {
    
    @Resource
    private OrganizationService organizationService;
    
    @ApiOperation("新增部门")
    @PostMapping("/save")
    public R<Long> save(@RequestBody @Validated OrganizationDTO request) {
        Long id = organizationService.saveOrganization(request);
        return R.data(id);
    }
    
    @ApiOperation("修改部门")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody @Validated OrganizationDTO request) {
        boolean result = organizationService.updateOrganization(request);
        return R.data(result);
    }
    
    @ApiOperation("删除部门")
    @ApiImplicitParam(name = "id", value = "部门ID", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        boolean result = organizationService.deleteOrganization(id);
        if (!result) {
            return R.fail("删除失败，该部门可能存在子部门");
        }
        return R.data(true);
    }
    
    @ApiOperation("获取部门树")
    @GetMapping("/tree")
    public R<List<OrganizationEntity>> tree() {
        List<OrganizationEntity> tree = organizationService.getOrganizationTree();
        return R.data(tree);
    }
    
    @ApiOperation("分页查询部门")
    @PostMapping("/page")
    public R<PageVO<OrganizationEntity>> page(@RequestBody @Validated PageDTO request) {
        PageVO<OrganizationEntity> page = organizationService.pageOrganization(request);
        return R.data(page);
    }
    
    @ApiOperation("获取部门详情")
    @ApiImplicitParam(name = "id", value = "部门ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/getById/{id}")
    public R<OrganizationEntity> getById(@PathVariable Long id) {
        OrganizationEntity organization = organizationService.getOrganizationById(id);
        return R.data(organization);
    }
} 
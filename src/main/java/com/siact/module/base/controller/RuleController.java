package com.siact.module.base.controller;

import com.siact.common.result.R;
import com.siact.module.base.dto.RuleAddDTO;
import com.siact.module.base.service.IRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@Api(tags = "条规管理")
@RestController
@RequestMapping("/rule")
public class RuleController {
    @Autowired
    private IRuleService ruleService;

    @ApiOperation("查询条规列表")
    @GetMapping("/list")
    public R list() {
        return R.data(ruleService.listRules());
    }

    @ApiOperation("详情")
    @GetMapping("/detail/{ruleCode}")
    public R detail(@PathVariable @NotBlank(message = "规则编码不能为空") String ruleCode) {
        return R.data(ruleService.detail(ruleCode));
    }

    @ApiOperation("保存条规")
    @PostMapping("/save")
    public R save(@RequestBody RuleAddDTO dto) {
        return R.data(ruleService.save(dto));
    }

    @ApiOperation("删除条规(硬删除)")
    @GetMapping("/delete/{ruleCode}")
    public R delete(@PathVariable @NotBlank(message = "规则编码不能为空") String ruleCode) {
        return R.data(ruleService.delete(ruleCode));
    }
} 
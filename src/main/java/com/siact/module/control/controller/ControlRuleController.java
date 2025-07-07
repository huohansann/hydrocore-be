package com.siact.module.control.controller;

import com.siact.common.result.R;
import com.siact.module.control.dto.ControlRuleDTO;
import com.siact.module.control.dto.ControlRuleQuery;
import com.siact.module.control.service.ControlRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "窑炉约束规则")
@RestController
@RequestMapping("/controlRule")
public class ControlRuleController {

    @Autowired
    private ControlRuleService controlRuleService;

    @ApiOperation("查询约束规则列表")
    @PostMapping("/list")
    public R list(@RequestBody ControlRuleQuery query) {
        return R.data(controlRuleService.selectControlRuleList(query));
    }

    @ApiOperation("查询约束规则是否允许修改")
    @PostMapping("/legal")
    public R legal(@RequestBody ControlRuleQuery query) {
        return R.data(controlRuleService.legal(query));
    }

    @ApiOperation("获取约束规则信息")
    @GetMapping("/getInfo/{id}")
    public R getInfo(@PathVariable("id") Long id) {
        return R.data(controlRuleService.selectControlRuleById(id));
    }

    @ApiOperation("新增约束规则")
    @PostMapping("/add")
    public R add(@RequestBody ControlRuleDTO dto) {
        return R.data(controlRuleService.insertControlRule(dto));
    }

    @ApiOperation("修改约束规则")
    @PostMapping("/batchEdit")
    public R edit(@RequestBody List<ControlRuleDTO> dtoList) {
        return R.data(controlRuleService.updateControlRule(dtoList));
    }

    @ApiOperation("删除约束规则")
    @GetMapping("/remove/{ids}")
    public R remove(@PathVariable Long[] ids) {
        return R.data(controlRuleService.deleteControlRuleByIds(ids));
    }
}

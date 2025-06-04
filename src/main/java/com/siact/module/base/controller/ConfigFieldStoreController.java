package com.siact.module.base.controller;

import com.siact.common.result.R;
import com.siact.module.base.dto.ConfigFieldStoreDTO;
import com.siact.module.base.dto.ConfigFieldStoreQuery;
import com.siact.module.base.service.IConfigFieldStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "配置字段存储")
@RestController
@RequestMapping("/configFieldStore")
public class ConfigFieldStoreController {
    @Autowired
    private IConfigFieldStoreService configFieldStoreService;

    @ApiOperation("查询配置列表")
    @PostMapping("/list")
    public R list(@RequestBody ConfigFieldStoreQuery query) {
        return R.data(configFieldStoreService.selectConfigFieldStoreList(query));
    }

    @ApiOperation("获取配置信息")
    @GetMapping("/getInfo/{id}")
    public R getInfo(@PathVariable("id") Long id) {
        return R.data(configFieldStoreService.selectConfigFieldStoreById(id));
    }

    @ApiOperation("新增配置")
    @PostMapping("/add")
    public R add(@RequestBody ConfigFieldStoreDTO dto) {
        return R.data(configFieldStoreService.insertConfigFieldStore(dto));
    }

    @ApiOperation("修改配置")
    @PostMapping
    public R edit(@RequestBody ConfigFieldStoreDTO dto) {
        return R.data(configFieldStoreService.updateConfigFieldStore(dto));
    }

    @ApiOperation("删除配置")
    @GetMapping("/remove/{ids}")
    public R remove(@PathVariable Long[] ids) {
        return R.data(configFieldStoreService.deleteConfigFieldStoreByIds(ids));
    }
} 
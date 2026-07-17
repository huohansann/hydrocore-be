package com.siact.hydrocore.module.base.controller;

import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.module.base.dto.ConfigFieldStoreDTO;
import com.siact.hydrocore.module.base.dto.ConfigFieldStoreQuery;
import com.siact.hydrocore.module.base.service.IConfigFieldStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "配置字段存储")
@RestController
@RequestMapping("/configFieldStore")
public class ConfigFieldStoreController {
    @Autowired
    private IConfigFieldStoreService configFieldStoreService;

    @ApiOperation("查询配置列表")
    @PostMapping("/list")
    public ApiResponse<?> list(@RequestBody ConfigFieldStoreQuery query) {
        return ApiResponse.success(configFieldStoreService.selectConfigFieldStoreList(query));
    }

    @ApiOperation("获取配置信息")
    @GetMapping("/getInfo/{id}")
    public ApiResponse<?> getInfo(@PathVariable("id") Long id) {
        return ApiResponse.success(configFieldStoreService.selectConfigFieldStoreById(id));
    }

    @ApiOperation("新增配置")
    @PostMapping("/add")
    public ApiResponse<?> add(@RequestBody ConfigFieldStoreDTO dto) {
        return ApiResponse.success(configFieldStoreService.insertConfigFieldStore(dto));
    }

    @ApiOperation("修改配置")
    @PostMapping("/batchEdit")
    public ApiResponse<?> edit(@RequestBody List<ConfigFieldStoreDTO> dtoList) {
        return ApiResponse.success(configFieldStoreService.updateConfigFieldStore(dtoList));
    }

    @ApiOperation("删除配置")
    @GetMapping("/remove/{ids}")
    public ApiResponse<?> remove(@PathVariable Long[] ids) {
        return ApiResponse.success(configFieldStoreService.deleteConfigFieldStoreByIds(ids));
    }
}

package com.siact.hydrocore.module.device.controller;

import com.siact.hydrocore.common.annotation.NoResponseAdvice;
import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.device.command.DeviceMappingCommand;
import com.siact.hydrocore.module.device.query.DeviceMappingQuery;
import com.siact.hydrocore.module.device.service.DeviceMappingService;
import com.siact.hydrocore.module.device.vo.DeviceImportResult;
import com.siact.hydrocore.module.device.vo.DeviceMappingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "设备点位管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/device/mapping")
public class DeviceMappingController {
    private final DeviceMappingService service;

    @ApiOperation("分页查询")
    @GetMapping("/page")
    public ApiResponse<PageVO<DeviceMappingVO>> page(DeviceMappingQuery query) {
        return ApiResponse.success(service.list(query));
    }

    @ApiOperation("查询单条")
    @GetMapping("/{id}")
    public ApiResponse<DeviceMappingVO> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping("/add")
    public ApiResponse<Boolean> add(@Valid @RequestBody DeviceMappingCommand command) {
        return ApiResponse.success(service.add(command));
    }

    @ApiOperation("修改")
    @PutMapping("/update")
    public ApiResponse<Boolean> update(@Valid @RequestBody DeviceMappingCommand command) {
        return ApiResponse.success(service.update(command));
    }

    @ApiOperation("单条删除")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.success(service.delete(id));
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        return ApiResponse.success(service.deleteBatch(ids));
    }

    @ApiOperation("全部清空")
    @DeleteMapping("/clear")
    public ApiResponse<Boolean> clear() {
        return ApiResponse.success(service.clear());
    }

    @ApiOperation("导入设备点位")
    @PostMapping("/import")
    public ApiResponse<DeviceImportResult> importData(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(service.importData(file));
    }

    @NoResponseAdvice
    @ApiOperation("导出设备点位")
    @GetMapping("/export")
    public void exportData(DeviceMappingQuery query, @RequestParam(defaultValue = "excel") String format,
                           HttpServletResponse response) {
        service.exportData(query, format, response);
    }

    @NoResponseAdvice
    @ApiOperation("下载导入模板")
    @GetMapping("/import-template")
    public void downloadImportTemplate(HttpServletResponse response) {
        service.downloadImportTemplate(response);
    }
}

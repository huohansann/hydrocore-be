package com.siact.hydrocore.module.device.controller;

import com.siact.hydrocore.common.annotation.NoResponseAdvice;
import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.device.query.DeviceRealtimeQuery;
import com.siact.hydrocore.module.device.service.DeviceRealtimeService;
import com.siact.hydrocore.module.device.vo.DeviceRealtimeVO;
import com.siact.hydrocore.module.device.vo.SelectOptionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "设备实时数据")
@RequiredArgsConstructor
@RestController
@RequestMapping("/device/realtime")
public class DeviceRealtimeController {
    private final DeviceRealtimeService service;

    @ApiOperation("点位ID下拉选项")
    @GetMapping("/itemIds")
    public ApiResponse<List<SelectOptionVO>> listItemIds() {
        return ApiResponse.success(service.listItemIds());
    }

    @ApiOperation("设备名称下拉选项")
    @GetMapping("/deviceNames")
    public ApiResponse<List<SelectOptionVO>> listDeviceNames() {
        return ApiResponse.success(service.listDeviceNames());
    }

    @ApiOperation("分页查询实时数据")
    @PostMapping("/query")
    public ApiResponse<PageVO<DeviceRealtimeVO>> query(@Valid @RequestBody DeviceRealtimeQuery query,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.query(query, page, pageSize));
    }

    @NoResponseAdvice
    @ApiOperation("导出实时数据")
    @GetMapping("/export")
    public void export(DeviceRealtimeQuery query,
                       @RequestParam(defaultValue = "excel") String format,
                       HttpServletResponse response) {
        service.export(query, format, response);
    }
}

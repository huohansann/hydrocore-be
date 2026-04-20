package com.siact.module.device.controller;

import com.siact.common.annotation.NoResponseAdvice;
import com.siact.common.vo.PageVO;
import com.siact.module.device.query.DeviceRealtimeQuery;
import com.siact.module.device.service.DeviceRealtimeService;
import com.siact.module.device.vo.DeviceRealtimeVO;
import com.siact.module.device.vo.SelectOptionVO;
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
    public List<SelectOptionVO> listItemIds() {
        return service.listItemIds();
    }

    @ApiOperation("设备名称下拉选项")
    @GetMapping("/deviceNames")
    public List<SelectOptionVO> listDeviceNames() {
        return service.listDeviceNames();
    }

    @ApiOperation("分页查询实时数据")
    @PostMapping("/query")
    public PageVO<DeviceRealtimeVO> query(@Valid @RequestBody DeviceRealtimeQuery query,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return service.query(query, page, pageSize);
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

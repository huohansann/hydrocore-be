package com.siact.module.control.controller;

import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.GasForecastQueryDTO;
import com.siact.module.control.service.ControlSettingGasService;
import com.siact.module.control.vo.GasForecastVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-04 13:37
 * @className : ControlSettingGasController
 * @description : 天然气设置控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/control/gas")
public class ControlSettingGasController {
    private final ControlSettingGasService service;

    public @GetMapping("/setting") List<ControlSettingGasDTO> setting() {
        return service.querySetting();
    }

    public @PostMapping("/publish") Boolean publish(@RequestBody List<ControlSettingGasDTO> list) {
        return service.publish(list);
    }

    /**
     * 天然气历史数据+预测数据
     */
    public @PostMapping("/forecast") GasForecastVO forecast(@RequestBody GasForecastQueryDTO query) {
        return service.forecast(query);
    }

    /**
     * 天然气预测点位配置项
     */
    public @GetMapping("/forecast/config") List<Map<String, String>> forecastConfig() {
        return service.queryForecastConfig();
    }
}

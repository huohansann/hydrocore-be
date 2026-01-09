package com.siact.module.control.controller;

import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.service.ControlSettingGasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}

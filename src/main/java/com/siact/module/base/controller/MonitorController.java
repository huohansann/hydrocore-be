package com.siact.module.base.controller;

import com.siact.common.R;
import com.siact.module.base.dto.Load;
import com.siact.module.base.service.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;

/**
 * @author Bruce_Hmz
 * @date 2025/4/27
 */
@Api(tags = "监控服务")
@RestController
@RequestMapping("/monitor")
@Slf4j
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @ApiOperation(value = "查询负载率数据")
    @GetMapping("/queryLoadRate")
    public R<Load> queryLoadRate(@NotBlank String dataCode) throws IOException {
        log.info("查询负载率数据入参：{}", dataCode);
        Load load = monitorService.queryLoadRate(dataCode);
        return R.data(load);
    }
}

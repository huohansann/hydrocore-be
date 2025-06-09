package com.siact.module.control.controller;

import com.siact.common.R;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.control.service.KilnPublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "窑炉控制下发")
@RestController
@RequestMapping("/control")
public class KilnPublishController {

    @Autowired
    private KilnPublishService kilnPublishService;


    @ApiOperation("天然气与风气值下发")
    @PostMapping("/publish")
    public R publish(@RequestBody List<KilnInfoDistributeDTO> list) {
        return kilnPublishService.publish(list);
    }
}

package com.siact.module.model.controller;

import com.siact.common.R;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.vo.ModelConfigParamSaveVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "模型配置")
@RestController
@RequestMapping("/model")
public class ModelConfigParamController {

    @Autowired
    private ModelConfigParamService modelConfigParamService;

    @ApiOperation(value = "保存参数")
    @PostMapping("/saveParam")
    public R saveParam(@RequestBody ModelConfigParamSaveVO configParamSaveVo) {
        modelConfigParamService.saveParam(configParamSaveVo);
        return R.success();
    }

}

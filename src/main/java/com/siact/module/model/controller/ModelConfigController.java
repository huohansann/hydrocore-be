package com.siact.module.model.controller;

import com.siact.common.R;
import com.siact.common.exception.CustomException;
import com.siact.module.model.dto.ModelAssessChartDTO;
import com.siact.module.model.dto.ModelConfigParamRtnDTO;
import com.siact.module.model.dto.ModelOutputSelectRtnDTO;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.model.vo.ModelConfigParamSaveVO;
import com.siact.module.model.vo.PublishModelVO;
import com.siact.module.permission.vo.ModelQueryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Api(tags = "模型配置")
@RestController
@Validated
@RequestMapping("/model")
public class ModelConfigController {

    @Autowired
    private ModelConfigParamService modelConfigParamService;

    @Autowired
    private ModelInfoService modelInfoService;

    @ApiOperation(value = "获取参数模板")
    @GetMapping("/getParamTemplate")
    public R<Map<String, String>> getParamTemplate() {
        // 获取参数模板
        return R.success(modelConfigParamService.getParamTemplate());
    }

    @ApiOperation(value = "查询当前模型对应时间步长的参数配置")
    @GetMapping("/queryParam")
    public R<ModelConfigParamRtnDTO> queryParamByDataCodeAndPredictedTypeCode(@RequestParam @NotBlank(message = "dataCode不能为空") String dataCode,
                                                                              @RequestParam @NotBlank(message = "步长code不能为空") String predictedTypeCode) {
        // 获取参数模板
        return R.success(modelConfigParamService.queryParamByDataCodeAndPredictedTypeCode(dataCode, predictedTypeCode));
    }

    @ApiOperation(value = "保存/修改参数")
    @PostMapping("/saveOrUpdateParam")
    public R saveOrUpdateParam(@RequestBody @Validated ModelConfigParamSaveVO configParamSaveVo) {
        modelConfigParamService.saveOrUpdateParam(configParamSaveVo);
        return R.success();
    }

    @ApiOperation(value = "下发参数生成对应模型")
    @PostMapping("/sendParam")
    public R sendParamById(@RequestBody @NotBlank(message = "id不能为空") Long id) {
        modelConfigParamService.sendParamById(id);
        return R.success();
    }

    @ApiOperation(value = "查询当前模型对应时间步长的所有模型(包含指标评价)")
    @PostMapping("/queryModel")
    public R<ModelOutputSelectRtnDTO> queryModelByDataCodeGroupByPredictedTypeCodes(@RequestBody @Validated ModelQueryVO modelQueryVO) {
        ModelOutputSelectRtnDTO rtnDTOMap = modelInfoService.queryModelByDataCodeGroupByPredictedTypeCodes(modelQueryVO.getDataCode(), modelQueryVO.getPredictedTypeCodeList());
        return R.success(rtnDTOMap);
    }

    @ApiOperation(value = "根据模型id,获取指标评价的柱状图")
    @PostMapping("/queryModelAssessChart")
    public R<ModelAssessChartDTO> queryModelAssessChart(@RequestBody List<Long> modelIdList) {
        if (ObjectUtils.isEmpty(modelIdList)) {
            throw new CustomException("未选择下发模型");
        }
        ModelAssessChartDTO rtnDTOMap = modelInfoService.queryModelAssessChart(modelIdList);
        return R.success(rtnDTOMap);
    }

    @ApiOperation(value = "下发模型")
    @PostMapping("/publishModel")
    public R sendModel(@RequestBody @Validated PublishModelVO publishModelVO) {
        modelInfoService.publishModel(publishModelVO);
        return R.success();
    }

}

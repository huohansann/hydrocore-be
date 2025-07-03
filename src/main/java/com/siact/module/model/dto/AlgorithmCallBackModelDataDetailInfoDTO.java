package com.siact.module.model.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("算法回调模型数据详情信息")
public class AlgorithmCallBackModelDataDetailInfoDTO {
    @JSONField(name = "true")
    @ApiModelProperty("真实值")
    private List<Integer> trueVal;

    @ApiModelProperty("预测值")
    private List<Integer> predict;
}

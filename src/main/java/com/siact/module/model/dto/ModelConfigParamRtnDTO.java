package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("参数设置Dto")
public class ModelConfigParamRtnDTO {
    private Long id;

    @ApiModelProperty(value = "预测设备的孪生code")
    private String dataCode;

    @ApiModelProperty(value = "预测类型 1:单步预测 2:多步预测")
    private Integer predictedType;

    @ApiModelProperty(value = "预测类型Code,单步如:T20,T40,多步:MULTI")
    private String predictedTypeCode;

    @ApiModelProperty(value = "公共参数配置(页面配置json)")
    private ModelConfigParamDTO publicSetting;

    @ApiModelProperty(value = "算法参数配置(页面配置json)")
    private List<ModelConfigParamDetailDTO> algorithmSetting;

//    @ApiModelProperty(value = "创建时间")
//    private Date createTime;

    @ApiModelProperty(value = "算法生成模型训练集开始时间")
    @NotNull(message = "算法生成模型训练集开始时间不能为空")
    private Date trainDataStartTime;

    @ApiModelProperty(value = "算法生成模型训练集结束时间")
    @NotNull(message = "算法生成模型训练集结束时间不能为空")
    private Date trainDataEndTime;
}

package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value ="模型数据")
public class ModelInfoDTO {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("模型数据的dataCode")
    private String dataCode;

    @ApiModelProperty("算法Code")
    private String algorithmCode;

    @ApiModelProperty("预测类型 1:单步预测 2:多步预测")
    private Integer predictedType;

    @ApiModelProperty("预测类型Code,单步如:T20,T40,多步:MULTI")
    private String predictedTypeCode;

    @ApiModelProperty("模型设置的id")
    private Long configParamId;

    @ApiModelProperty("模型生成状态  1:生成中 2:生成成功 3:生成失败")
    private Integer status;

    @ApiModelProperty("自定义模型名称")
    private String customModelName;

    @ApiModelProperty("模型名称(算法)")
    private String modelName;

    @ApiModelProperty("模型Code(算法)")
    private String modelCode;

    @ApiModelProperty("决定系数")
    private String determination;

    @ApiModelProperty("MSE均方误差")
    private String mse;

    @ApiModelProperty("MAE平均绝对误差")
    private String mae;

    @ApiModelProperty("Accuracy精度")
    private String accuracy;

    @ApiModelProperty("是否有效 1:有效,0:无效")
    private Integer valid;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("当前模型是否选中")
    private Boolean selected;
}

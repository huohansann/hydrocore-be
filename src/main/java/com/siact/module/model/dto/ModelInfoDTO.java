package com.siact.module.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ModelInfoDTO {

    private Long id;

    // 模型数据的dataCode
    private String dataCode;

    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;

    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;

    // 模型生成状态  1:生成中 2:生成成功 3:生成失败
    private Integer status;

    // 模型名称
    private String modelName;

    // 模型Code
    private String modelCode;

    // 决定系数
    private String determination;

    // MSE均方误差
    private String mse;

    // MAE平均绝对误差
    private String mae;

    // Accuracy精度
    private String accuracy;

    private Date createTime;

    private Date updateTime;
}

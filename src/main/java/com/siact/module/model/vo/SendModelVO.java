package com.siact.module.model.vo;

import lombok.Data;

@Data
public class SendModelVO {
    private Long id;

    // 模型数据的dataCode
    private String dataCode;

    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;

    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;

    // 模型名称
    private String modelName;

    // 模型Code
    private String modelCode;
}

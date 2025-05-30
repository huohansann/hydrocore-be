package com.siact.module.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("model_publish_info")
public class ModelPublishInfoEntity {
    private Long id;
    // 模型数据的dataCode
    private String dataCode;
    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;
    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;
    // 算法模型信息id
    private Long modelInfoId;
    // 算法模型code
    private String modelCode;
    private Date createTime;
}

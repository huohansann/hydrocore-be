package com.siact.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("model_info")
public class ModelInfoEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    // 模型数据的dataCode
    private String dataCode;
    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;
    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;
    // 算法响应的参数(择到另一张表,可能是mqtt或者是回调接口,主要是记录)
    private String algorithmResp;
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

}

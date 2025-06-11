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
    private Long id;
    // 模型数据的dataCode
    private String dataCode;
    // 算法code
    private String algorithmCode;
    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;
    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;
    // 模型生成状态  1:生成中 2:生成成功 3:生成失败
    private Integer status;
    // 算法响应的参数(择到另一张表,可能是mqtt或者是回调接口,主要是记录)
    private String algorithmResp;
    // 自定义模型名称
    private String customModelName;
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
    // 状态1:有效,0:无效
    private Integer valid;
    private Date createTime;
    private Date updateTime;

}

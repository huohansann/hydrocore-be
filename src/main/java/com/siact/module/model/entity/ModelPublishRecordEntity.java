package com.siact.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("model_publish_record")
public class ModelPublishRecordEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    // 模型数据的dataCode
    private String dataCode;
    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;
    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;
    // 下发配置id
    private Long publishInfoId;
    // 算法模型信息id
    private Long modelInfoId;
    // 算法模型code
    private String modelCode;
    // 模型下发状态(0:未下发 1:下发中 2:下发完成)
    private String status;
    // 算法响应的参数(择到另一张表,可能是mqtt或者是回调接口,主要是记录)
    private String algorithmResp;
    private Date createTime;

}

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
    // 模型发布信息id
    private Long publishInfoId;
    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;
    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;
    // 下发配置id
    private String publishParam;
    // 算法模型信息id
    private Long modelInfoId;
    // 模型下发状态(0:未下发 1:下发中 2:下发完成)
    private Integer status;
    //  创建时间
    private Date createTime;
    // 更新时间
    private Date updateTime;

}

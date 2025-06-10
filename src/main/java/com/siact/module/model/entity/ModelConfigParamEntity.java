package com.siact.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("model_config_param")
public class ModelConfigParamEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    // 预测设备的孪生code
    private String dataCode;
    // 预测类型 1:单步预测 2:多步预测
    private Integer predictedType;
    // 预测类型Code,单步如:T20,T40,多步:MULTI
    private String predictedTypeCode;
    // 公共参数配置(页面配置json)
    private String publicSetting;
    // 算法参数配置(页面配置json)
    private String algorithmSetting;
    private Date createTime;
    // 自定义模型名称
    private String customModelName;

}

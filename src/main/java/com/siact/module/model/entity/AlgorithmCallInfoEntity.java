package com.siact.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("algorithm_call_info")
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmCallInfoEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    // 响应类型:train(调用算法训练模型),inference(调用算法获取预测数据),modelInfo(算法回调模型数据)
    private String type;
    // 模型ID
    private Long modelId;
    // 请求时间
    private String reqTime;
    // 请求参数
    private String reqJson;
    // 响应时间
    private String respTime;
    // 响应参数
    private String respJson;
    // 创建时间
    private Date createTime;
}

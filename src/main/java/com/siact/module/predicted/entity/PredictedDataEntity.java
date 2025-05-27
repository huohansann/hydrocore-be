package com.siact.module.predicted.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("predicted_data")
public class PredictedDataEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String dataCode;
    private Integer predictedType;
    private String predictedTypeCode;
    private String time;
    private String itemVal;
    private String unit;
    private Date createTime;
}

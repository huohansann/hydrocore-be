package com.siact.module.predicted.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AlgorithmPredictionCallDataDTO {
    @ApiModelProperty("预测数据的时间")
    private String time;
    @ApiModelProperty("算法预测完成的时间")
    private String end_time;
    @ApiModelProperty("算法预测结果")
    private Map<String, List<BigDecimal>> result;
}

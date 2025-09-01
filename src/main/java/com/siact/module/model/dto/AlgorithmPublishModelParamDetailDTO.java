package com.siact.module.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AlgorithmPublishModelParamDetailDTO {

    @ApiModelProperty("算法模型id")
    private String model_id;

    @ApiModelProperty("算法模型名称")
    private String model_name;

    @ApiModelProperty("选中的算法类型,如bp")
    private String method;

    @ApiModelProperty("工况总数")
    private Integer work_code_num;

    @ApiModelProperty("当前工况编码")
    private Integer work_code;

    @ApiModelProperty("预测base数据")
    private Map<String, String> data;

    @ApiModelProperty("预测的数据为前多少分钟-开始范围")
    private Integer rangeStart;

    @ApiModelProperty("预测的数据为前多少分钟-结束范围")
    private Integer rangeEnd;

    @ApiModelProperty("预测采样间隔")
    private String sample;

    @ApiModelProperty("预测类型,单步('single_step')或多步('multiple_step')")
    private String type;

    @ApiModelProperty("预测步数,即步长T20,为20,T40为40")
    private Integer future_number;
}

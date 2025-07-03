package com.siact.module.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AlgorithmGenerateModelParamDTO {
    @ApiModelProperty("窑炉系统的,唯一标识id")
    private String model_id;
    @ApiModelProperty("公共参数设置,(列表,参与训练的特征名)")
    private List<String> features;
    @ApiModelProperty("需要计算的目标列,这里固定为温度预测对应的code,即MC1..对应的code")
    private String target;
    @ApiModelProperty("选中的算法类型,如bp")
    private String method;
    @ApiModelProperty("算法参数设置")
    private Map<String, Object> method_par;
    @ApiModelProperty("one-hot编码，工况总数")
    private Integer work_code_num;

    @ApiModelProperty("算法预测需要的点位数据(需要排除换机等日期数据,多组数据需要分组)")
    private Map<String, Map<String, List<BigDecimal>>> data;

    @ApiModelProperty("预测类型,单步('single_step')或多步('multiple_step')")
    private String type;
    @ApiModelProperty("预测的数据为前多少分钟")
    private String past_number;
    @ApiModelProperty("预测步数,即步长T20,为20,T40为40")
    private Integer future_number;

    @ApiModelProperty("数据比例,用于数据集的划分")
    private List<Double> data_rate;

}

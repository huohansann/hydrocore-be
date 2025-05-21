package com.siact.common.vo;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: wr
 * @create: 2025-05-13 10:38
 */
@Data
@ApiModel(description = "报表数据")
public class ReportDataVo {
    @ApiModelProperty(value = "表头数据")
    private List<ReportRowDataVo> headList;

    @ApiModelProperty(value = "数据")
    private List<JSONObject> dataList;

    @ApiModelProperty(value = "总数")
    private int total;
}
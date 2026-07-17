package com.siact.hydrocore.common.vo;

import com.alibaba.fastjson2.JSONObject;
import com.siact.hydrocore.common.dto.PageDataDto;
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
public class ReportDataPageVo {
    @ApiModelProperty(value = "表头数据")
    private List<ReportRowDataVo> headList;

    @ApiModelProperty(value = "数据")
    private PageDataDto<JSONObject> pageInfo;

    @ApiModelProperty(value = "总数")
    private int total;
}
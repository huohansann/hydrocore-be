package com.siact.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc: 报表数据
 * @author: wr
 * @create: 2025-05-13 10:39
 */
@Data
@ApiModel(description = "报表行数据")
public class ReportCellDataVo {
    @ApiModelProperty(value = "属性值")
    private String dataVal;

    @ApiModelProperty(value = "下级属性")
    private List<ReportCellDataVo> children;
}
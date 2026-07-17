package com.siact.hydrocore.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: wr
 * @create: 2025-05-13 10:39
 */
@Data
@ApiModel(description = "报表行数据")
public class ReportRowDataVo {

    @ApiModelProperty(value = "参数dataCode")
    private String dataCode;

    @ApiModelProperty(value = "参数code")
    private String paramCode;

    @ApiModelProperty(value = "参数名称")
    private String paramName;

    @ApiModelProperty(value = "参数类型 1:first 2:inc")
    private String paramType;

    @ApiModelProperty(value = "单位")
    private String unit;

//    @ApiModelProperty(value = "是否合并")
//    private boolean  merge;
//
//    @ApiModelProperty(value = "是否展示")
//    private boolean  isShow;

    @ApiModelProperty(value = "下级参数信息")
    private List<ReportRowDataVo> children;

    public ReportRowDataVo() {
    }

    public ReportRowDataVo(String dataCode,String paramCode,  String paramName) {
        this.dataCode = dataCode;
        this.paramCode = paramCode;
        this.paramName = paramName;
    }
}
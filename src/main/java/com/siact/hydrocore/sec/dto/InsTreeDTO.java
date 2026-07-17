package com.siact.hydrocore.sec.dto;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class InsTreeDTO {
    @ApiModelProperty(
            value = "实例ID",
            position = 1
    )
    private Long insId;
    @ApiModelProperty(
            value = "实例名称",
            position = 2
    )
    private String insName;
    @ApiModelProperty(
            value = "实例编码",
            position = 3
    )
    private String insCode;
    @ApiModelProperty(
            value = "节点类型（项目：project；系统：system；站点：station；单元：unit；管路：pipe；设备：eq；表计：meter）",
            position = 4
    )
    private String nodeType;
    @ApiModelProperty(
            value = "数字化编码",
            position = 5
    )
    private String dataCode;
    @ApiModelProperty(
            value = "父节点数字化编码",
            position = 6
    )
    private String parentDataCode;
    @ApiModelProperty(
            value = "子数据列表",
            position = 7
    )
    private List<InsTreeDTO> children = Lists.newArrayList();
    @ApiModelProperty(
            value = "是否被命中",
            position = 8
    )
    private boolean isSelected;
    @ApiModelProperty(
            value = "模型名称",
            position = 9
    )
    private String modelName;
    @ApiModelProperty(
            value = "模型数字化编码",
            position = 10
    )
    private String modelDataCode;

}

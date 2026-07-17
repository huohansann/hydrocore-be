package com.siact.hydrocore.sec.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class InfoListQueryVo {
    @ApiModelProperty(
            value = "数字化编码",
            position = 10
    )
    private Set<String> dataCodes;
    @ApiModelProperty(
            value = "属性分组（定义：def，基础：base，静态：static，动态：dynamic,端点信息：rtnode）",
            required = true,
            position = 20
    )
    private List<String> propGroups = null;
    @ApiModelProperty(
            value = "属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性",
            position = 30
    )
    private List<String> propTypes = null;
    @ApiModelProperty(
            value = "是否加载备选项",
            position = 40
    )
    private boolean loadValSelectFlag;
    @ApiModelProperty(
            value = "是否加载字典表label",
            position = 50
    )
    private boolean loadLabelFlag;
    @ApiModelProperty(
            value = "属性模型Code（属性模型短码）",
            position = 4
    )
    private List<String> propModelCodes;
    @ApiModelProperty(
            value = "页码，默认1",
            position = 5
    )
    private Integer pageNumber = 1;
    @ApiModelProperty(
            value = "每页条数，默认10",
            position = 6
    )
    private Integer pageSize = 10;

}

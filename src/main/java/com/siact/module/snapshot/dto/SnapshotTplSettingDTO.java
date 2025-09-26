package com.siact.module.snapshot.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SnapshotTplSettingDTO {

    @ApiModelProperty("模板code")
    private String code;

    @ApiModelProperty("模板类型:TEMP(窑炉温度),GAS(天然气)")
    private String type;

    @ApiModelProperty("模板名称")
    private String name;

    @ApiModelProperty("前端是否展示")// ps:该字段不影响后端集成数据
    private Boolean show;

    @ApiModelProperty("查询点位")
    private List<SnapshotTplSettingDetailDTO> queryDataCode;
}

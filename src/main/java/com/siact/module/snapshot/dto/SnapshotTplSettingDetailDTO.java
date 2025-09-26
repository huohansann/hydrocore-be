package com.siact.module.snapshot.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SnapshotTplSettingDetailDTO {

    @ApiModelProperty("查询点位名称")
    private String name;

    @ApiModelProperty("数字孪生长码")
    private String dataCode;

    @ApiModelProperty("数字孪生属性长码")
    private String propCode;
}

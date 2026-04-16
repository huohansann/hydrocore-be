package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel(description = "设备点位导入结果")
public class DeviceImportResult {

    @ApiModelProperty("新增成功条数")
    private int successCount;

    @ApiModelProperty("覆盖更新条数")
    private int updateCount;

    @ApiModelProperty("失败条数")
    private int failCount;

    @ApiModelProperty("失败详情")
    private List<ImportError> errors = new ArrayList<>();

    @Data
    @ApiModel(description = "导入错误详情")
    public static class ImportError {

        @ApiModelProperty("行号")
        private int row;

        @ApiModelProperty("点位名称")
        private String pointName;

        @ApiModelProperty("点位ID")
        private String itemId;

        @ApiModelProperty("失败原因")
        private String reason;
    }
}

package com.siact.module.snapshot.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 快照视图对象
 *
 * @author Roo
 * @date 2025-09-22
 */
@Data
public class SnapshotChartVO {

    @ApiModelProperty("图表数据")
    private List<SnapshotChartDetailVO> chartData;

    private Map<String, List<BigDecimal>> rangeData;

    @ApiModelProperty(value = "x轴数据")
    private List<String> xAxis;

}
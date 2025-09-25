package com.siact.module.base.vo;

import lombok.Data;

import java.util.List;

@Data
public class HistoryConfigChartQueryVO {
    List<String> dataCodeList;
    String startTime;
    String endTime;
    Integer ts;
    String tsUnit;
    String formatVal;
}

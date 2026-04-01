package com.siact.module.control.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasForecastSeriesVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dataCode;         // 孪生编码
    private String name;             // 名称
    private GasForecastDataVO data;  // 点位数据
}

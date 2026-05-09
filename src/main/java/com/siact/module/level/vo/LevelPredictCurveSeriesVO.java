package com.siact.module.level.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
public class LevelPredictCurveSeriesVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dataCode;
    private String name;
    private Map<String, LevelCurveDataVO> data;
}

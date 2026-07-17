package com.siact.hydrocore.module.base.vo;

import lombok.Data;

import java.util.List;

/**
 * {
 * 	"dataCodes": [
 * 		"PGY02014_SZL01001_STZL001001_U00000000_EQ000000000000_MPRCL2001"
 * 	],
 * 	"startTime": "2025-03-06 00:00:00",
 * 	"endTime": "2025-03-06 23:00:00",
 * 	"ts": 1,
 * 	"tsUnit": "H",
 * 	"calcType": "FIRST",
 * 	"fill": true,
 * 	"simple": true
 * }
 */

@Data
public class ProjectPropVO {

    public ProjectPropVO() {
        this.fill = true;
        this.simple = true;
        this.ts = 1;
    }

    private String projectCode;

    private String systemCode;

    private List<String> dataCodes;

    private String startTime;

    private String endTime;

    private int ts;

    private String tsUnit;

    private String calcType;

    private boolean fill;

    private boolean simple;
}

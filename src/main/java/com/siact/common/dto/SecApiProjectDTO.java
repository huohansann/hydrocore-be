package com.siact.common.dto;

import lombok.Data;

@Data
public class SecApiProjectDTO {
    private String id;
    private String modelDataCode;
    private String parentInsId;
    private String parentInsDataCode;
    private String modelId;
    private String dataCode;
    private String serial;
    private String proCode;
    private String proType;
    private String proName;
    private String proAlias;
    private String descr;
    private String createTime;
}

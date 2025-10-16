package com.siact.module.process.dto;

import lombok.Data;

@Data
public class ProcessOneHotEncoderDTO {
    private String type;
    private Integer algorithmProcessCode;
    private int[] oneHotArr;
}
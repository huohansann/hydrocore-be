package com.siact.module.device.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceMappingQueryDTO {
    private String pointName;
    private String itemId;
    private String propCode;
    private String propName;
    private String deviceCode;
    private String deviceName;
}

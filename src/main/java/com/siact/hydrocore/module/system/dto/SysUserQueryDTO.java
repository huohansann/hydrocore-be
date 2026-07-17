package com.siact.hydrocore.module.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysUserQueryDTO {
    private String account;
    private String username;
    private Long orgId;
    private Integer status;
}

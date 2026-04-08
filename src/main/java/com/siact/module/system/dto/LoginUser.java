package com.siact.module.system.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String account;
    private String username;
}

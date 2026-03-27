package com.siact.module.control.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class GasForecastDataValueVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Object[]> value;
}

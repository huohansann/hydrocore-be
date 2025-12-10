package com.siact.module.algorithm.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-08 16:11
 * @className : IntelliTplSettingDTO
 * @description : 智能算法输出点位设置类
 */
@Data
public class IntelliTplSettingDTO {
    private String type;
    private String name;
    private List<IntelliTplSettingDetailDTO> dataCodeList;
}

package com.siact.module.base.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("费价模型的量费及单价返回信息")
public class FeeModelValFeePriceRtnDTO extends FeeModelBaseRtnDTO{
    @ApiModelProperty("当前费价模型单价")
    private BigDecimal price;

    public FeeModelValFeePriceRtnDTO() {
    }

    public FeeModelValFeePriceRtnDTO(BigDecimal val, BigDecimal fee, BigDecimal price) {
        super(val, fee);
        this.price = price;
    }
}

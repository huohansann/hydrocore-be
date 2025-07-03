package com.siact.module.predicted.enums;

import lombok.Getter;

@Getter
public enum AlgorithmCallStatusEnum {

    RUNNING(0, "回调中"),
    SUCCESS(1, "已完成回调");

    private final int status;
    private final String desc;

    AlgorithmCallStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

}

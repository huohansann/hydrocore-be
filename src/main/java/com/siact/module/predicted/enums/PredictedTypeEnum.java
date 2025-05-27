package com.siact.module.predicted.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum PredictedTypeEnum {

    SINGLE_T20(1, "T20"),
    SINGLE_T40(1, "T40"),
    SINGLE_T60(1, "T60"),
    SINGLE_T80(1, "T80"),
    SINGLE_T27(1, "T27"),
    SINGLE_T54(1, "T54"),
    MULTI(2, "MULTI"),
    ;

    private final Integer type;
    private final String code;

    PredictedTypeEnum(Integer type, String code) {
        this.type = type;
        this.code = code;
    }

    /**
     * 根据code获取type
     *
     * @param typeCode
     * @return
     */
    public static Integer getTypeByCode(String typeCode) {
        for (PredictedTypeEnum typeEnum : PredictedTypeEnum.values()) {
            if (typeEnum.code.equals(typeCode)) {
                return typeEnum.type;
            }
        }
        return null;
    }

    public static Integer singleType() {
        return 1;
    }

    public static Integer multiType() {
        return 2;
    }

    public static List<Integer> multiTypeList() {
        return Arrays.stream(PredictedTypeEnum.values())
                .map(PredictedTypeEnum::getType)
                .distinct().collect(Collectors.toList());
    }

    public static List<PredictedTypeEnum> getSingleTypeList() {
        return Arrays.stream(PredictedTypeEnum.values())
                .filter(typeEnum -> typeEnum.type.equals(singleType()))
                .collect(Collectors.toList());
    }
}

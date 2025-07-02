package com.siact.module.predicted.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum PredictedTypeEnum {

    SINGLE_T20(1, "single_step", "T20", "T20", 20),
    SINGLE_T40(1, "single_step", "T40", "T40", 40),
    SINGLE_T60(1, "single_step", "T60", "T60", 60),
    SINGLE_T80(1, "single_step", "T80", "T80", 80),
    SINGLE_T27(1, "single_step", "T27", "T27", 27),
    SINGLE_T54(1, "single_step", "T54", "T54", 54),
    MULTI(2, "multiple_step", "MULTI", "多步预测", 80),
    ;

    private final Integer type;
    private final String algorithmCode;
    private final String code;
    private final String name;
    private final Integer step;

    public static Map<String, PredictedTypeEnum> typeCodeEnumMap =
            Arrays.stream(PredictedTypeEnum.values()).collect(Collectors.toMap(PredictedTypeEnum::getCode, o -> o));


    PredictedTypeEnum(Integer type,String algorithmCode, String code,String name, Integer step) {
        this.type = type;
        this.algorithmCode = algorithmCode;
        this.code = code;
        this.name = name;
        this.step = step;
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

    /**
     * 根据code获取enum
     *
     * @param typeCode
     * @return
     */
    public static PredictedTypeEnum getEnumByCode(String typeCode) {
        for (PredictedTypeEnum typeEnum : PredictedTypeEnum.values()) {
            if (typeEnum.code.equals(typeCode)) {
                return typeEnum;
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

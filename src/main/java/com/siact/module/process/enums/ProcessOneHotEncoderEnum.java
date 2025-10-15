package com.siact.module.process.enums;

import lombok.Getter;

@Getter
public enum ProcessOneHotEncoderEnum {

    TYPE_Ⅲ24X("Ⅲ24X", 0, new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅳ24X("Ⅳ24X", 1, new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅲ20X("Ⅲ20X", 2, new int[]{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅳ20X("Ⅳ20X", 3, new int[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅲ27X("Ⅲ27X", 4, new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅳ27X("Ⅳ27X", 5, new int[]{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅲ24Y("Ⅲ24Y", 6, new int[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}),
    TYPE_Ⅳ24Y("Ⅳ24Y", 7, new int[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}),
    TYPE_Ⅲ20Y("Ⅲ20Y", 8, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}),
    TYPE_Ⅳ20Y("Ⅳ20Y", 9, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}),
    TYPE_Ⅲ27Y("Ⅲ27Y", 10,new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}),
    TYPE_Ⅳ27Y("Ⅳ27Y", 11,new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}),
    TYPE_Ⅲ21X("Ⅲ21X", 12, new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅳ21X("Ⅳ21X", 13, new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
    TYPE_Ⅲ21Y("Ⅲ21Y", 14, new int[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}),
    TYPE_Ⅳ21Y("Ⅳ21Y", 15, new int[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0});

    final String type;
    // 算法对应的工况code
    final int algorithmProcessCode;
    final int[] oneHotArr;

    ProcessOneHotEncoderEnum(String type, int algorithmProcessCode, int[] oneHotArr) {
        this.type = type;
        this.algorithmProcessCode = algorithmProcessCode;
        this.oneHotArr = oneHotArr;
    }

    /**
     * 将类型字符串转换为 one-hot 编码数组
     *
     * @param type 类型名称（如 "Type1"）
     * @return 对应的 one-hot 编码数组
     * @throws IllegalArgumentException 如果类型不存在
     */
    public static ProcessOneHotEncoderEnum getEnumByType(String type) {
        for (ProcessOneHotEncoderEnum encoderEnum : ProcessOneHotEncoderEnum.values()) {
            if (encoderEnum.type.equals(type)) {
                return encoderEnum;
            }
        }

        return null;
    }


    /**
     * 将类型字符串转换为 one-hot 编码数组
     *
     * @param type 类型名称（如 "Type1"）
     * @return 对应的 one-hot 编码数组
     * @throws IllegalArgumentException 如果类型不存在
     */
    public static int[] getOneHotByType(String type) {
        for (ProcessOneHotEncoderEnum encoderEnum : ProcessOneHotEncoderEnum.values()) {
            if (encoderEnum.type.equals(type)) {
                return encoderEnum.oneHotArr;
            }
        }

        return null;
    }

    /**
     * 将类型字符串转换为 operatingCode 编码
     *
     * @param type 类型名称
     * @return 对应的 operatingCode 编码
     * @throws IllegalArgumentException 如果类型不存在
     */
    public static Integer getAlgorithmCodeByType(String type) {
        for (ProcessOneHotEncoderEnum encoderEnum : ProcessOneHotEncoderEnum.values()) {
            if (encoderEnum.type.equals(type)) {
                return encoderEnum.algorithmProcessCode;
            }
        }

        return null;
    }

}
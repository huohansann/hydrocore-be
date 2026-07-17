package com.siact.hydrocore.common.constant;

//import sun.misc.BASE64Encoder;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 常量工具
 * @author dell
 */
public interface ConstantUtil {


    //BASE64Encoder BASE = new BASE64Encoder();
    Base64.Encoder BASE = Base64.getEncoder();

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(ConstantTime.DATE_FORMAT);

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME);


    DateTimeFormatter DATE_TIME_FORMATTER_00_00 = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_00_00);

    DateTimeFormatter DATE_TIME_FORMATTER_MM_00 = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_MM_00);


    DateTimeFormatter DATE_TIME_MONTH = DateTimeFormatter.ofPattern(ConstantTime.MONTH_FORMAT);


    DateTimeFormatter DATE_TIME_YEAR = DateTimeFormatter.ofPattern(ConstantTime.YEAR_FORMAT);


    DateTimeFormatter DATE_TIME_DD_HH_MM = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_DD_HH_MM);


    DateTimeFormatter DATE_TIME_HH_MM = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_HH_MM);

    SimpleDateFormat SDF = new SimpleDateFormat(ConstantTime.DATE_TIME);

}

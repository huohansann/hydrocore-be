package com.siact.common.utils;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author simon
 * @version 1.0
 * @project siact-sec
 * @description JEP公式计算 @[]
 * @date 2023/10/19 17:16:20
 */
public class JepUtils {

    /**
     * 公式计算
     *
     * @param formula    公式
     * @param param2Vals 变量-》值
     * @param scale      精度
     * @param nullCalc   空值是否计算 如果计算 则将空值当0参与运算 如果不计算则当前计算结果为null
     * @param nullVal   如果所有数据都为null，则返回nullVal
     * @return
     */
    private static Object calc(String formula, Map<String, BigDecimal> param2Vals, int scale, boolean nullCalc,
                         BigDecimal nullVal) throws ParseException, EvaluationException {

        //如果数据全为null则返回null
        if (param2Vals.values().stream().filter(Objects::nonNull).collect(Collectors.toList()).size() == 0) {
            return nullVal;
        }
        String fm = formula.replaceAll("@|\\[|\\]", "");
        //如果仅是赋值操作，则不进行计算，提升性能
        if(param2Vals.containsKey(fm)){
            return param2Vals.get(fm);
        }

        //JEP初始化
        Jep jep = new Jep();
        jep.parse(fm);
        //添加变量
        BigDecimal bpv = null;
        //是否计算
        boolean isCalc = true;
        for (String s : param2Vals.keySet()) {
            try {
                BigDecimal v = param2Vals.get(s);
                if (v == null) {
                    if (nullCalc) {
                        //空值置为0参与运算
                        jep.addVariable(s, 0);
                    } else {
                        isCalc = false;
                        break;
                    }
                } else {
                    jep.addVariable(s, param2Vals.get(s));
                }
            } catch (JepException e) {
                e.printStackTrace();
                throw new RuntimeException("JEP添加变量异常");
            }
        }

        //执行计算
        if (isCalc) {
            return jep.evaluate();

        }
        return null;
    }

    public static BigDecimal calcBigDecimal(String formula, Map<String, BigDecimal> param2Vals, int scale, boolean nullCalc,
                               BigDecimal nullVal) throws ParseException, EvaluationException {
        Object result = calc(formula, param2Vals, scale, nullCalc, nullVal);
        return result == null ? null : new BigDecimal(result.toString()).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public static Boolean calcBoolean(String formula, Map<String, BigDecimal> param2Vals, int scale, boolean nullCalc,
                                       BigDecimal nullVal) throws ParseException, EvaluationException {
        Object result = calc(formula, param2Vals, scale, nullCalc, nullVal);
        return result == null ? null : Boolean.valueOf(result.toString());
    }


    public static void main(String[] args) throws ParseException, EvaluationException {
        Map<String, BigDecimal> param2Vals = new HashMap<>();
        param2Vals.put("curVal", new BigDecimal(4));   // 现查
        param2Vals.put("val", new BigDecimal(795)); // 入参
        // 增加 8-15  炉子比较
        String formula = "(curVal + 8 <= val) && (val <= curVal + 15)";
        boolean calc = (Boolean)calc(formula, param2Vals, 2, true, null);

        // 区间比较
        String formula1 = "(lowVal <= val) && (val <= upLow)";
        System.out.println(calc);
    }

}

package com.siact.common.constant;


import java.util.Arrays;
import java.util.List;

/**
 * 基本常量类型
 * @author dell
 */
public interface ConstantBase {

    String SUCCESS = "success";

    String FAIL = "fail";

    String OK = "OK";

    String ORDER = "order";

    String STATE = "state";

    String UTF = "UTF-8";

    String X_AXIS = "xAxis";

    String VALUE_STR = "value";

    String TYPE = "type";

    String NAME = "name";

    String TAG = "tag";

    String UP = "up";

    String DOWN = "down";

    String FLAT = "flat";

    String NORMAL = "normal";

    String HTTP = "http://";

    String KEY = "key";

    String PARENT = "parent";

    String CODE = "code";

    String ITEM_VALUE = "itemValue";

    /**
     * 用户标识
     */
    String USER_KEY = "userId";
    /**
     *  升序
     */
    String ASC = "asc";
    /**
     * 降序
     */
    String DESC = "desc";
    /**
     * 删除字段名
     */
    String DEL_FLAG = "del_flag";
    /**
     * 当前页码
     */
    String PAGE = "page";
    /**
     * 每页显示记录数
     */
    String LIMIT = "limit";
    /**
     * 排序字段
     */
    String ORDER_FIELD = "orderField";
    /**
     * 开始时间后缀
     */
    String STIME_SUFFIX = " 00:00:00";
    /**
     * 结束时间后缀
     */
    String ETIME_SUFFIX = " 23:59:59";

    String TIME_00 = "00:00";
    String TIME_24 = "24:00";
    /**
     * 计算类型：均值
     */
    String AVG = "AVG";
    /**
     * 计算类型：最大
     */
    String MAX = "MAX";
    /**
     * 计算类型：最小
     */
    String MIN = "MIN";
    /**
     * 计算类型：最新值
     */
    String LAST = "LAST";
    /**
     * 计算类型：最早值
     */
    String FIRST = "FIRST";
    /**
     * 计算类型：累加
     */
    String TOTAL = "TOTAL";
    /**
     * 计算类型：增量
     */
    String INC = "INC";
    /**
     * 计算类型：累加
     */
    String SUM = "SUM";
    /**
     * 计算类型：统计
     */
    String COUNT = "COUNT";
    List<String> CALCULATE_TYPE = Arrays.asList(AVG, MAX, MIN, LAST, FIRST, TOTAL, INC, SUM, COUNT);

    /**
     * 创建时间字段
     */
    String CREATE_TIME = "create_time";

    /**
     * 优化步骤
     */
    String optimizationTask = "optimizationTask";


    /**
     * 优化引擎操作步骤
     */
    String optimizationEngine = "optimizationEngine";

    /**
     * 系统拓扑
     */
    String optimizationTopology = "optimizationTopology";

    /**
     * 约束参数设置
     */
    String optimizationConstraint = "optimizationConstraint";

    /**
     * 优化目标设置
     */
    String optimizationTarget = "optimizationTarget";


    /**
     * 模型
     */
    String model = "model";

    /**
     * 约束编码
     */
    String constraintCode = "constraint";


    /**
     * 匹配数字孪生设备实例表达式
     */
    String matchRegex = "(?<=\")P[a-zA-Z0-9]{7}_S[a-zA-Z0-9]{7}_ST[a-zA-Z0-9]{8}_U[a-zA-Z0-9]{8}_(?!EQ000000000000)EQ[a-zA-Z0-9]{12}_MP0000000(?=\")";


    /**
     * 基础属性编码
     */
    String baseProp = "baseProperties";


    /**
     * 静态属性编码
     */
    String staticProp = "staticProperties";

    /**
     * 机理模型
     */
    String mechanismModel = "mechanismModel";

    /**
     * 预测任务
     */
    String forestTask = "forestTask";


    /**
     * 费价模型
     */
    String feeModel = "feeModel";

    /**
     * 能效模型
     */
    String energyModel = "energyModel";

    /**
     * 计划任务
     */
    String planTask = "planTask";


    /**
     * 电负荷告警信息
     */
    String POWER_ALARM = "电负荷预测值和实际值存在偏差，偏差率为:";

    /**
     * 热负荷告警信息
     */
    String HEAT_ALARM = "热负荷预测值和实际值存在偏差，偏差率为:";

    /**
     * 冷负荷告警信息
     */
    String COLD_ALARM = "冷负荷预测值和实际值存在偏差，偏差率为:";


    /**
     * 偏差编码
     */
    String deviation = "deviation";


    /**
     * 默认为空消息
     */
     String DEFAULT_NULL_MESSAGE = "暂无数据";
    /**
     * 默认成功消息
     */
     String DEFAULT_SUCCESS_MESSAGE = "操作成功";
    /**
     * 默认失败消息
     */
     String DEFAULT_FAILURE_MESSAGE = "操作失败";

    /**
     * 日
     */
    String D = "D";

    /**
     * 月
     */
    String M = "M";


    /**
     * 年
     */
    String Y = "Y";


    /**
     * 负载率X轴
     */
    List<String> loadRateXAxisData = Arrays.asList("空载", "轻载", "正常", "重载", "过载");


    /**
     * 天然气（属性短码）
     */
    String GAS = "ZTR";
    /**
     * 水（属性短码）
     */
    String WATER = "ZYS";
    /**
     * 电（属性短码）
     */
    String ELECTRICITY = "ZYD";

    String BASE_VAL = "基准数据";

    String ACTIVE_POWER = "ActivePower";
    /**
     * 区间费价
     */
    String TIME_OF_USE_TARIFF = "electricity-1";

    String ENERGY_SAVING_MODULE = "energySavingModule";

    String RATE_STR = "_rate";

    String STR_00 = ":00";

    String STR_24_00 = " 23:59:59";

    String STR_00_00 = " 00:00:00";

    String STR_01 = "-01";

    String EMPTY_STR = " ";
}

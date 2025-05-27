package com.siact.sec.utils;

import com.alibaba.fastjson2.JSONObject;
import com.siact.common.constant.ConstantNum;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.utils.UnitConversion;
import com.siact.module.base.dto.BasicDataDTO;
import com.siact.module.base.dto.ColumnChartDTO;
import com.siact.sec.dto.CommonChartDataDto;
import com.siact.sec.dto.CommonChartParamsDto;
import com.siact.sec.dto.CommonChartResultDto;
import com.siact.sec.dto.IntervalDataDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * @author dell
 */
@Slf4j
public class CommonHandle {
    /**
     * 获取柱状图折线图量数据 以时间间隔为分组
     * 同时具备有code为分组的“总和”和“平均值”
     *
     * @param vo          前端入参 具体请看前端入参实体定义
     * @param dataDtoList 符合条件的全量数据集合
     * @return
     */
    public static CommonChartResultDto getCommonChartResultDto(CommonChartParamsDto vo, List<IntervalDataDto> dataDtoList) {
        printLog("getCommonChartResultDto", "获取柱状图折线图费用数据", vo, dataDtoList);

        //创建返回数据封装对象
        CommonChartResultDto resultDto = new CommonChartResultDto();
        List<CommonChartDataDto> chartDataDtoList = new ArrayList<>();
        List<String> nameList = vo.getNames();
        List<String> unitList = vo.getUnits();
        List<Boolean> showTableList = vo.getShowTables();

        // 匹配坐标轴
        List<String> timeList = IntervalTimeUtil.queryIntervalTimeList(vo.getStartTime(), vo.getEndTime(), vo.getTsUnit(), vo.getTs(), vo.getFormatVal());

        // 对数据时间进行格式化
        List<IntervalDataDto> intervalDataDtoList = DataServerUtils.intervalDataFormat(dataDtoList, vo.getFormatVal());

        List<String> dataCodeList = vo.getDataCodes();
        // 数据按编码分组
        Map<String, List<IntervalDataDto>> dataMap = intervalDataDtoList.stream().collect(Collectors.groupingBy(IntervalDataDto::getDataCode));
        int length = dataCodeList.size();
        // 循环遍历匹配数据
        for (int i = 0; i < dataCodeList.size(); i++) {
            String dataCode = dataCodeList.get(i);
            CommonChartDataDto dataDto = new CommonChartDataDto();
            dataDto.setDataCode(dataCode);

            if (dataMap.containsKey(dataCode)) {
                List<IntervalDataDto> itemDataList = dataMap.get(dataCode);
                Map<String, List<IntervalDataDto>> itemDataMap = itemDataList.stream().collect(Collectors.groupingBy(IntervalDataDto::getTime));
                List<Object[]> resDataList = new ArrayList<>();
                // 返回数据封装
                timeList.forEach(t -> {
                    if (itemDataMap.containsKey(t)) {
//                        double val = itemDataMap.get(t).stream().filter(d -> ObjectUtils.isNotEmpty(d.getItemVal())).mapToDouble(d -> d.getItemVal().doubleValue()).sum();

                        List<IntervalDataDto> intervalDataDtos = itemDataMap.get(t);
                        BigDecimal val = intervalDataDtos.get(0).getItemVal();
                        resDataList.add(new Object[]{t, null == val ? val : UnitConversion.doublePreString(val.doubleValue(), ConstantNum.NUMBER_TWO)});
                    } else {
                        resDataList.add(new Object[]{t, ConstantSymbol.SHORT_LINE});
                    }
                });
                dataDto.setData(resDataList);
                defaultValHandle(dataDto, length, nameList, unitList, showTableList, i);
                // 总和、平均值计算
                OptionalDouble average = itemDataList.stream().filter(d -> ObjectUtils.isNotEmpty(d.getItemVal())).mapToDouble(d -> d.getItemVal().doubleValue()).average();
                double avgVal = average.isPresent() ? average.getAsDouble() : 0d;
                double totalVal = itemDataList.stream().filter(d -> ObjectUtils.isNotEmpty(d.getItemVal())).mapToDouble(d -> d.getItemVal().doubleValue()).sum();
                dataDto.setAveValue(BigDecimal.valueOf(avgVal).setScale(ConstantNum.NUMBER_TWO, RoundingMode.HALF_UP));
                dataDto.setTotalValue(BigDecimal.valueOf(totalVal).setScale(ConstantNum.NUMBER_TWO, RoundingMode.HALF_UP));

            } else {
                defaultValHandle(dataDto, length, nameList, unitList, showTableList, i, timeList);
            }
            chartDataDtoList.add(dataDto);
        }

        resultDto.setList(chartDataDtoList);
        resultDto.setXAxisData(timeList);
        return resultDto;
    }

    /**
     * 生成柱状图/折线图数据集合（按时间间隔分组）
     *
     * @param vo          图表参数对象，包含：
     *                    - startTime/endTime 时间范围
     *                    - tsUnit 时间单位（时/日/月）
     *                    - formatVal 时间格式要求
     *                    - dataCodes 需要处理的数据编码列表
     *                    - names/units/showTables 数据项配置参数
     * @param dataDtoList 原始数据集合（需包含所有可能的时间段数据）
     * @return ColumnChartDTO 结构包含：
     * - data: 按数据编码分组的数据集合，每个元素包含时间点对应的值
     * - xAxis: 完整的X轴时间标签列表
     */
    public static ColumnChartDTO getColumnChartDTO(CommonChartParamsDto vo, List<IntervalDataDto> dataDtoList) {
        printLog("getCommonChartResultDto", "获取柱状图折线图费用数据", vo, dataDtoList);

        // 初始化返回对象及基础数据结构
        ColumnChartDTO resultDto = new ColumnChartDTO();
        List<BasicDataDTO> chartDataDtoList = new ArrayList<>();
        List<String> nameList = vo.getNames(); // 数据项名称
        List<String> unitList = vo.getUnits(); // 数据项单位

        // 生成完整的X轴时间刻度列表（包含所有可能的时间点）
        List<String> timeList = IntervalTimeUtil.queryIntervalTimeList(vo.getStartTime(), vo.getEndTime(), vo.getTsUnit(), vo.getTs(), vo.getFormatVal());

        // 标准化原始数据：统一时间字段格式
        List<IntervalDataDto> intervalDataDtoList = DataServerUtils.intervalDataFormat(dataDtoList, vo.getFormatVal());

        // 按数据编码建立数据索引（dataCode -> 对应的数据集）
        List<String> dataCodeList = vo.getDataCodes();
        Map<String, List<IntervalDataDto>> dataMap = intervalDataDtoList.stream().collect(Collectors.groupingBy(IntervalDataDto::getDataCode));

        // 遍历所有需要处理的数据编码
        for (int i = 0; i < dataCodeList.size(); i++) {
            String dataCode = dataCodeList.get(i);
            BasicDataDTO dataDto = new BasicDataDTO();
            dataDto.setDataCode(dataCode);

            if (dataMap.containsKey(dataCode)) {
                // 存在数据时的处理流程
                List<IntervalDataDto> itemDataList = dataMap.get(dataCode);
                // 建立时间点->数据的映射关系
                Map<String, List<IntervalDataDto>> itemDataMap = itemDataList.stream().collect(Collectors.groupingBy(IntervalDataDto::getTime));

                // 按时间序列填充数据（存在数据时取最新值，不存在时填充短横线）
                List<Object[]> resDataList = new ArrayList<>();
                timeList.forEach(t -> {
                    if (itemDataMap.containsKey(t)) {
                        BigDecimal val = itemDataMap.get(t).get(0).getItemVal();
                        String formattedVal = (val != null) ? UnitConversion.doublePreString(val.doubleValue(), ConstantNum.NUMBER_TWO) : null;
                        resDataList.add(new Object[]{t, formattedVal});
                    } else {
                        resDataList.add(new Object[]{t, ConstantSymbol.SHORT_LINE});
                    }
                });
                // 给基础数据赋值
                defaultValHandle(dataDto, nameList, unitList, itemDataList.get(0), resDataList, i);
            } else {
                // 无数据时的默认处理（填充空值）
                defaultValHandle(dataDto, nameList, unitList, timeList, i);
            }
            chartDataDtoList.add(dataDto);
        }

        // 构建最终返回结构
        resultDto.setData(chartDataDtoList);
        resultDto.setXAxis(timeList);
        return resultDto;
    }

    /**
     * 生成柱状图/折线图数据集合（按时间间隔分组）
     *
     * @param vo          图表参数对象，包含：
     *                    - startTime/endTime 时间范围
     *                    - tsUnit 时间单位（时/日/月）
     *                    - formatVal 时间格式要求
     *                    - dataCodes 需要处理的数据编码列表
     *                    - names/units/showTables 数据项配置参数
     * @param dataDtoList 原始数据集合（需包含所有可能的时间段数据）
     * @return ColumnChartDTO 结构包含：
     * - data: 按数据编码分组的数据集合，每个元素包含时间点对应的值
     * - xAxis: 完整的X轴时间标签列表
     */
    public static ColumnChartDTO buildColumnChartDTO(CommonChartParamsDto vo, List<IntervalDataDto> dataDtoList) {
        printLog("getCommonChartResultDto", "获取柱状图折线图费用数据", vo, dataDtoList);

        // 初始化返回对象及基础数据结构
        ColumnChartDTO resultDto = new ColumnChartDTO();
        List<BasicDataDTO> chartDataDtoList = new ArrayList<>();
        List<String> nameList = vo.getNames(); // 数据项名称
        List<String> unitList = vo.getUnits(); // 数据项单位

        // 生成完整的X轴时间刻度列表（包含所有可能的时间点）
       List<String> timeList = IntervalTimeUtil.getIntervalTimeList(vo.getStartTime(), vo.getEndTime(), vo.getTsUnit(), vo.getTs(), vo.getFormatVal());

        // 标准化原始数据：统一时间字段格式
        List<IntervalDataDto> intervalDataDtoList = DataServerUtils.intervalDataFormat(dataDtoList, vo.getFormatVal());

        // 按数据编码建立数据索引（dataCode -> 对应的数据集）
        List<String> dataCodeList = vo.getDataCodes();
        Map<String, List<IntervalDataDto>> dataMap = intervalDataDtoList.stream().collect(Collectors.groupingBy(IntervalDataDto::getDataCode));

        // 遍历所有需要处理的数据编码
        for (int i = 0; i < dataCodeList.size(); i++) {
            String dataCode = dataCodeList.get(i);
            BasicDataDTO dataDto = new BasicDataDTO();
            dataDto.setDataCode(dataCode);

            if (dataMap.containsKey(dataCode)) {
                // 存在数据时的处理流程
                List<IntervalDataDto> itemDataList = dataMap.get(dataCode);
                // 建立时间点->数据的映射关系
                Map<String, List<IntervalDataDto>> itemDataMap = itemDataList.stream().collect(Collectors.groupingBy(IntervalDataDto::getTime));

                // 按时间序列填充数据（存在数据时取最新值，不存在时填充短横线）
                List<Object[]> resDataList = new ArrayList<>();
                timeList.forEach(t -> {
                    if (itemDataMap.containsKey(t)) {
                        BigDecimal val = itemDataMap.get(t).get(0).getItemVal();
                        String formattedVal = (val != null) ? UnitConversion.doublePreString(val.doubleValue(), ConstantNum.NUMBER_TWO) : null;
                        resDataList.add(new Object[]{t, formattedVal});
                    } else {
                        resDataList.add(new Object[]{t, ConstantSymbol.SHORT_LINE});
                    }
                });
                // 给基础数据赋值
                defaultValHandle(dataDto, nameList, unitList, itemDataList.get(0), resDataList, i);
            } else {
                // 无数据时的默认处理（填充空值）
                defaultValHandle(dataDto, nameList, unitList, timeList, i);
            }
            chartDataDtoList.add(dataDto);
        }

        // 构建最终返回结构
        resultDto.setData(chartDataDtoList);
        resultDto.setXAxis(timeList);
        return resultDto;
    }

    private static void defaultValHandle(CommonChartDataDto dto, int length, List<String> names, List<String> units, List<Boolean> showTables, int index) {
        if (CollectionUtils.isNotEmpty(names)) {
            if (names.size() == length) {
                dto.setName(names.get(index));
            }
        }

        if (CollectionUtils.isNotEmpty(units)) {
            if (units.size() == length) {
                dto.setUnit(units.get(index));
            }
        }

        if (CollectionUtils.isNotEmpty(showTables)) {
            if (showTables.size() == length) {
                dto.setShowTable(showTables.get(index));
            }
        }

    }

    /**
     * 获取柱状图折线图量数据 以时间间隔为分组
     *
     * @param dto
     * @param nameList
     * @param unitList
     * @param intervalDataDto
     * @param resDataList
     * @param index
     */
    private static void defaultValHandle(BasicDataDTO dto, List<String> nameList, List<String> unitList, IntervalDataDto intervalDataDto, List<Object[]> resDataList, int index) {
        Boolean flag = intervalDataDto != null;
        dto.setInsDataCode(flag ? intervalDataDto.getInsDataCode() : null);
        // 数据名称:如果nameList不为空，则取nameList中的值，否则取itemData中的值
        dto.setName(nameList != null && nameList.size() > index ? nameList.get(index) : (flag ? intervalDataDto.getDataName() : null)); // 数据名称
        // 数据单位：如果unitList不为空，则取unitList中的值，否则取itemData中的值
        dto.setUnit(unitList != null && unitList.size() > index ? unitList.get(index) : (flag ? intervalDataDto.getUnit() : null)); // 数据单位
        dto.setData(resDataList); // 数据集合
    }

    /**
     * 获取柱状图折线图量数据 以时间间隔为分组
     *
     * @param dto
     * @param length
     * @param names
     * @param units
     * @param showTables
     * @param index
     * @param timeList
     */
    private static void defaultValHandle(CommonChartDataDto dto, int length, List<String> names, List<String> units, List<Boolean> showTables, int index, List<String> timeList) {
        List<Object[]> dataList = new ArrayList<>();
        timeList.forEach(t -> dataList.add(new Object[]{t, ConstantSymbol.SHORT_LINE}));
        dto.setData(dataList);
        defaultValHandle(dto, length, names, units, showTables, index);
    }

    /**
     * 获取柱状图折线图量数据 以时间间隔为分组
     *
     * @param dto
     * @param names
     * @param units
     * @param timeList
     * @param index
     */
    private static void defaultValHandle(BasicDataDTO dto, List<String> names, List<String> units, List<String> timeList, int index) {
        List<Object[]> dataList = new ArrayList<>();
        timeList.forEach(t -> dataList.add(new Object[]{t, ConstantSymbol.SHORT_LINE}));
        dto.setData(dataList);
        defaultValHandle(dto, names, units, null, dataList, index);
    }

    /**
     * 根据不同的数据类型打印不同的日志，通用日志打印代码的抽去
     *
     * @param methodName    方法名称
     * @param methodExplain 方法释义
     * @param params        为可变数组，可以将所有的入参传入其中
     */
    public static void printLog(String methodName, String methodExplain, Object... params) {
        log.info("------>方法名:{}，方法释义:{}", methodName, methodExplain);
        if (!Objects.isNull(params) && params.length > 0) {
            int i = 1;
            for (Object obj : params) {
                if (Objects.isNull(obj)) {
                    log.info("参数位置：{}、参数内容为空", i);
                } else if (obj instanceof String) {//字符串直接打印
                    log.info("参数位置：{}、参数内容：{}", i, obj);
                } else if (obj instanceof Integer || obj instanceof BigDecimal || obj instanceof Double || obj instanceof Float) { //整型参数打印
                    log.info("参数位置：{}、参数内容：{}", i, obj);
                } else if (obj instanceof Collection) {//集合打印
                    log.info("参数位置：{}、参数内容：{}", i, ((Collection<?>) obj).size());
                } else {//映射对象及其他未捕获的类型打印
                    try {
                        log.info("参数位置：{}、参数内容：{}", i, JSONObject.toJSONString(obj));
                    } catch (Exception e) {
                        log.info("参数位置：{}、参数内容：{}", i, obj);
                    }
                }
                i++;
            }
        }
    }


}

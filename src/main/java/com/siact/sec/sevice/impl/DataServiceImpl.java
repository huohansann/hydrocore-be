package com.siact.sec.sevice.impl;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.api.common.api.vo.common.R;
import com.siact.api.common.api.vo.prop.*;
import com.siact.api.feign.api.ins.PropService;
import com.siact.common.constant.ConstantNum;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.constant.ConstantUtil;
import com.siact.common.exception.ActiveException;
import com.siact.common.exception.CommonEnum;
import com.siact.common.exception.CustomException;
import com.siact.common.redis.RedisService;
import com.siact.common.utils.ExcelUtils;
import com.siact.common.utils.TimeUtil;
import com.siact.ins.server.common.vo.prop.PropJHValVo;
import com.siact.sec.dto.CommonChartDataDto;
import com.siact.sec.dto.CommonChartParamsDto;
import com.siact.sec.dto.CommonChartResultDto;
import com.siact.sec.dto.CumulativeDataDTO;
import com.siact.sec.dto.EqDypropInsDTO;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalNoteValParamsDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.dto.PropInsDTO;
import com.siact.sec.sevice.DataService;
import com.siact.sec.sevice.SecInsService;
import com.siact.sec.utils.CommonHandle;
import com.siact.sec.utils.ConvertUtils;
import com.siact.sec.vo.CommonChartParamsVo;
import com.siact.sec.vo.CumulativeDataVO;
import com.siact.sec.vo.ExportCommonChartParamsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class DataServiceImpl implements DataService {

    @Resource
    private PropService propService;

    @Autowired
    private SecInsService secInsService;

    @Autowired
    private RedisService redisService;

    @Value("${node.history.timeout}")
    private Long nodeHistoryTimeOut;

    /**
     * 节点历史的redis-key
     */
    public final static String REDISKEY_NODEHISTORY = "nodeHistory";


    /**
     * 查询柱状图、折线图等图表数据(量)
     *
     * @param vo
     * @return
     */
    @Override
    public CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo) {
        log.info("查询柱状图、折线图等图表数据(量), params:{}", com.alibaba.fastjson2.JSON.toJSONString(vo));
        // 是否进行短码替换
        if (CollectionUtils.isNotEmpty(vo.getPropModelCodes())) {
            List<PropInsDTO> propInsDtoList = queryPropCodeByInsCodeAndShortCode(String.join(ConstantSymbol.COMMA, vo.getDataCodes()), String.join(ConstantSymbol.COMMA, vo.getPropModelCodes()));
            vo.setDataCodes(propInsDtoList.stream().map(PropInsDTO::getPropCode).collect(Collectors.toList()));
        }
        // 查询数据
        List<IntervalDataDto> dataDtoList = queryIntervalVal(ConvertUtils.sourceToTarget(vo, IntervalValParamsDto.class));
        // 封装结果返回
        CommonChartParamsDto commonChartParamsDto = ConvertUtils.sourceToTarget(vo, CommonChartParamsDto.class);
        return CommonHandle.getCommonChartResultDto(commonChartParamsDto, dataDtoList);
    }

    /**
     * 查询某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dataCodes 数字孪生编码codes
     * @param startTime 开始时间 yyyy-MM-dd hh:mm:ss
     * @param endTime   结束时间 yyyy-MM-dd hh:mm:ss
     * @param calcType  计算类型 AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     * @return {dataCode:value}
     */
    @Override
    public JSONObject queryBetweenVal(String dataCodes, String startTime, String endTime, String calcType) {
        log.info("查询某个时间段的增量, dataCodes:{}, startTime:{}, endTime:{}", dataCodes, startTime, endTime);
        try {
            if (StringUtils.isEmpty(dataCodes) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
                log.error("参数校验不通过, dataCode:{}, startTime:{}, endTime:{}", dataCodes, startTime, endTime);
                throw new ActiveException(CommonEnum.REQUEST_PARAM_ABONRMAL);
            }

            Set<String> dataCodeSet = new HashSet<>(Arrays.asList(dataCodes.split(ConstantSymbol.COMMA)));
            PropValQuerySVo vo = new PropValQuerySVo();
            vo.setDataCodes(dataCodeSet);
            vo.setStartTime(startTime);
            vo.setEndTime(endTime);
            vo.setCalcType(calcType);

            log.info("查询数字孪生两点之前的值，dataCodes:{}, startTime:{}, endTime:{}, calcType:{}", dataCodes, startTime, endTime, calcType);
            //请求数字孪生并获取数据
            R<List<PropValFMResultVo>> calcData = propService.historyFMCalc(vo);
            List<PropValFMResultVo> dataList = analysisSiactSecData(calcData);

            JSONObject resJson = new JSONObject();
            // 解析数据
            dataList.forEach(itemData -> {
                String dataCode = itemData.getDataCode();
                // 获取并添加值
                getAndAddPropValue(resJson, itemData, dataCode);
            });

            return resJson;
        } catch (ActiveException e) {
            log.error("请求数字孪生查询某个时间段的量发生异常", e);
            return null;
        }
    }

    /**
     * 查询某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     * AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     * @param parms
     * @return
     */
    @Override
    public JSONObject queryBetweenVal(IntervalValParamsDto parms) {
        log.info("查询某个时间段的增量,{}", JSONObject.toJSONString(parms));
        try {
            PropValQuerySVo vo = ConvertUtils.sourceToTarget(parms, PropValQuerySVo.class);
            vo.setDataCodes(new HashSet<>(parms.getDataCodes()));
            //请求数字孪生并获取数据
            R<List<PropValFMResultVo>> calcData = propService.historyFMCalc(vo);
            List<PropValFMResultVo> dataList = analysisSiactSecData(calcData);
            JSONObject resJson = new JSONObject();
            // 解析数据
            dataList.forEach(itemData -> {
                String dataCode = itemData.getDataCode();
                // 获取并添加值
                getAndAddPropValue(resJson, itemData, dataCode);
            });

            return resJson;
        } catch (ActiveException e) {
            log.error("请求数字孪生查询某个时间段的量发生异常", e);
            return null;
        }
    }

    /**
     * 查询等时间间隔的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dto
     * @return
     */
    @Override
    public List<IntervalDataDto> queryIntervalVal(IntervalValParamsDto dto) {
        List<IntervalDataDto> dataDtoList = new ArrayList<>();
        log.info("查询等时间间隔的量,request params:{}", JSONObject.toJSONString(dto));

        try {
            PropValQuerySVo vo = ConvertUtils.sourceToTarget(dto, PropValQuerySVo.class);
            vo.setDataCodes(new HashSet<>(dto.getDataCodes()));
            vo.setFill(true);

            log.info("查询等时间间隔的量, 请求数字孪生参数:{}", JSONObject.toJSONString(vo));
            R<List<PropValFMResultVo>> propData = propService.historyFMCalc(vo);
            List<PropValFMResultVo> propDataDataList = analysisSiactSecData(propData);

            propDataDataList.forEach(itemData -> {
                String dataCode = itemData.getDataCode();
                analysisRequestData(dataDtoList, itemData, dataCode);
            });
        } catch (ActiveException e) {
            log.error("请求数字孪生查询节点下属性等时间间隔的量发生异常", e);
        }

        return dataDtoList;
    }


    @Override
    public JSONObject queryRealValue(String dataCodes) {

        JSONObject resJson = new JSONObject();

        if (StringUtils.isEmpty(dataCodes)) {
            log.error("查询实时值参数为空");
            return null;
        }

        Set<String> dataCodeSet = new HashSet<>(Arrays.asList(dataCodes.split(ConstantSymbol.COMMA)));

        log.info("查询实时值, dataCodes:{}", dataCodes);
        R<List<PropRtValVo>> rt = propService.rtFm(dataCodeSet);

        if (ObjectUtils.isEmpty(rt)) {
            log.error("查询实时值出错，无返回值!");
            return null;
        }

        if (!rt.getCode().equals(ConstantNum.TWO_HUNDRED.toString())) {
            log.error("查询实时值出错，msg:{}", rt.getMsg());
            return null;
        }

        List<PropRtValVo> rtData = rt.getData();
        if (CollectionUtils.isEmpty(rtData)) {
            log.info("查询实时值无数据");
            return null;
        }

        log.info("查询实时值,data:{}", JSONObject.toJSONString(rtData));
        rtData.forEach(data -> {
            resJson.put(data.getDataCode(), data.getPropVal());
        });

        return resJson;
    }

    /**
     * 查询节点下属性某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dataCode       数字孪生编码code
     * @param propModelCodes 属性模型短码,示例值([ "EP1" ])
     * @param startTime      开始时间 yyyy-MM-dd hh:mm:ss
     * @param endTime        结束时间 yyyy-MM-dd hh:mm:ss
     * @param calcType       计算类型 AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     * @return {dataCode:value}
     */
    @Override
    public JSONObject queryNoteBetweenVal(String dataCode, String propModelCodes, String startTime, String endTime, String calcType) {
        log.info("查询节点下属性某个时间段的量, dataCodes:{}, propModelCodes:{}, startTime:{}, endTime:{}", dataCode, propModelCodes, startTime, endTime);
        try {
            JSONObject resJson = new JSONObject();
            if (StringUtils.isEmpty(dataCode) || StringUtils.isEmpty(propModelCodes) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
                log.error("参数校验不通过, dataCode:{}, propModelCodes:{}, startTime:{}, endTime:{}", dataCode, propModelCodes, startTime, endTime);
                throw new ActiveException(CommonEnum.REQUEST_PARAM_ABONRMAL);
            }

            //组装Vo请求参数对象
            NodePropValQueryVo vo = assemblyVoObject(dataCode, propModelCodes, startTime, endTime, calcType);

            //请求数字孪生获取数据
            R<List<PropValFMResultVo>> calcData = propService.nodeHistory(vo);
            List<PropValFMResultVo> dataList = analysisSiactSecData(calcData);

            // 解析数据
            dataList.forEach(itemData -> {
                String code = itemData.getModelCode();
                getAndAddPropValue(resJson, itemData, code);
            });

            return resJson;
        } catch (ActiveException e) {
            log.error("请求数字孪生查询节点下属性某个时间段的量发生异常,{}", e);
            return null;
        }
    }

    /**
     * 查询节点下属性等时间间隔的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
     *
     * @param dto
     * @return
     */
    @Override
    public List<IntervalDataDto> queryNoteIntervalVal(IntervalNoteValParamsDto dto) {
        log.info("查询节点下属性等时间间隔的量,request params:{}", JSONObject.toJSONString(dto));
        List<IntervalDataDto> dataDtoList = new ArrayList<>();
        try {
            NodePropValQueryVo vo = ConvertUtils.sourceToTarget(dto, NodePropValQueryVo.class);
            vo.setFill(true);
            log.info("查询节点下属性等时间间隔的量, 请求数字孪生参数:{}", JSONObject.toJSONString(vo));
            //对hash结构的field过长进行MD5唯一标识
            String mdsObjStr = MD5Utils.md5Hex(JSONObject.toJSONString(dto), "UTF-8");
            log.info("redis的唯一标识field为{}", mdsObjStr);
            //先从缓存里面去 缓存里面没有再去进行接口调用
            Object obj = redisService.getCacheMapValue(REDISKEY_NODEHISTORY, mdsObjStr);
            if (Objects.isNull(obj)) {
                log.info("nodeHistory 缓存中没有需要通过接口获取!!!");
                R<List<PropValFMResultVo>> propData = propService.nodeHistory(vo);
                List<PropValFMResultVo> propDataDataList = analysisSiactSecData(propData);
                List<IntervalDataDto> dataDtoListCopy = new ArrayList<>();
                //解析数字孪生返回值
                propDataDataList.forEach(itemData -> {
                    String modelCode = itemData.getModelCode();
                    analysisRequestData(dataDtoListCopy, itemData, modelCode);
                });
                dataDtoList = dataDtoListCopy;
                //将该数据保存在Redis缓存中
                if (CollectionUtils.isNotEmpty(dataDtoList)) {
                    redisService.setCacheMapValue(REDISKEY_NODEHISTORY, mdsObjStr, dataDtoList, nodeHistoryTimeOut);
                }
            } else {
                dataDtoList = JSONObject.parseArray(JSON.toJSONString(obj), IntervalDataDto.class);
            }
        } catch (ActiveException e) {
            log.error("请求数字孪生查询节点下属性等时间间隔的量发生异常{}", e);
        }
        return dataDtoList;
    }

    /**
     * 导出(量)查询柱状图、折线图等图表数据
     *
     * @param response
     * @param vo
     */
    @Override
    public void exportIntervalInstantData(HttpServletResponse response, ExportCommonChartParamsVO vo) {

        String fileName = vo.getFileName();
        CommonChartResultDto commonChartResultDto = queryCommonChartData(vo);
        if (null == commonChartResultDto) {
            log.error(fileName + "未查询到数据");
            return;
        }
        List<CommonChartDataDto> list = commonChartResultDto.getList();
        Map<String, List<Object[]>> dataMap = list.stream().collect(Collectors.toMap(CommonChartDataDto::getDataCode, CommonChartDataDto::getData));


        //封装导出excel头部信息
        List<ExcelExportEntity> excelExportEntityList = new ArrayList<>();
        ExcelExportEntity firstEntity = new ExcelExportEntity("时间", "time", 25);
        excelExportEntityList.add(firstEntity);

        String[] propNameArr = vo.getPropNames().split(",");
        List<String> dataCodes = vo.getDataCodes();
        for (int i = 0; i < dataCodes.size(); i++) {
            String dataCode = dataCodes.get(i);
            excelExportEntityList.add(new ExcelExportEntity(propNameArr[i], dataCode, 15));
        }

        //获取数据
        List<JSONObject> dataList = new ArrayList<>();

        List<String> xList = commonChartResultDto.getXAxisData();
        for (int i = 0; i < xList.size(); i++) {
            String time = xList.get(i);
            JSONObject itemJson = new JSONObject(true);
            itemJson.put("time", time);
            for (String propCode : dataCodes) {
                Object[] dataObj = dataMap.get(propCode).get(i);
                if (StringUtils.equals(time, String.valueOf(dataObj[0]))) {
                    itemJson.put(propCode, dataObj[1]);
                } else {
                    itemJson.put(propCode, null);
                }
            }

            dataList.add(itemJson);
        }

        ExcelUtils.exportExcel(excelExportEntityList, fileName, dataList, response);
    }

    @Override
    public List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo) {
        List<CumulativeDataDTO> resultList = new ArrayList<>();

        //获取累计值
        List<String> codes = vo.getDataCodes();
        String startTime = vo.getStartTime();
        String endTime = vo.getEndTime();
        JSONObject dataJson = queryBetweenVal(String.join(",", codes), startTime, endTime, "INC");

        for (String code : codes) {
            resultList.add(new CumulativeDataDTO(code, dataJson.getBigDecimal(code)));
        }

        DateTimeFormatter df = TimeUtil.df;
        BigDecimal zero = BigDecimal.ZERO;

        //计算累计同比
        if (vo.isYoy()) {
            String yoyStartTime = LocalDateTime.parse(startTime, df).minusYears(1).format(df);
            String yoyEndTime = LocalDateTime.parse(endTime, df).minusYears(1).format(df);
            JSONObject yoyDataJson = queryBetweenVal(String.join(",", codes), yoyStartTime, yoyEndTime, "INC");

            if (null != yoyDataJson) {
                calYoyData(resultList, zero, yoyDataJson);
            }
        }

        //计算累计环比
        if (vo.isQoq()) {
            String timeType = vo.getTimeType();
            String qoqStartTime = startTime;
            String qoqEndTime = endTime;
            if (StringUtils.equalsIgnoreCase("d", timeType)) {
                qoqStartTime = LocalDateTime.parse(startTime, df).minusDays(1).format(df);
                qoqEndTime = LocalDateTime.parse(endTime, df).minusDays(1).format(df);
            } else if (StringUtils.equalsIgnoreCase("m", timeType)) {
                qoqStartTime = LocalDateTime.parse(startTime, df).minusMonths(1).format(df);
                qoqEndTime = LocalDateTime.parse(endTime, df).minusMonths(1).format(df);
            } else if (StringUtils.equalsIgnoreCase("y", timeType)) {
                qoqStartTime = LocalDateTime.parse(startTime, df).minusDays(1).format(df);
                qoqEndTime = LocalDateTime.parse(endTime, df).minusDays(1).format(df);
            }

            JSONObject qoqDataJson = queryBetweenVal(String.join(",", codes), qoqStartTime, qoqEndTime, "INC");
            if (null != qoqDataJson) {
                calQoqData(resultList, zero, qoqDataJson);
            }
        }

        return resultList;
    }

    @Override
    public List<IntervalDataDto> queryForecastIntervalVal(NodePropFutureValQueryVo vo) {
        List<IntervalDataDto> dataDtoList = new ArrayList<>();
        log.info("查询等时间间隔的量,request params:{}", JSONObject.toJSONString(vo));

        try {
            log.info("查询等时间间隔的量, 请求数字孪生参数:{}", JSONObject.toJSONString(vo));
            R<List<PropValFMResultVo>> propData = propService.nodeFuture(vo);
            List<PropValFMResultVo> propDataDataList = analysisSiactSecData(propData);

            propDataDataList.forEach(itemData -> {
                String dataCode = itemData.getDataCode();
                analysisRequestData(dataDtoList, itemData, dataCode);
            });
        } catch (ActiveException e) {
            log.error("请求数字孪生查询节点下属性等时间间隔的量发生异常", e);
        }

        return dataDtoList;
    }

    private void calQoqData(List<CumulativeDataDTO> resultList, BigDecimal zero, JSONObject qoqDataJson) {
        for (CumulativeDataDTO cumulativeDataDTO : resultList) {
            String code = cumulativeDataDTO.getCode();
            BigDecimal value = cumulativeDataDTO.getValue();
            BigDecimal qoqVal = qoqDataJson.getBigDecimal(code);
            if (value != null && qoqVal != null && qoqVal.compareTo(zero) != 0) {
                BigDecimal qoqRatio = value.subtract(qoqVal).divide(qoqVal, 4, BigDecimal.ROUND_HALF_UP);
                if (qoqRatio.compareTo(zero) > 0) {
                    cumulativeDataDTO.setQoqTrend("up");
                } else if (qoqRatio.compareTo(zero) < 0) {
                    cumulativeDataDTO.setQoqTrend("down");
                } else {
                    cumulativeDataDTO.setQoqTrend("unchg");
                }

                cumulativeDataDTO.setQoq(qoqRatio.abs());
            }
        }
    }

    private void calYoyData(List<CumulativeDataDTO> resultList, BigDecimal zero, JSONObject yoyDataJson) {
        for (CumulativeDataDTO cumulativeDataDTO : resultList) {
            String code = cumulativeDataDTO.getCode();
            BigDecimal value = cumulativeDataDTO.getValue();
            BigDecimal yoyVal = yoyDataJson.getBigDecimal(code);
            if (value != null && yoyVal != null && yoyVal.compareTo(zero) != 0) {
                BigDecimal yoyRatio = value.subtract(yoyVal).divide(yoyVal, 4, BigDecimal.ROUND_HALF_UP);

                if (yoyRatio.compareTo(zero) > 0) {
                    cumulativeDataDTO.setYoyTrend("up");
                } else if (yoyRatio.compareTo(zero) < 0) {
                    cumulativeDataDTO.setYoyTrend("down");
                } else {
                    cumulativeDataDTO.setYoyTrend("unchg");
                }
                cumulativeDataDTO.setYoy(yoyRatio.abs());
            }
        }
    }


    /**
     * 分析请求数据并构建IntervalDataDto列表
     *
     * @param dataDtoList 用于存储处理结果的IntervalDataDto集合（入参/出参）
     * @param itemData    包含时序属性值的源数据对象
     * @param dataCode    需要设置到结果对象中的数据编码
     */
    private void analysisRequestData(List<IntervalDataDto> dataDtoList, PropValFMResultVo itemData, String dataCode) {
        // 处理时序属性值列表
        List<PropValTimeVo> valTimeVoList = itemData.getValTimes();
        String insDataCode = itemData.getInsDataCode();
        if (CollectionUtils.isNotEmpty(valTimeVoList)) {
            valTimeVoList.forEach(valTimeVo -> {
                // 提取时间点和对应属性值列表
                Date dateTime = valTimeVo.getDateTime();
                List<PropJHValVo> propValVoList = valTimeVo.getPropValVos();

                // 处理每个时间点的属性值（仅处理第一个有效值）
                if (CollectionUtils.isNotEmpty(propValVoList)) {
                    PropJHValVo propJHValVo = propValVoList.get(ConstantNum.ZERO_INT);

                    // 构建数据对象并填充字段
                    IntervalDataDto dataDto = new IntervalDataDto();
                    dataDto.setInsDataCode(insDataCode);                  // 设置实例数据编码
                    dataDto.setDataCode(dataCode);                        // 设置业务数据编码
                    dataDto.setDataName(propJHValVo.getPropName());       // 设置属性名称
                    dataDto.setUnit(propJHValVo.getUnit());               // 设置计量单位
                    dataDto.setTime(ConstantUtil.SDF.format(dateTime));   // 格式化时间
                    // 处理空值并转换数值类型
                    dataDto.setItemVal(StringUtils.isBlank(propJHValVo.getPropVal()) ? null : BigDecimal.valueOf(Double.parseDouble(propJHValVo.getPropVal())));
                    dataDtoList.add(dataDto);
                }
            });
        }
    }


    private List<PropValFMResultVo> analysisSiactSecData(R<List<PropValFMResultVo>> calcData) {
        if (ObjectUtils.isEmpty(calcData)) {
            log.error("查询数字孪生节点下属性两点之间的值出错，无返回值!");
            throw new ActiveException(CommonEnum.REQUEST_FAIL);
        }

        // 返回值非200
        if (!calcData.getCode().equals(ConstantNum.TWO_HUNDRED.toString())) {
            log.error("查询数字孪生节点下属性两点之间的值出错，msg:{}", calcData.getMsg());
            throw new ActiveException(CommonEnum.REQUEST_STAUTS_ABONRMAL);
        }

        // 返回结果为空
        List<PropValFMResultVo> dataList = calcData.getData();
        if (CollectionUtils.isEmpty(dataList)) {
            log.info("查询数字孪生节点下属性两点之间的值无数据！");
            throw new ActiveException(CommonEnum.REQUEST_DATA_BLANK);
        }
        return dataList;
    }

    private NodePropValQueryVo assemblyVoObject(String dataCode, String propModelCodes, String startTime, String endTime, String calcType) {
        NodePropValQueryVo vo = new NodePropValQueryVo();
        vo.setDataCode(dataCode);
        vo.setPropModelCodes(Arrays.asList(propModelCodes.split(",")));
        vo.setStartTime(startTime);
        vo.setEndTime(endTime);
        vo.setCalcType(calcType);
        return vo;
    }

    private void getAndAddPropValue(JSONObject resJson, PropValFMResultVo itemData, String code) {
        // 添加默认值
        resJson.put(code, null);

        List<PropValTimeVo> valTimesList = itemData.getValTimes();
        if (CollectionUtils.isNotEmpty(valTimesList)) {
            PropValTimeVo propValTimeVo = valTimesList.get(ConstantNum.ZERO_INT);
            List<PropJHValVo> propValVosList = propValTimeVo.getPropValVos();
            if (CollectionUtils.isNotEmpty(propValVosList)) {
                PropJHValVo propJHValVo = propValVosList.get(ConstantNum.ZERO_INT);
                String propVal = propJHValVo.getPropVal();
                resJson.put(code, null == propVal ? null : new BigDecimal(propVal).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }
    }

    public List<PropInsDTO> queryPropCodeByInsCodeAndShortCode(String dataCodes, String shortCodes) {
        InfoListQueryVo vo = new InfoListQueryVo();
        List<String> dataCodeList = Arrays.asList(dataCodes.split(","));
        vo.setDataCodes(new HashSet<>(dataCodeList));
        List<String> shortCodeList = Arrays.asList(shortCodes.split(","));
        vo.setPropModelCodes(shortCodeList);

        Map<String, List<EqDypropInsDTO>> map = secInsService.queryInsDynamicProp(vo);

        if (MapUtils.isEmpty(map)) {
            log.error("未查询到属性信息");
            throw new CustomException("未查询到属性信息");
        }

        List<PropInsDTO> resultList = new ArrayList<>();
        for (String dataCode : dataCodeList) {
            List<EqDypropInsDTO> propInsDTOS = map.get(dataCode);
            if (CollectionUtils.isEmpty(propInsDTOS)) {
                continue;
            }

            for (EqDypropInsDTO dyPropInsDTO : propInsDTOS) {
                PropInsDTO propInsDTO = new PropInsDTO();
                propInsDTO.setInsCode(dataCode);
                propInsDTO.setPropName(dyPropInsDTO.getPropName());
                propInsDTO.setPropCode(dyPropInsDTO.getDataCode());
                propInsDTO.setShortCode(dyPropInsDTO.getPropCode());
                propInsDTO.setPropVal(dyPropInsDTO.getPropVal());
                propInsDTO.setUnit(dyPropInsDTO.getUnit());
                resultList.add(propInsDTO);
            }
        }

        return resultList;
    }




}

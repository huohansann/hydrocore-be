package com.siact.hydrocore.tdengine.service;

import com.alibaba.fastjson.JSONObject;
import com.siact.api.common.api.vo.prop.NodePropFutureValQueryVo;
import com.siact.hydrocore.sec.dto.*;
import com.siact.hydrocore.sec.vo.CommonChartParamsVo;
import com.siact.hydrocore.sec.vo.CumulativeDataVO;

import java.util.List;
import java.util.Map;

/**
 * TDengine 直接查询服务接口
 * 替代数字孪生接口，解决限流导致的数据缺失问题
 */
public interface TaosDataService {

    /**
     * 查询柱状图、折线图等图表数据(量)
     *
     * @param vo   查询参数
     * @return 图表数据结果
     */
    CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo);

    /**
     * 查询某个时间段的量
     * calcType: AVG/MAX/MIN/LAST/FIRST/SUM/INC/COUNT
     *
     * @param dataCodes   数字孪生编码codes，逗号分隔
     * @param startTime   开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime     结束时间 yyyy-MM-dd HH:mm:ss
     * @param calcType    计算类型
     * @return {dataCode: value}
     */
    JSONObject queryBetweenVal(String dataCodes, String startTime, String endTime, String calcType);

    /**
     * 查询某个时间段的量（参数对象版本）
     *
     * @param params   查询参数对象
     * @return {dataCode: value}
     */
    JSONObject queryBetweenVal(IntervalValParamsDto params);

    /**
     * 查询等时间间隔的量
     *
     * @param dto   查询参数
     * @return 时间间隔数据列表
     */
    List<IntervalDataDto> queryIntervalVal(IntervalValParamsDto dto);

    /**
     * 查询实时值（最后一包数据）
     *
     * @param dataCodes   数字孪生编码codes，逗号分隔
     * @return {dataCode: value}
     */
    JSONObject queryRealValue(String dataCodes);

    /**
     * 查询节点下属性某个时间段的量
     *
     * @param dataCode        数字孪生编码code
     * @param propModelCodes  属性模型短码（对应 itemid TAG）
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param calcType        计算类型
     * @return {propModelCode: value}
     */
    JSONObject queryNoteBetweenVal(String dataCode, String propModelCodes,
                                    String startTime, String endTime, String calcType);

    /**
     * 查询节点下属性等时间间隔的量
     *
     * @param dto   查询参数
     * @return 时间间隔数据列表
     */
    List<IntervalDataDto> queryNoteIntervalVal(IntervalNoteValParamsDto dto);

    /**
     * 查询累计值
     *
     * @param vo   查询参数
     * @return 累计数据列表
     */
    List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo);

    /**
     * Compatibility path for the external data API. HydroCore baseline does not expose prediction as a default business capability.
     * TDengine returns an empty list for this path.
     *
     * @param params   查询参数
     * @return 空列表
     */
    List<IntervalDataDto> queryForecastIntervalVal(NodePropFutureValQueryVo params);

    /**
     * 查询原始时序数据（无聚合）
     *
     * @param dataCodes   数字孪生编码列表
     * @param startTime   开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime     结束时间 yyyy-MM-dd HH:mm:ss
     * @return 每行包含 ts、datacode、itemvalue
     */
    List<Map<String, Object>> queryRawData(List<String> dataCodes, String startTime, String endTime);
}

package com.siact.module.pressure.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.common.constant.ConstantSymbol;
import com.siact.module.pressure.dto.PressureDto;
import com.siact.module.pressure.dto.PressureQuery;
import com.siact.module.pressure.entity.PressureControlConfigEntity;
import com.siact.module.pressure.mapper.PressureControlConfigMapper;
import com.siact.module.pressure.service.PressureService;
import com.siact.module.pressure.vo.PressureHistoryVO;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.tdengine.service.TaosDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.Resource;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: HouBo
 * @Date: 2026/5/8 9:34
 * @Description: 窑压控制服务实现
 */
@Slf4j
@Service
public class PressureServiceImpl implements PressureService {

    @Resource
    private PressureControlConfigMapper pressureControlConfigMapper;

    // 仅供开发使用
    @Value("${forecast.test-mode.enabled:false}")
    private boolean testModeEnabled;

    @Resource
    private TaosDataService taosDataService;

    @Override
    public JSONObject getModelData(PressureDto pressureDto) {
        // 开启测试模式
        if (testModeEnabled) {
            return getMockModelData(pressureDto);
        }

        String joinDataCode = String.join(ConstantSymbol.COMMA, pressureDto.getDataCodes());
        JSONObject jsonObject = taosDataService.queryRealValue(joinDataCode);

        JSONObject resultObj = new JSONObject();
        for (String dataCode : pressureDto.getDataCodes()) {
            BigDecimal dataVal = null;
            if (jsonObject != null && jsonObject.containsKey(dataCode)) {
                dataVal = jsonObject.getBigDecimal(dataCode);
            }
            resultObj.put(dataCode, dataVal);
        }

        return resultObj;

    }

    @Override
    public List<PressureControlConfigEntity> selectAll() {
        return pressureControlConfigMapper.selectList(null);
    }

    @Override
    public int updateAll(List<PressureControlConfigEntity> list) {
        Date now = new Date();
        for (PressureControlConfigEntity entity : list) {
            entity.setUpdateTime(now);
            pressureControlConfigMapper.updateById(entity);
        }
        return list.size();
    }

    @Override
    public PressureHistoryVO queryHistory(PressureQuery query) {
        if (testModeEnabled) {
            return getMockHistoryData(query);
        }

        List<String> dataCodes = new ArrayList<>();
        dataCodes.add(query.getDataCode());

        IntervalValParamsDto params = new IntervalValParamsDto();
        params.setDataCodes(dataCodes);
        params.setStartTime(query.getStartTime());
        params.setEndTime(query.getEndTime());
        params.setTs(query.getTs());
        params.setTsUnit(query.getTsUnit());
        params.setCalcType(query.getCalcType());

        List<IntervalDataDto> dataList = taosDataService.queryIntervalVal(params);

        List<String> xdata = new ArrayList<>();
        List<String> ydata = new ArrayList<>();
        for (IntervalDataDto d : dataList) {
            xdata.add(d.getTime());
            ydata.add(d.getItemVal() != null ? d.getItemVal().toPlainString() : null);
        }

        return PressureHistoryVO.builder()
                .dataCode(query.getDataCode())
                .xdata(xdata)
                .ydata(ydata)
                .build();
    }

    /**
     * @Author: HouBo
     * @Date: 2026/5/8 9:48
     * @Description: 开发环境自用, 从json搞点数据看看
     */
    private JSONObject getMockModelData(PressureDto dto) {
        log.info("测试模式开启，从 JSON 文件读取 mock 数据");
        try {
            ClassPathResource resource = new ClassPathResource("testJson/monitoring/getModelData.json");
            String jsonContent = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            JSONObject mockData = JSONObject.parseObject(jsonContent);

            JSONObject resultObj = new JSONObject();
            for (String dataCode : dto.getDataCodes()) {
                resultObj.put(dataCode, mockData.get(dataCode));
            }
            return resultObj;
        } catch (Exception e) {
            log.error("读取测试 JSON 文件失败，降级为正常模式", e);
//            return doQueryRealValue(dto);
            return null;
        }
    }

    /**
     * @Author: HouBo
     * @Date: 2026/5/8 14:00
     * @Description: 开发环境自用, mock窑压历史数据
     */
    private PressureHistoryVO getMockHistoryData(PressureQuery query) {
        log.info("测试模式开启，生成 mock 窑压历史数据");
        List<String> xdata = new ArrayList<>();
        List<String> ydata = new ArrayList<>();

        LocalDateTime start = LocalDateTime.parse(query.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse(query.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int interval = query.getTs() != null ? query.getTs() : 5;

        double baseValue = 6.0;
        for (LocalDateTime t = start; !t.isAfter(end); t = t.plusMinutes(interval)) {
            xdata.add(t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            double value = baseValue + Math.sin(t.getMinute() * 0.1) * 0.5;
            ydata.add(String.format("%.2f", value));
        }

        return PressureHistoryVO.builder()
                .dataCode(query.getDataCode())
                .xdata(xdata)
                .ydata(ydata)
                .build();
    }
}

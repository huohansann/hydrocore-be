package com.siact.hydrocore.sec.sevice.impl;

import com.siact.api.common.api.vo.common.R;
import com.siact.api.common.api.vo.eq.EqListQueryVo;
import com.siact.api.common.api.vo.eq.EqVO;
import com.siact.api.feign.api.ins.EqInsService;
import com.siact.hydrocore.common.constant.ConstantNum;
import com.siact.hydrocore.common.exception.ActiveException;
import com.siact.hydrocore.common.exception.CommonEnum;
import com.siact.hydrocore.sec.convertor.ClassConvertor2DTO;
import com.siact.hydrocore.sec.dto.EqDypropInsDTO;
import com.siact.hydrocore.sec.dto.EqStpropInsDTO;
import com.siact.hydrocore.sec.sevice.DevService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class DevServiceImpl implements DevService {

    @Autowired
    private EqInsService eqInsService;


    /**
     * 批量查询设备对应属性短码的动态属性信息
     *
     * dataCodes 数字孪生编码codes
     * propGroups 属性分组（基础：base，静态：static，动态：dynamic,端点信息：rtnode）
     * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
     * propModelCodes 属性模型Code（属性模型短码）
     * @return {List<EqVO>}
     */
    @Override
    public Map<String, List<EqDypropInsDTO>> queryDeviceDynamicProp(EqListQueryVo vo) {
        log.info("批量查询设备对应属性短码的静态属性信息, dataCodes:{}, propTypes:{}, propModelCodes:{}", vo.getDataCodes(), vo.getPropTypes(), vo.getPropModelCodes());
        try {
            List<EqVO> eqPropList = getDevPropInfo(vo, "dynamic");
            return eqPropList.stream().collect(Collectors.toMap(eq -> eq.getDefinitionProperty().getDataCode(), eq -> ClassConvertor2DTO.INSTANCE.eqDypropInsVos2DTOs(eq.getDynamicProperties())));
        } catch (ActiveException e) {
            log.error("批量查询设备对应属性短码的静态属性信息发生异常", e);
            return new HashMap<>();
        }
    }
    /**
     * 批量查询设备对应属性短码的静态属性信息
     *
     * dataCodes 数字孪生编码codes
     * propGroups 属性分组（基础：base，静态：static，动态：dynamic,端点信息：rtnode）
     * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
     * propModelCodes 属性模型Code（属性模型短码）
     * @return {List<EqVO>}
     */
    @Override
    public Map<String, List<EqStpropInsDTO>> queryDeviceStaticProp(EqListQueryVo vo) {
        log.info("批量查询设备对应属性短码的静态属性信息, dataCodes:{}, propTypes:{}, propModelCodes:{}", vo.getDataCodes(), vo.getPropTypes(), vo.getPropModelCodes());
        try {
            List<EqVO> eqPropList = getDevPropInfo(vo, "static");
            return eqPropList.stream().collect(Collectors.toMap(eq -> eq.getDefinitionProperty().getDataCode(), eq -> ClassConvertor2DTO.INSTANCE.eqStpropInsVos2DTOs(eq.getStaticProperties())));
        } catch (ActiveException e) {
            log.error("批量查询设备对应属性短码的静态属性信息发生异常", e);
            return new HashMap<>();
        }
    }

    private List<EqVO> getDevPropInfo(EqListQueryVo vo, String propGroup) {
        vo.setPropGroups(Arrays.asList(propGroup));
        R<List<EqVO>> list = eqInsService.list(vo);
        R<List<EqVO>> r = list;
        List<EqVO> eqPropList = analysisSiactSecData(r);
        return eqPropList;
    }

    private List<EqVO> analysisSiactSecData(R<List<EqVO>> r) {
        if (ObjectUtils.isEmpty(r)) {
            log.error("查询数字孪设备批量查询的值出错，无返回值!");
            throw new ActiveException(CommonEnum.REQUEST_FAIL);
        }

        // 返回值非200
        if (!r.getCode().equals(ConstantNum.TWO_HUNDRED.toString())) {
            log.error("查询数字孪设备批量查询的值出错，msg:{}", r.getMsg());
            throw new ActiveException(CommonEnum.REQUEST_STAUTS_ABONRMAL);
        }

        // 返回结果为空
        List<EqVO> dataList = r.getData();
        if (CollectionUtils.isEmpty(dataList)) {
            log.info("查询数字孪设备批量查询的值出错的值无数据！");
            throw new ActiveException(CommonEnum.REQUEST_DATA_BLANK);
        }
        return dataList;
    }
}

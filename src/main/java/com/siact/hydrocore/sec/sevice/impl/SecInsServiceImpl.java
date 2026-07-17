package com.siact.hydrocore.sec.sevice.impl;

import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.api.common.api.vo.common.InsVO;
import com.siact.api.common.api.vo.common.R;
import com.siact.api.common.api.vo.common.TMInsSimpleVo;
import com.siact.api.feign.api.ins.InsService;
import com.siact.hydrocore.common.constant.ConstantNum;
import com.siact.hydrocore.common.exception.ActiveException;
import com.siact.hydrocore.common.exception.CommonEnum;
import com.siact.ins.server.common.vo.common.InsTreeVo;
import com.siact.hydrocore.sec.convertor.ClassConvertor2DTO;
import com.siact.hydrocore.sec.dto.EqDypropInsDTO;
import com.siact.hydrocore.sec.dto.EqStpropInsDTO;
import com.siact.hydrocore.sec.dto.InsTreeDTO;
import com.siact.hydrocore.sec.dto.TMInsSimpleDTO;
import com.siact.hydrocore.sec.sevice.SecInsService;
import com.siact.hydrocore.sec.utils.SiactSecApiFeignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SecInsServiceImpl implements SecInsService {

    @Autowired
    private InsService insService;


    /**
     * 查询实例下使用指定设备模型长码的设备
     *
     * dataCode 数字孪生编码codes
     * modelDataCode 设备模板长码code
     * @return {List<TMInsSimpleDTO>}
     */
    @Override
    public List<TMInsSimpleDTO> queryInsListByModeCode(String dataCode, String modelDataCode) {
        log.info("可实例化列表, dataCode:{}, modelDataCode:{}", dataCode, modelDataCode);
        try {
            R<List<TMInsSimpleVo>> r = insService.insList(dataCode, modelDataCode, true);
            List<TMInsSimpleVo> tmInsSimpleVos = SiactSecApiFeignUtil.list(r);
            return ClassConvertor2DTO.INSTANCE.tMInsSimpleVos2DTOs(tmInsSimpleVos);
        } catch (ActiveException e) {
            log.error("sec instance list query failed, operation=queryInsListByModeCode, dataCode={}, modelDataCode={}", dataCode, modelDataCode, e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询实例下所有设备
     * @param dataCode
     * @return
     */
    @Override
    public List<InsTreeDTO> queryAllDevUnderIns(String dataCode) {
        log.info("查询实例下所有设备, dataCode:{} ", dataCode);
        try {
            R<List<InsTreeVo>> r = insService.list(dataCode, true, false);
            List<InsTreeVo> devInsList = SiactSecApiFeignUtil.obj(r);
            return ClassConvertor2DTO.INSTANCE.insTreeVos2DTOs(devInsList);
        } catch (ActiveException e) {
            log.error("查询实例下所有设备异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询实例树
     * @param dataCode 数字化编码,不传则查询项目实例。
     * @param hasEq 是否需要查询设备,默认false。
     * @param hasSub 是否需要查询孙子节点,默认false。
     * @return
     */
    @Override
    public List<InsTreeDTO> queryTree(String dataCode, boolean hasEq, boolean hasSub) {
        log.info("查询实例树, dataCode:{} ", dataCode);
        try {
            R<List<InsTreeVo>> r = insService.list(dataCode, hasSub, hasEq, false, false, false, false);
            List<InsTreeVo> devInsList = SiactSecApiFeignUtil.obj(r);
            return ClassConvertor2DTO.INSTANCE.insTreeVos2DTOs(devInsList);
        } catch (ActiveException e) {
            log.error("查询实例树异常", e);
            return new ArrayList<>();
        }
    }

    /**
     *
     *
     * dataCodes 数字孪生编码codes
     * propGroups 属性分组（基础：base，静态：static，动态：dynamic,端点信息：rtnode）
     * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
     * propModelCodes 属性模型Code（属性模型短码）
     * @return {List<EqVO>}
     */
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
    public Map<String, List<EqDypropInsDTO>> queryInsDynamicProp(InfoListQueryVo vo) {
        log.info("批量查询设备对应属性短码的静态属性信息, dataCodes:{}, propTypes:{}, propModelCodes:{}", vo.getDataCodes(), vo.getPropTypes(), vo.getPropModelCodes());
        try {
            List<InsVO> insPropList = getInsPropInfo(vo, "dynamic");
            return insPropList.stream().collect(Collectors.toMap(ins -> ins.getDefinitionProperty().getDataCode(), ins -> ClassConvertor2DTO.INSTANCE.insDypropInsVos2DTOs(ins.getDynamicProperties())));
        } catch (ActiveException e) {
            log.error("sec instance property query failed, operation=queryInsDynamicProp, dataCodes={}, propTypes={}, propModelCodes={}", vo.getDataCodes(), vo.getPropTypes(), vo.getPropModelCodes(), e);
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
    public Map<String, List<EqStpropInsDTO>> queryInsStaticProp(InfoListQueryVo vo) {
        log.info("批量查询设备对应属性短码的静态属性信息, dataCodes:{}, propTypes:{}, propModelCodes:{}", vo.getDataCodes(), vo.getPropTypes(), vo.getPropModelCodes());
        try {
            List<InsVO> insPropList = getInsPropInfo(vo, "static");
            return insPropList.stream().collect(Collectors.toMap(ins -> ins.getDefinitionProperty().getDataCode(), ins -> ClassConvertor2DTO.INSTANCE.insStpropInsVos2DTOs(ins.getStaticProperties())));
        } catch (ActiveException e) {
            log.error("sec instance property query failed, operation=queryInsStaticProp, dataCodes={}, propTypes={}, propModelCodes={}", vo.getDataCodes(), vo.getPropTypes(), vo.getPropModelCodes(), e);
            return new HashMap<>();
        }
    }

    private List<InsVO> getInsPropInfo(InfoListQueryVo vo, String propGroup) {
        vo.setPropGroups(Arrays.asList("def", propGroup));
        Integer pageSize = vo.getPageSize();
        vo.setPageSize(null == pageSize ? Integer.MAX_VALUE : pageSize);
        Integer pageNumber = vo.getPageNumber();
        vo.setPageNumber(null == pageNumber || pageNumber <1 ? 1 : pageNumber);
        vo.setLoadLabelFlag(true);
        vo.setLoadValSelectFlag(true);
        R<List<InsVO>> r = insService.batchAll(vo);
        List<InsVO> insPropList = analysisSiactSecData(r);
        return insPropList;
    }

    private List<InsVO> analysisSiactSecData(R<List<InsVO>> r) {
        if (ObjectUtils.isEmpty(r)) {
            log.error("查询数字实例批量查询的值出错，无返回值!");
            throw new ActiveException(CommonEnum.REQUEST_FAIL);
        }

        // 返回值非200
        if (!r.getCode().equals(ConstantNum.TWO_HUNDRED.toString())) {
            log.error("查询数字孪实例批量查询的值出错，msg:{}", r.getMsg());
            throw new ActiveException(CommonEnum.REQUEST_STAUTS_ABONRMAL);
        }

        // 返回结果为空
        List<InsVO> dataList = r.getData();
        if (CollectionUtils.isEmpty(dataList)) {
            log.info("查询数字孪实例批量查询的值出错的值无数据！");
            throw new ActiveException(CommonEnum.REQUEST_DATA_BLANK);
        }
        return dataList;
    }
}

package com.siact.sec.sevice;


import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.sec.dto.EqDypropInsDTO;
import com.siact.sec.dto.EqStpropInsDTO;
import com.siact.sec.dto.InsTreeDTO;
import com.siact.sec.dto.TMInsSimpleDTO;

import java.util.List;
import java.util.Map;

public interface SecInsService {
    /**
     * 查询实例下使用指定设备模型长码的设备
     *
     * dataCode 数字孪生编码codes
     * modelDataCode 设备模板长码code
     * @return {List<TMInsSimpleDTO>}
     */
    List<TMInsSimpleDTO> queryInsListByModeCode(String dataCode, String modelDataCode);

    /**
     * 查询实例下所有设备
     * @param dataCode
     * @return
     */
    List<InsTreeDTO> queryAllDevUnderIns(String dataCode);

    /**
     * 查询实例树
     * @param dataCode 数字化编码,不传则查询项目实例。
     * @param hasEq 是否需要查询设备,默认false。
     * @param hasSub 是否需要查询孙子节点,默认false。
     * @return
     */
    List<InsTreeDTO> queryTree(String dataCode, boolean hasEq, boolean hasSub);

    /**
     * 批量查询设备对应属性短码的动态属性信息
     *
     * dataCodes 数字孪生编码codes
     * propGroups 属性分组（基础：base，静态：static，动态：dynamic,端点信息：rtnode）
     * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
     * propModelCodes 属性模型Code（属性模型短码）
     * @return {Map<String, List<EqDypropInsVO>>}
     */
    Map<String, List<EqDypropInsDTO>> queryInsDynamicProp(InfoListQueryVo vo);

    /**
     * 批量查询设备对应属性短码的静态属性信息
     *
     * dataCodes 数字孪生编码codes
     * propGroups 属性分组（基础：base，静态：static，动态：dynamic,端点信息：rtnode）
     * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
     * propModelCodes 属性模型Code（属性模型短码）
     * @return {Map<String, List<EqDypropInsVO>>}
     */
    Map<String, List<EqStpropInsDTO>> queryInsStaticProp(InfoListQueryVo vo);
}

package com.siact.sec.convertor;

import com.siact.api.common.api.vo.common.DypropInsVO;
import com.siact.api.common.api.vo.common.StpropInsVO;
import com.siact.api.common.api.vo.common.TMInsSimpleVo;
import com.siact.api.common.api.vo.eq.EqDypropInsVO;
import com.siact.api.common.api.vo.eq.EqStpropInsVO;
import com.siact.ins.server.common.vo.common.InsTreeVo;
import com.siact.sec.dto.EqDypropInsDTO;
import com.siact.sec.dto.EqStpropInsDTO;
import com.siact.sec.dto.InsTreeDTO;
import com.siact.sec.dto.TMInsSimpleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ClassConvertor2DTO {
    ClassConvertor2DTO INSTANCE = Mappers.getMapper(ClassConvertor2DTO.class);

    List<TMInsSimpleDTO> tMInsSimpleVos2DTOs (List<TMInsSimpleVo> vos);

    List<InsTreeDTO> insTreeVos2DTOs (List<InsTreeVo> vos);
    List<EqDypropInsDTO> eqDypropInsVos2DTOs (List<EqDypropInsVO> vos);

    List<EqStpropInsDTO> eqStpropInsVos2DTOs (List<EqStpropInsVO> vos);

    List<EqDypropInsDTO> insDypropInsVos2DTOs (List<DypropInsVO> vos);

    List<EqStpropInsDTO> insStpropInsVos2DTOs (List<StpropInsVO> vos);
}

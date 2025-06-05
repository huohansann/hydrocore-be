package com.siact.module.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.exception.CustomException;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.dto.ProcessLogPageDTO;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.enums.DefoamSystemEnum;
import com.siact.module.process.enums.FireCycleEnum;
import com.siact.module.process.enums.ProductLineEnum;
import com.siact.module.process.mapper.ProcessLogMapper;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.vo.ProcessLogVO;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工艺日志Service实现
 */
@Service
public class ProcessLogServiceImpl extends ServiceImpl<ProcessLogMapper, ProcessLogEntity> implements IProcessLogService {

    private LambdaQueryWrapper<ProcessLogEntity> buildQueryWrapper(ProcessLogDTO dto) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<>();
        // 根据起始日期 终止日期 产线数量 换火周期 消泡系统 换机 进行查询检索
        if (ObjectUtils.isNotEmpty(dto.getStartTime())) {
            wrapper.ge(ProcessLogEntity::getStartTime, dto.getStartTime());
        }
        if (ObjectUtils.isNotEmpty(dto.getEndTime() )) {
            wrapper.le(ProcessLogEntity::getEndTime, dto.getEndTime());
        }
        if (ObjectUtils.isNotEmpty(dto.getProductLineNum() )) {
            wrapper.eq(ProcessLogEntity::getProductLineNum, dto.getProductLineNum());
        }
        if (ObjectUtils.isNotEmpty(dto.getFireCycle() )) {
            wrapper.eq(ProcessLogEntity::getFireCycle, dto.getFireCycle());
        }
        if (ObjectUtils.isNotEmpty(dto.getDefoamSystem() )) {
            wrapper.eq(ProcessLogEntity::getDefoamSystem, dto.getDefoamSystem());
        }
        if (ObjectUtils.isNotEmpty(dto.getReplaceMachine() )) {
            wrapper.eq(ProcessLogEntity::getReplaceMachine, dto.getReplaceMachine());
        }

        return wrapper;
    }

    @Override
    public PageVO<ProcessLogEntity> pageQuery(ProcessLogPageDTO queryDTO) {
        Page<ProcessLogEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ProcessLogEntity> wrapper = buildQueryWrapper(queryDTO);
        IPage<ProcessLogEntity> entityPage = page(page, wrapper);
        return PageVO.build(entityPage);
    }

    @Override
    public List<ProcessLogVO> listAll(ProcessLogDTO queryDTO) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = buildQueryWrapper(queryDTO);
        List<ProcessLogEntity> list = this.list(wrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ProcessLogVO getById(Long id) {
        ProcessLogEntity entity = baseMapper.selectById(id);
        return entity == null ? null : toVO(entity);
    }

    @Override
    public boolean add(ProcessLogDTO dto) {
        ProcessLogEntity entity = new ProcessLogEntity();
        // 校验时间区间是否存在重复
        if (checkTimeExist(dto)) {
            throw new CustomException("时间区间已存在");
        }

        BeanUtils.copyProperties(dto, entity);

        // 生成工况编码 逻辑:工况编码=产线数量+换火周期+消泡
        String operatingCode = getOperatingCode(dto);
        entity.setOperatingCode(operatingCode);

        // 生成二进制编码
        String binaryCode = getBinaryCode(dto);
        entity.setBinaryCode(binaryCode);

        return this.save(entity);
    }

    @Override
    public boolean update(ProcessLogDTO dto) {
        ProcessLogEntity entity = new ProcessLogEntity();
        // 校验时间区间是否存在重复
        if (checkTimeExist(dto)) {
            throw new CustomException("时间区间已存在");
        }

        BeanUtils.copyProperties(dto, entity);

        // 生成工况编码 逻辑:工况编码=产线数量+换火周期+消泡
        String operatingCode = getOperatingCode(dto);
        entity.setOperatingCode(operatingCode);

        // 生成二进制编码
        String binaryCode = getBinaryCode(dto);
        entity.setBinaryCode(binaryCode);

        return this.updateById(entity);
    }


    @NotNull
    private static String getBinaryCode(ProcessLogDTO dto) {
        ProductLineEnum productLineEnum = ProductLineEnum.getByCode(dto.getProductLineNum());
        FireCycleEnum fireCycleEnum = FireCycleEnum.getByCode(dto.getFireCycle());
        DefoamSystemEnum defoamSystemEnum = DefoamSystemEnum.getByCode(dto.getDefoamSystem());
        if (ObjectUtils.isEmpty(productLineEnum) || ObjectUtils.isEmpty(fireCycleEnum) || ObjectUtils.isEmpty(defoamSystemEnum)) {
            throw new CustomException("产线数量或换火周期或消泡系统状态错误,无法匹配该类型");
        }

        // 生成二进制编码
        String binaryCode =
                productLineEnum.getBinaryCode()
                        + fireCycleEnum.getBinaryCode()
                        + defoamSystemEnum.getBinaryCode();
        return binaryCode;
    }

    @NotNull
    private static String getOperatingCode(ProcessLogDTO dto) {
        return dto.getProductLineNum() + dto.getFireCycle() + dto.getDefoamSystem();
    }

    /**
     * 校验时间区间是否存在重复
     *
     * @param dto
     */
    private Boolean checkTimeExist(ProcessLogDTO dto) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<>();
        // 如果id不为空 需要判断id 不等于id
        if (ObjectUtils.isNotEmpty(dto.getId())) {
            wrapper.ne(ProcessLogEntity::getId, dto.getId());
        }
        wrapper.and(w -> w.le(ProcessLogEntity::getStartTime, dto.getEndTime()).and(o -> o.ge(ProcessLogEntity::getEndTime, dto.getStartTime())));
//                .or(w -> w.ge(ProcessLogEntity::getStartTime, dto.getEndTime()).and(o -> o.le(ProcessLogEntity::getEndTime, dto.getEndTime())));

//        wrapper.and(w -> w.ge(ProcessLogEntity::getStartTime, dto.getStartTime()).and(o -> o.ge(ProcessLogEntity::getEndTime, dto.getEndTime())).or().le(ProcessLogEntity::getStartTime, dto.getStartTime()).and(o -> o.le(ProcessLogEntity::getEndTime, dto.getEndTime())));

        return baseMapper.selectCount(wrapper) > 0;
    }


    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public ProcessLogVO queryByDate(String queryDate) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(ProcessLogEntity::getStartTime, queryDate);
        wrapper.ge(ProcessLogEntity::getEndTime, queryDate);

        ProcessLogEntity processLogEntity = baseMapper.selectOne(wrapper);
        return processLogEntity == null ? null : toVO(processLogEntity);
    }

    private ProcessLogVO toVO(ProcessLogEntity entity) {
        ProcessLogVO vo = new ProcessLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
} 
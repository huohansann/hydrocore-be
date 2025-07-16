package com.siact.module.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantBase;
import com.siact.common.constant.ConstantTime;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.ConvertUtils;
import com.siact.common.utils.LoginUntil;
import com.siact.common.utils.TimeUtil;
import com.siact.module.permission.dto.UserTokenDTO;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.dto.ProcessLogPageDTO;
import com.siact.module.process.dto.ProcessLogQueryDTO;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.enums.DefoamSystemEnum;
import com.siact.module.process.enums.FireCycleEnum;
import com.siact.module.process.enums.ProductLineEnum;
import com.siact.module.process.enums.ReplaceMachineEnum;
import com.siact.module.process.mapper.ProcessLogMapper;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.utils.ProcessOneHotEncoderEnum;
import com.siact.module.process.vo.ProcessLogVO;
import com.siact.sec.utils.IntervalTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工艺日志Service实现
 */
@Slf4j
@Service
public class ProcessLogServiceImpl extends ServiceImpl<ProcessLogMapper, ProcessLogEntity> implements IProcessLogService {

    private LambdaQueryWrapper<ProcessLogEntity> buildQueryWrapper(ProcessLogQueryDTO dto) {
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

        wrapper.orderByDesc(ProcessLogEntity::getStartTime);

        return wrapper;
    }

    /**
     * 转换返回的时间测试
     * @param record
     */
    private static void formatRtnTime(ProcessLogEntity record) {
        record.setStartTime(IntervalTimeUtil.dateFormat(record.getStartTime(),"yyyy-MM-dd HH点"));
        record.setEndTime(IntervalTimeUtil.dateFormat(record.getEndTime(),"yyyy-MM-dd HH点"));
        record.setOperationDate(IntervalTimeUtil.dateFormat(record.getOperationDate(), "yyyy-MM-dd"));
    }

    @Override
    public PageVO<ProcessLogEntity> pageQuery(ProcessLogPageDTO queryDTO) {
        Page<ProcessLogEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ProcessLogEntity> wrapper = buildQueryWrapper(queryDTO);
        IPage<ProcessLogEntity> entityPage = page(page, wrapper);
        for (ProcessLogEntity record : entityPage.getRecords()) {
            formatRtnTime(record);
        }


        return PageVO.build(entityPage);
    }

    @Override
    public List<ProcessLogVO> listAll(ProcessLogQueryDTO queryDTO) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = buildQueryWrapper(queryDTO);
        List<ProcessLogEntity> list = this.list(wrapper);
        for (ProcessLogEntity entity : list) {
            formatRtnTime(entity);
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ProcessLogVO getById(Long id) {
        ProcessLogEntity entity = baseMapper.selectById(id);
        formatRtnTime(entity);
        return entity == null ? null : toVO(entity);
    }

    @Override
    public boolean add(ProcessLogDTO dto) {
        // 校验时间区间是否存在重复
        if (checkTimeExist(dto)) {
            throw new CustomException("时间区间已存在");
        }

        ProcessLogEntity entity = new ProcessLogEntity();
        BeanUtils.copyProperties(dto, entity);

        // 换机状态 换机的结束时间为开始时间  次日的8:30  (ps:正常的工况不设置endTime)
        if (entity.getReplaceMachine().equals(ReplaceMachineEnum.REPLACED.getValue())) {
            String startTime = IntervalTimeUtil.dateFormat(dto.getStartTime(), ConstantTime.DATE_TIME);
            String endTime = TimeUtil.getCalcTime(startTime, 1, ConstantBase.D);
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
        }

        // 获取当前startTime的上一个时段
        ProcessLogEntity beforeEntity = getBeforeEntityByTime(entity.getStartTime());
        // 如果上一个时段是正常的,补充上一个正常时段的endTime  如果上个时段是换机,则不处理(因为换机有默认结束时间)
        if (beforeEntity.getReplaceMachine().equals(ReplaceMachineEnum.NORMAL.getValue())) {
            beforeEntity.setEndTime(entity.getStartTime());
        }

        ProcessLogEntity nextEntity = getAfterEntityByTime(entity.getStartTime());
        if (ObjectUtils.isNotEmpty(nextEntity)) {
            // 如果存在下一个时段信息  证明是中间插入
            // 需要根据下一个entity补充 当前的endTime (ps:下一个时段 向前提一分钟)
            entity.setEndTime(TimeUtil.getCalcTime(nextEntity.getStartTime(), -1, ConstantBase.MIN));
        }

        // 获取当前操作人员
        UserTokenDTO currentUser = LoginUntil.getCurrentUser();
        if (ObjectUtils.isNotEmpty(currentUser)) {
            String username = currentUser.getUsername();
            entity.setOperator(username);
        } else {
             throw new CustomException("当前登录状态失效!,请重新登录");
        }


        // 生成工况编码 逻辑:工况编码=产线数量+换火周期+消泡
        String operatingCode = getOperatingCode(dto);
        entity.setOperatingCode(operatingCode);

        // 生成二进制编码
        String binaryCode = getOneHotEncoding(dto);
        entity.setBinaryCode(binaryCode);

        return this.save(entity);
    }

    private ProcessLogEntity getBeforeEntityByTime(String startTime) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<ProcessLogEntity>()
                .lt(ProcessLogEntity::getStartTime, startTime)
                .orderByDesc(ProcessLogEntity::getStartTime)
                .last("limit 1");

        return baseMapper.selectOne(wrapper);
    }

    private ProcessLogEntity getAfterEntityByTime(String startTime) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<ProcessLogEntity>()
                .gt(ProcessLogEntity::getStartTime, startTime)
                .orderByAsc(ProcessLogEntity::getStartTime)
                .last("limit 1");

        return baseMapper.selectOne(wrapper);
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
        String binaryCode = getOneHotEncoding(dto);
        entity.setBinaryCode(binaryCode);

        return this.updateById(entity);
    }

    private static String getOneHotEncoding(ProcessLogDTO dto) {
        ProductLineEnum productLineEnum = ProductLineEnum.getByCode(dto.getProductLineNum());
        FireCycleEnum fireCycleEnum = FireCycleEnum.getByCode(dto.getFireCycle());
        DefoamSystemEnum defoamSystemEnum = DefoamSystemEnum.getByCode(dto.getDefoamSystem());
        if (ObjectUtils.isEmpty(productLineEnum) || ObjectUtils.isEmpty(fireCycleEnum) || ObjectUtils.isEmpty(defoamSystemEnum)) {
            throw new CustomException("产线数量或换火周期或消泡系统状态错误,无法匹配该类型");
        }

        // 生成二进制编码
        String type =
                productLineEnum.getCode()
                        + fireCycleEnum.getCode()
                        + defoamSystemEnum.getCode();
        int[] oneHotByType = ProcessOneHotEncoderEnum.getOneHotByType(type);
        return oneHotByType == null ? null : Arrays.stream(oneHotByType).mapToObj(o -> o + "").collect(Collectors.joining());
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

        wrapper.and(w -> w.eq(ProcessLogEntity::getStartTime, dto.getStartTime()).and(o -> o.ge(ProcessLogEntity::getEndTime, dto.getStartTime())));

        return baseMapper.selectCount(wrapper) > 0;
    }


    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public Boolean deleteBatch(List<Long> idList) {
        return removeByIds(idList);
    }

    @Override
    public Map<String,List<ProcessLogVO>> queryByDateRange(String startTime, String endTime) {
        List<ProcessLogEntity> processLogEntity = getByTimeRange(startTime, endTime);

        List<ProcessLogVO> processLogVOList = processLogEntity == null ? null : ConvertUtils.sourceToTarget(processLogEntity, ProcessLogVO.class);
        if (ObjectUtils.isEmpty(processLogVOList)) {
            log.info("查询时间段内无数据");
            return new HashMap<>();
        }
        Map<String, List<ProcessLogVO>> rtnMap = processLogVOList.stream()
                .collect(Collectors.groupingBy(o -> IntervalTimeUtil.dateFormat(o.getStartTime(), ConstantTime.DATE_FORMAT)));
        rtnMap.values().stream().flatMap(List::stream).forEach(record->{
            record.setStartTime(IntervalTimeUtil.dateFormat(record.getStartTime(),"yyyy-MM-dd HH:ss"));
            record.setEndTime(IntervalTimeUtil.dateFormat(record.getEndTime(),"yyyy-MM-dd HH:ss"));
            record.setOperationDate(IntervalTimeUtil.dateFormat(record.getOperationDate(), "yyyy-MM-dd"));
        });
        return rtnMap;
    }

    public List<ProcessLogEntity> getByTimeRange(String startTime, String endTime) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.and(w -> w.ge(ProcessLogEntity::getStartTime, startTime).le(ProcessLogEntity::getEndTime, endTime));
        wrapper.or(o -> o.ge(ProcessLogEntity::getStartTime, startTime).le(ProcessLogEntity::getEndTime, startTime));
        wrapper.or(o -> o.le(ProcessLogEntity::getStartTime, endTime).ge(ProcessLogEntity::getEndTime, endTime));
        wrapper.or(o -> o.ge(ProcessLogEntity::getStartTime, startTime).and(o1 -> o1.isNull(ProcessLogEntity::getEndTime)));

        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<ProcessLogEntity> getByTimeRange(String startTime, String endTime, int replaceMachine) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ProcessLogEntity::getReplaceMachine, replaceMachine);

        wrapper.and(w -> w.ge(ProcessLogEntity::getStartTime, startTime).le(ProcessLogEntity::getEndTime, endTime));
        wrapper.or(o -> o.ge(ProcessLogEntity::getStartTime, startTime).le(ProcessLogEntity::getEndTime, startTime));
        wrapper.or(o -> o.le(ProcessLogEntity::getStartTime, endTime).ge(ProcessLogEntity::getEndTime, endTime));
        wrapper.or(o -> o.ge(ProcessLogEntity::getStartTime, startTime).and(o1 -> o1.isNull(ProcessLogEntity::getEndTime)));

        return baseMapper.selectList(wrapper);
    }

    @Override
    public ProcessLogVO queryByDate(String queryDate) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(ProcessLogEntity::getStartTime, queryDate);
        wrapper.ge(ProcessLogEntity::getEndTime, queryDate);

        ProcessLogEntity processLogEntity = baseMapper.selectOne(wrapper);
        if (ObjectUtils.isEmpty(processLogEntity)) {
            log.info("查询时间段内无数据");
            return null;
        }
        formatRtnTime(processLogEntity);
        return toVO(processLogEntity);
    }

    private ProcessLogVO toVO(ProcessLogEntity entity) {
        ProcessLogVO vo = new ProcessLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
} 
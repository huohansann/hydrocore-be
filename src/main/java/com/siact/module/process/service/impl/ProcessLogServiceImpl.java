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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        record.setStartTime(record.getStartTime() == null ? null : IntervalTimeUtil.dateFormat(record.getStartTime(), "yyyy-MM-dd HH:mm"));
        record.setEndTime(record.getEndTime() == null ? null : IntervalTimeUtil.dateFormat(record.getEndTime(), "yyyy-MM-dd HH:mm"));
        record.setOperationDate(record.getOperationDate() == null ? null : IntervalTimeUtil.dateFormat(record.getOperationDate(), "yyyy-MM-dd"));
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
    @Transactional(rollbackFor = Exception.class)
    public void add(ProcessLogDTO dto) {
        // 校验时间区间是否存在重复
        if (checkTimeExist(dto)) {
            throw new CustomException("时间区间已存在");
        }

        ProcessLogEntity entity = new ProcessLogEntity();
        BeanUtils.copyProperties(dto, entity);

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

        if (entity.getReplaceMachine().equals(ReplaceMachineEnum.REPLACED.getValue())) {
            addReplaceMachineProcessLog(dto, entity);
        } else {
            addNormalProcessLog(entity);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addNormalProcessLog(ProcessLogEntity entity) {
        // 逻辑:新增的是正常工况
        // 需要  补全上一个正常工况的endTime
        // 如果当前新增的工况为插入数据  即后续存在其他工况  那么要补充当前工况的endTime

        // 1:获取当前startTime的上一个时段(从大到小  倒序排列)
        List<ProcessLogEntity> beforeList = getBeforeListByTimeDESC(entity.getStartTime(), 1);
        // 如果上一个时段是正常的,更新上一个正常时段的endTime  如果上个时段是换机,则不处理(因为换机有默认结束时间)
        if (beforeList != null && !beforeList.isEmpty()) {
            // 获取上一个时段信息
            ProcessLogEntity beforeEntity = beforeList.get(0);
            // 补充上一个时段的endTime
            if (beforeEntity.getReplaceMachine().equals(ReplaceMachineEnum.NORMAL.getValue())) {
                beforeEntity.setEndTime(TimeUtil.getCalcTime(entity.getStartTime(), -1, ConstantBase.MIN));
            }
            this.updateById(beforeEntity);
        }

        // 2:获取当前startTime的下一个时段(从小到大  正序排列)
        ProcessLogEntity nextEntity = getAfterEntityByTimeASC(entity.getStartTime());
        if (ObjectUtils.isNotEmpty(nextEntity)) {
            // 如果存在下一个时段信息  证明是中间插入
            // 需要根据下一个entity补充 当前的endTime (ps:下一个时段 向前提一分钟)
            entity.setEndTime(TimeUtil.getCalcTime(nextEntity.getStartTime(), -1, ConstantBase.MIN));
        }
        this.save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addReplaceMachineProcessLog(ProcessLogDTO dto, ProcessLogEntity replaceMachineAddEntity) {
        // 换机状态 换机的结束时间为开始时间+1天  次日的8:30  (ps:正常的工况不设置endTime)
        // 逻辑: 新增换机状态 需要自动追加 换机后的正常状态 且工况参照换机前的正常状态的工况
        ArrayList<ProcessLogEntity> addOrUpdateList = new ArrayList<>();

        // 1:添加换机工况
        String startTime = IntervalTimeUtil.dateFormat(dto.getStartTime(), ConstantTime.DATE_TIME);
        String endTime = TimeUtil.getCalcTime(startTime, 1, ConstantBase.D);
        replaceMachineAddEntity.setStartTime(startTime);
        replaceMachineAddEntity.setEndTime(endTime);
        addOrUpdateList.add(replaceMachineAddEntity);

        // 获取当前startTime的之前的所有工况  (ps:默认查询前100个,理论上肯定存在正常的数据)
        List<ProcessLogEntity> beforeList = getBeforeListByTimeDESC(replaceMachineAddEntity.getStartTime(), 100);

        // 2:初始化正常的工况
        ProcessLogEntity normalAddEntity = null;
        if (beforeList != null && !beforeList.isEmpty()) {
            for (ProcessLogEntity processLogEntity : beforeList) {
                if (processLogEntity.getReplaceMachine().equals(ReplaceMachineEnum.NORMAL.getValue())) {
                    // 找到之前的数据当中  第一个正常的工况
                    // 换机还要在新增默认正常的一个工况
                    normalAddEntity = ConvertUtils.sourceToTarget(processLogEntity, ProcessLogEntity.class);
                    normalAddEntity.setId(null);// 新增数据 id为null
                    // 开始时间为entity的结束时间 + 1分钟
                    normalAddEntity.setStartTime(TimeUtil.getCalcTime(endTime, 1, ConstantBase.MIN));
                    normalAddEntity.setEndTime(null);
                    normalAddEntity.setOperator("System");
                    normalAddEntity.setOperationDate(replaceMachineAddEntity.getOperationDate());
                    addOrUpdateList.add(normalAddEntity);
                    break;
                }
            }

            // 2.1 如果上一个时段是正常的,更新上一个正常时段的endTime  如果上个时段是换机,则不处理(因为换机有默认结束时间)
            ProcessLogEntity beforeUpdateEntity = beforeList.get(0);
            if (beforeUpdateEntity.getReplaceMachine().equals(ReplaceMachineEnum.NORMAL.getValue())) {
                beforeUpdateEntity.setEndTime(TimeUtil.getCalcTime(replaceMachineAddEntity.getStartTime(), -1, ConstantBase.MIN));
                addOrUpdateList.add(beforeUpdateEntity);
            }
        }

        // 3:如果是中间插入的数据  需要补全新增正常工况的endTime
        if (normalAddEntity != null) {
            ProcessLogEntity nextUpdateEntity = getAfterEntityByTimeASC(normalAddEntity.getStartTime());
            if (ObjectUtils.isNotEmpty(nextUpdateEntity)) {
                // 如果存在下一个时段信息  证明是中间插入
                // 需要根据下一个entity补充 当前的endTime (ps:下一个时段 向前提一分钟)
                normalAddEntity.setEndTime(TimeUtil.getCalcTime(nextUpdateEntity.getStartTime(), -1, ConstantBase.MIN));
            }
        }

        saveOrUpdateBatch(addOrUpdateList);
    }

    private List<ProcessLogEntity> getBeforeListByTimeDESC(String startTime, Integer limit) {
        LambdaQueryWrapper<ProcessLogEntity> wrapper = new LambdaQueryWrapper<ProcessLogEntity>()
                .lt(ProcessLogEntity::getStartTime, startTime)
                .orderByDesc(ProcessLogEntity::getStartTime)
                .last("limit " + limit);

        return baseMapper.selectList(wrapper);
    }

    private ProcessLogEntity getAfterEntityByTimeASC(String startTime) {
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
            record.setStartTime(IntervalTimeUtil.dateFormat(record.getStartTime(),"yyyy-MM-dd HH:mm"));
            record.setEndTime(IntervalTimeUtil.dateFormat(record.getEndTime(),"yyyy-MM-dd HH:mm"));
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
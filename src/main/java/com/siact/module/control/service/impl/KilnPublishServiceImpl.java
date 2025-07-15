package com.siact.module.control.service.impl;

import com.siact.common.R;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.control.service.KilnPublishService;
import com.siact.module.control.validator.RuleValidateResult;
import com.siact.module.control.validator.RuleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 下发服务实现类
 *
 * @author wr
 */
@Service
@Slf4j
public class KilnPublishServiceImpl implements KilnPublishService {

    @Autowired
    private IKilnInfoService kilnInfoService;

    @Autowired
    private List<RuleValidator> validators;

    /**
     * 手动下发(不需要校验条规)
     * @param list
     * @return
     */
    @Override
    public R publish(List<KilnInfoDistributeDTO> list) {

        // 1:获取当前DCS运行值  (ps:查询点位)

        // 2. 保存下发参数
        kilnInfoService.updateDistribute(list);

        // TODO  没有下发记录

        // 3. 下发 暂时不开发
        return R.success();
    }

    /**
     * 自动下发(需要校验条规)
     * @return
     */
    @Override
    public R autoPublish() {

        // 1:获取当前智控计算值 (ps:查询算法)
        List<KilnInfoDistributeDTO> list = new ArrayList<>();

        // 2:获取当前DCS运行值 (ps:查询点位)

        // 3:本次控制变动值 = 智控计算值 - 当前DCS运行值 的绝对值  (ps:这里的逻辑暂时未完成  TODO)

        // 4. 责任链自动校验参数
        AnnotationAwareOrderComparator.sort(validators);
        for (RuleValidator validator : validators) {
            RuleValidateResult result = validator.validate(list);
            if (!result.isPass()) {
                return R.success(result.getMessage(), result.getErrors());
            }
        }

        // 5. 下发 暂时不开发
        return R.success();
    }
}

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

    @Override
    public R publish(List<KilnInfoDistributeDTO> list) {
        // 1. 更新下发参数
        kilnInfoService.updateDistribute(list);

        // 2. 责任链自动校验
        AnnotationAwareOrderComparator.sort(validators);
        for (RuleValidator validator : validators) {
            RuleValidateResult result = validator.validate(list);
            if (!result.isPass()) {
                return R.fail(result.getMessage(), result.getErrors());
            }
        }

        // 3. 下发 暂时不开发
        return R.success();
    }
}

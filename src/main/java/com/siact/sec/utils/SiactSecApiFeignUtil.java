package com.siact.sec.utils;

import com.siact.api.common.api.vo.common.R;
import com.siact.common.exception.ActiveException;
import com.siact.common.exception.CommonEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
public class SiactSecApiFeignUtil<T> {
    /**
     * 从R中获取对象
     *
     * @param r R
     * @return 对象
     */
    public static <T> T obj(R<T> r) {
        checkRStatus(r);
        return r.getData();
    }

    /**
     * 从R中获取对象集合
     *
     * @param r R
     * @return 对象集合
     */
    public static <T> List<T> list(R<List<T>> r) {
        checkRStatus(r);
        return r.getData();
    }

    /**
     * 检查R状态
     *
     * @param r R
     */
    private static void checkRStatus(R r) {

        if (ObjectUtils.isEmpty(r)) {
            log.error("查询数字孪生出错，无返回值!");
            throw new ActiveException(CommonEnum.REQUEST_FAIL);
        }

        if(!Objects.equals(R.OK().getCode(), r.getCode())) {
            log.error("查询数字孪生返回状态非200出错，msg:{}", r.getMsg());
            throw new ActiveException(String.format("获取R结果异常：code: %s, msg: %s", r.getCode(), r.getMsg()));
        }
        if (ObjectUtils.isEmpty(r.getData())) {
            log.info("查询数字孪生无数据！");
            throw new ActiveException(CommonEnum.REQUEST_DATA_BLANK);
        }
    }
}

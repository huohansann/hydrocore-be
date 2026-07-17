package com.siact.hydrocore.common.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-22 19:41
 * @className : PageVO
 * @description : 分页视图对象
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PageVO<T> {
    private Long current;
    private Long size;
    private Long pages;
    private Long total;
    private List<T> records;

    public static <T> PageVO<T> empty() {
        return new PageVO<>(1L, 10L, 1L, 0L, Collections.emptyList());
    }

    public static <T> PageVO<T> build(IPage<T> page) {
        return PageVO.<T>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .total(page.getTotal())
                .records(page.getRecords())
                .build();
    }
}

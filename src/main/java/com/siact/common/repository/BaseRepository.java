package com.siact.common.repository;

import java.util.Collection;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-07 9:25
 * @className : BaseRepository
 * @description : 基础数据持久层对象
 */
public interface BaseRepository<T> {
    default boolean saveBatch(Collection<T> entityList) {
        return this.saveBatch(entityList, 1000);
    }

    boolean saveBatch(Collection<T> entityList, int batchSize);
}

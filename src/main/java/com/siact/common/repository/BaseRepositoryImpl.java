package com.siact.common.repository;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-07 9:46
 * @className : BaseRepositoryImpl
 * @description : 基础数据持久层对象
 */
public class BaseRepositoryImpl<M extends BaseMapper<T>, T> implements BaseRepository<T> {
    protected Log log = LogFactory.getLog(this.getClass());

    protected Class<T> entityClass = this.currentModelClass();
    protected Class<M> mapperClass = this.currentMapperClass();


    protected @SuppressWarnings("unchecked") Class<M> currentMapperClass() {
        return (Class<M>) ReflectionKit.getSuperClassGenericType(this.getClass(), BaseRepositoryImpl.class, 0);
    }

    protected @SuppressWarnings("unchecked") Class<T> currentModelClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(), BaseRepositoryImpl.class, 1);
    }


    public boolean saveBatch(Collection<T> entityList) {
        return this.saveBatch(entityList, 1000);
    }

    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        String sqlStatement = this.getSqlStatement(SqlMethod.INSERT_ONE);
        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
    }

    protected String getSqlStatement(SqlMethod sqlMethod) {
        return SqlHelper.getSqlStatement(this.mapperClass, sqlMethod);
    }

    protected <E> boolean executeBatch(Collection<E> list, int batchSize, BiConsumer<SqlSession, E> consumer) {
        return SqlHelper.executeBatch(this.entityClass, this.log, list, batchSize, consumer);
    }
}

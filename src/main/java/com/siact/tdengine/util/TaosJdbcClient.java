package com.siact.tdengine.util;

import com.siact.tdengine.config.TaosConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TDengine JDBC 查询封装
 * 提供统一的查询执行和结果处理
 */
@Slf4j
@Component
public class TaosJdbcClient {

    @Resource
    private TaosConfig taosConfig;

    /**
     * 支持 SQLException 的函数式接口
     */
    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(ResultSet rs) throws SQLException;
    }

    /**
     * 执行查询并使用映射函数处理结果
     *
     * @param sql      SQL 查询语句
     * @param mapper   结果映射函数
     * @return 查询结果列表
     */
    public <T> List<T> executeQuery(String sql, SqlFunction<T> mapper) {
        log.debug("执行 TDengine 查询: {}", sql);
        List<T> results = new ArrayList<>();

        HikariDataSource dataSource = taosConfig.getDataSource();
        if (dataSource == null) {
            log.warn("TDengine 数据源未配置，请检查 spring.datasource.taos.url 配置");
            return results;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapper.apply(rs));
            }

            log.debug("查询返回 {} 条记录", results.size());
            return results;

        } catch (SQLException e) {
            log.error("TDengine 查询失败: {}", e.getMessage(), e);
            return results; // 返回空列表，不抛异常
        }
    }

    /**
     * 执行查询返回单条结果
     *
     * @param sql      SQL 查询语句
     * @param mapper   结果映射函数
     * @return 单条结果，无结果时返回 null
     */
    public <T> T executeQueryOne(String sql, SqlFunction<T> mapper) {
        List<T> results = executeQuery(sql, mapper);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 获取数据值（处理 NULL）
     */
    public Double getDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * 获取字符串值（处理 NULL）
     */
    public String getString(ResultSet rs, String column) throws SQLException {
        return rs.getString(column);
    }

    /**
     * 获取时间戳字符串
     */
    public String getTimestampString(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toString() : null;
    }
}
package com.siact.hydrocore.tdengine.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TDengine SQL 语句构建工具
 * 提供安全的 SQL 构建，防止 SQL 注入
 */
public class TaosSqlBuilder {

    private static final String TABLE_NAME = "datasource";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * 构建等时间间隔聚合查询 SQL
     *
     * @param dataCodes   数字孪生编码列表（对应 devproperty TAG）
     * @param startTime   开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime     结束时间 yyyy-MM-dd HH:mm:ss
     * @param interval    时间间隔数值
     * @param unit        时间间隔单位（s/m/h/d）
     * @param calcType    聚合类型 AVG/MAX/MIN/LAST/FIRST/SUM/COUNT
     * @return SQL 语句
     */
    public static String buildIntervalQuerySql(List<String> dataCodes, String startTime,
                                                String endTime, int interval, String unit,
                                                String calcType) {
        String aggregateFunc = getAggregateFunction(calcType);
        String intervalUnit = convertIntervalUnit(unit);
        String safeStartTime = sanitizeTimestamp(startTime);
        String safeEndTime = sanitizeTimestamp(endTime);

        // TDengine 2.4 使用 _wstartts，LAST 类型不支持 INTERVAL
        if ("LAST".equals(aggregateFunc)) {
            return String.format(
                "SELECT ts, devproperty as datacode, itemvalue FROM %s " +
                "WHERE devproperty IN (%s) AND ts >= '%s' AND ts <= '%s' " +
                "ORDER BY ts DESC",
                TABLE_NAME, buildInClause(dataCodes), safeStartTime, safeEndTime
            );
        }

        return String.format(
            "SELECT _wstartts as ts, devproperty as datacode, %s(itemvalue) as itemvalue FROM %s " +
            "WHERE devproperty IN (%s) AND ts >= '%s' AND ts <= '%s' " +
            "INTERVAL(%d%s) FILL(NULL)",
            aggregateFunc, TABLE_NAME, buildInClause(dataCodes), safeStartTime, safeEndTime,
            interval, intervalUnit
        );
    }

    /**
     * 构建时间段聚合查询 SQL（查询两点之间的聚合值）
     *
     * @param dataCodes   数字孪生编码列表
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param calcType    聚合类型
     * @return SQL 语句
     */
    public static String buildAggregateQuerySql(List<String> dataCodes, String startTime,
                                                  String endTime, String calcType) {
        String aggregateFunc = getAggregateFunction(calcType);
        String safeStartTime = sanitizeTimestamp(startTime);
        String safeEndTime = sanitizeTimestamp(endTime);

        return String.format(
            "SELECT devproperty as datacode, %s(itemvalue) as itemvalue FROM %s " +
            "WHERE devproperty IN (%s) AND ts >= '%s' AND ts <= '%s'",
            aggregateFunc, TABLE_NAME, buildInClause(dataCodes), safeStartTime, safeEndTime
        );
    }

    /**
     * 构建实时值查询 SQL（查询最新一条数据）
     *
     * @param dataCodes   数字孪生编码列表
     * @return SQL 语句
     */
    public static String buildLatestQuerySql(List<String> dataCodes) {
        return String.format(
            "SELECT devproperty as datacode, itemvalue FROM %s " +
            "WHERE devproperty IN (%s) ORDER BY ts DESC LIMIT 1",
            TABLE_NAME, buildInClause(dataCodes)
        );
    }

    /**
     * 构建多编码实时值查询 SQL（每个编码最新一条）
     *
     * @param dataCodes   数字孪生编码列表
     * @return SQL 语句
     */
    public static String buildLatestQuerySqlForMultiple(List<String> dataCodes) {
        // TDengine 使用 LAST_ROW 查询每个子表的最新数据
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT devproperty as datacode, LAST(itemvalue) as itemvalue FROM ")
           .append(TABLE_NAME)
           .append(" WHERE devproperty IN (")
           .append(buildInClause(dataCodes))
           .append(") GROUP BY devproperty");
        return sql.toString();
    }

    /**
     * 构建原始时序数据查询 SQL（无聚合）
     *
     * @param dataCodes   数字孪生编码列表
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return SQL 语句
     */
    public static String buildRawDataQuerySql(List<String> dataCodes, String startTime, String endTime) {
        String safeStartTime = sanitizeTimestamp(startTime);
        String safeEndTime = sanitizeTimestamp(endTime);

        return String.format(
            "SELECT ts, devproperty as datacode, itemvalue FROM %s " +
            "WHERE devproperty IN (%s) AND ts >= '%s' AND ts <= '%s' ORDER BY ts",
            TABLE_NAME, buildInClause(dataCodes), safeStartTime, safeEndTime
        );
    }

    /**
     * 构建节点属性时间段聚合查询 SQL
     * 使用 itemid TAG 过滤属性
     *
     * @param dataCode        数字孪生编码（devproperty）
     * @param propModelCodes  属性短码列表（itemid）
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param calcType        聚合类型
     * @return SQL 语句
     */
    public static String buildNodeAggregateQuerySql(String dataCode, List<String> propModelCodes,
                                                     String startTime, String endTime,
                                                     String calcType) {
        String aggregateFunc = getAggregateFunction(calcType);
        String safeStartTime = sanitizeTimestamp(startTime);
        String safeEndTime = sanitizeTimestamp(endTime);

        return String.format(
            "SELECT itemid as propcode, %s(itemvalue) as itemvalue FROM %s " +
            "WHERE devproperty = '%s' AND itemid IN (%s) AND ts >= '%s' AND ts <= '%s'",
            aggregateFunc, TABLE_NAME, escapeValue(dataCode),
            buildInClause(propModelCodes), safeStartTime, safeEndTime
        );
    }

    /**
     * 构建节点属性等时间间隔查询 SQL
     *
     * @param dataCode        数字孪生编码
     * @param propModelCodes  属性短码列表
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param interval        时间间隔
     * @param unit            时间单位
     * @param calcType        聚合类型
     * @return SQL 语句
     */
    public static String buildNodeIntervalQuerySql(String dataCode, List<String> propModelCodes,
                                                    String startTime, String endTime,
                                                    int interval, String unit, String calcType) {
        String aggregateFunc = getAggregateFunction(calcType);
        String intervalUnit = convertIntervalUnit(unit);
        String safeStartTime = sanitizeTimestamp(startTime);
        String safeEndTime = sanitizeTimestamp(endTime);

        // TDengine 2.4 使用 _wstartts，LAST 类型不支持 INTERVAL
        if ("LAST".equals(aggregateFunc)) {
            return String.format(
                "SELECT ts, itemid as propcode, itemvalue FROM %s " +
                "WHERE devproperty = '%s' AND itemid IN (%s) AND ts >= '%s' AND ts <= '%s' " +
                "ORDER BY ts DESC",
                TABLE_NAME, escapeValue(dataCode),
                buildInClause(propModelCodes), safeStartTime, safeEndTime
            );
        }

        return String.format(
            "SELECT _wstartts as ts, itemid as propcode, %s(itemvalue) as itemvalue FROM %s " +
            "WHERE devproperty = '%s' AND itemid IN (%s) AND ts >= '%s' AND ts <= '%s' " +
            "INTERVAL(%d%s) FILL(NULL)",
            aggregateFunc, TABLE_NAME, escapeValue(dataCode),
            buildInClause(propModelCodes), safeStartTime, safeEndTime, interval, intervalUnit
        );
    }

    /**
     * 构建 IN 子句
     *
     * @param values   值列表
     * @return IN 子句字符串，如 'code1', 'code2'
     */
    private static String buildInClause(List<String> values) {
        return values.stream()
            .map(v -> "'" + escapeValue(v) + "'")
            .collect(Collectors.joining(", "));
    }

    /**
     * 转义值防止 SQL 注入
     *
     * @param value   原始值
     * @return 转义后的值
     */
    private static String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''")
                .replace(";", "")
                .replace("--", "")
                .replaceAll("(?i)\\b(drop|delete|insert|update|alter|truncate|create|exec|execute|union)\\b", "");
    }

    private static String sanitizeTimestamp(String value) {
        try {
            return LocalDateTime.parse(value, TIMESTAMP_FORMATTER).format(TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid timestamp format, expected yyyy-MM-dd HH:mm:ss", e);
        }
    }

    /**
     * 获取聚合函数 SQL 名称
     *
     * @param calcType   计算类型
     * @return SQL 函数名
     */
    static String getAggregateFunction(String calcType) {
        switch (calcType.toUpperCase()) {
            case "AVG":
                return "AVG";
            case "MAX":
                return "MAX";
            case "MIN":
                return "MIN";
            case "LAST":
                return "LAST";
            case "FIRST":
                return "FIRST";
            case "SUM":
                return "SUM";
            case "COUNT":
                return "COUNT";
            case "INC":
                return "SUM"; // 增量使用 SUM 计算
            case "TOTAL":
                return "SUM";
            default:
                return "AVG";
        }
    }

    /**
     * 转换时间间隔单位为 TDengine 格式
     *
     * @param unit   原始单位（Y/M/D/H/MIN/S）
     * @return TDengine INTERVAL 单位
     */
    private static String convertIntervalUnit(String unit) {
        switch (unit.toUpperCase()) {
            case "Y":
                return "y";
            case "M":
                return "m"; // 月
            case "D":
                return "d";
            case "H":
                return "h";
            case "MIN":
                return "m"; // 分钟（TDengine 用 m 表示分钟，需要根据数值区分）
            case "S":
                return "s";
            default:
                return "h";
        }
    }
}

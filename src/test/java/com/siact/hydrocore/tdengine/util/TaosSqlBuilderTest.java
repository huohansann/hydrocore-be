package com.siact.hydrocore.tdengine.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaosSqlBuilderTest {

    @Test
    void testBuildIntervalQuerySql() {
        List<String> dataCodes = Arrays.asList("code1", "code2");
        String sql = TaosSqlBuilder.buildIntervalQuerySql(
            dataCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            1, "h", "AVG"
        );

        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("AVG(itemvalue)"));
        assertTrue(sql.contains("INTERVAL(1h)"));
        assertTrue(sql.contains("'code1', 'code2'"));
    }

    @Test
    void testBuildAggregateQuerySql() {
        List<String> dataCodes = Arrays.asList("code1");
        String sql = TaosSqlBuilder.buildAggregateQuerySql(
            dataCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            "MAX"
        );

        assertNotNull(sql);
        assertTrue(sql.contains("MAX(itemvalue)"));
        assertTrue(sql.contains("'code1'"));
    }

    @Test
    void testBuildLatestQuerySql() {
        List<String> dataCodes = Arrays.asList("code1", "code2");
        String sql = TaosSqlBuilder.buildLatestQuerySqlForMultiple(dataCodes);

        assertNotNull(sql);
        assertTrue(sql.contains("LAST(itemvalue)"));
        assertTrue(sql.contains("GROUP BY devproperty"));
    }

    @Test
    void testSqlInjectionProtection() {
        List<String> dataCodes = Arrays.asList("code'; DROP TABLE datasource;--");
        String sql = TaosSqlBuilder.buildAggregateQuerySql(
            dataCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            "AVG"
        );

        // 单引号应该被转义为双单引号
        assertTrue(sql.contains("''"));
        assertFalse(sql.contains("DROP TABLE"));
    }

    @Test
    void testGetAggregateFunction() {
        assertEquals("AVG", TaosSqlBuilder.getAggregateFunction("AVG"));
        assertEquals("MAX", TaosSqlBuilder.getAggregateFunction("MAX"));
        assertEquals("MIN", TaosSqlBuilder.getAggregateFunction("MIN"));
        assertEquals("SUM", TaosSqlBuilder.getAggregateFunction("INC"));
        assertEquals("SUM", TaosSqlBuilder.getAggregateFunction("TOTAL"));
        assertEquals("AVG", TaosSqlBuilder.getAggregateFunction("UNKNOWN"));
    }

    @Test
    void testBuildNodeAggregateQuerySql() {
        List<String> propCodes = Arrays.asList("EP1", "EP2");
        String sql = TaosSqlBuilder.buildNodeAggregateQuerySql(
            "device001", propCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            "AVG"
        );

        assertNotNull(sql);
        assertTrue(sql.contains("itemid as propcode"));
        assertTrue(sql.contains("devproperty = 'device001'"));
        assertTrue(sql.contains("'EP1', 'EP2'"));
    }

    @Test
    void testBuildNodeIntervalQuerySql() {
        List<String> propCodes = Arrays.asList("EP1");
        String sql = TaosSqlBuilder.buildNodeIntervalQuerySql(
            "device001", propCodes, "2024-01-01 00:00:00", "2024-01-02 00:00:00",
            1, "h", "MAX"
        );

        assertNotNull(sql);
        assertTrue(sql.contains("MAX(itemvalue)"));
        assertTrue(sql.contains("INTERVAL(1h)"));
        assertTrue(sql.contains("itemid as propcode"));
    }
}
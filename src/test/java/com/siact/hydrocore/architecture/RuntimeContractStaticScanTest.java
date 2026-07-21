package com.siact.hydrocore.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeContractStaticScanTest {
    private static final Path MAIN_JAVA = Paths.get("src", "main", "java");
    private static final Path NACOS_CONFIG = Paths.get("src", "main", "resources", "nacos", "hydrocore.yml");
    private static final Path SCHEMA_SQL = Paths.get("src", "main", "resources", "db", "schema", "hydrocore_schema.sql");

    @Test
    void apiRuntimeCodeDoesNotUseLegacyResponseWrappersOrAdvice() throws IOException {
        List<String> forbidden = Arrays.asList(
                "com.siact.hydrocore.common.R",
                "com.siact.hydrocore.common.result.R",
                "com.siact.hydrocore.common.entity.ResponseEntity",
                "com.siact.hydrocore.common.annotation.NoResponseAdvice",
                "ResponseEntity<",
                "ResponseEntity.",
                "implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice"
        );

        List<String> failures = scanJavaFiles()
                .filter(path -> !path.endsWith(Paths.get("common", "R.java")))
                .filter(path -> !path.endsWith(Paths.get("common", "result", "R.java")))
                .filter(path -> !path.endsWith(Paths.get("common", "entity", "ResponseEntity.java")))
                .filter(path -> !path.endsWith(Paths.get("common", "annotation", "NoResponseAdvice.java")))
                .flatMap(path -> matchingLines(path, forbidden).stream())
                .collect(Collectors.toList());

        assertThat(failures).isEmpty();
    }

    @Test
    void runtimeCodeDoesNotUseConsoleOrPrintStackTrace() throws IOException {
        List<String> forbidden = Arrays.asList("System.out.println", "printStackTrace(");

        List<String> failures = scanJavaFiles()
                .flatMap(path -> matchingLines(path, forbidden).stream())
                .collect(Collectors.toList());

        assertThat(failures).isEmpty();
    }

    @Test
    void runtimeCodeDoesNotInjectRedisTemplatesOutsideRedisBoundary() throws IOException {
        List<String> forbidden = Arrays.asList(
                "org.springframework.data.redis.core.RedisTemplate",
                "org.springframework.data.redis.core.StringRedisTemplate",
                " RedisTemplate<",
                " StringRedisTemplate "
        );

        List<String> failures = scanJavaFiles()
                .filter(path -> !isRedisBoundary(path))
                .flatMap(path -> matchingLines(path, forbidden).stream())
                .collect(Collectors.toList());

        assertThat(failures).isEmpty();
    }

    @Test
    void redisCacheCodeDoesNotUseDynamicTypeMetadata() throws IOException {
        List<String> forbidden = Arrays.asList(
                "JSONWriter.Feature.WriteClassName",
                "JSONReader.autoTypeFilter",
                "AUTO_TYPE_FILTER",
                "parseObject(json, Object.class",
                "parseObject(str, Object.class",
                "new FastJson2JsonRedisSerializer(Object.class)"
        );

        List<String> failures = scanJavaFiles()
                .flatMap(path -> matchingLines(path, forbidden).stream())
                .collect(Collectors.toList());

        assertThat(failures).isEmpty();
    }

    @Test
    void runtimeCodeDoesNotUseLegacyResultCodeContract() throws IOException {
        List<String> forbidden = Arrays.asList(
                "com.siact.hydrocore.common.result.ResultCode",
                "com.siact.hydrocore.common.result.IErrorCode",
                "ResultCode implements IErrorCode",
                "interface IErrorCode"
        );

        List<String> failures = scanJavaFiles()
                .flatMap(path -> matchingLines(path, forbidden).stream())
                .collect(Collectors.toList());

        assertThat(failures).isEmpty();
    }

    @Test
    void businessRedisCallersUseExplicitStringOrTypedJsonMethods() throws IOException {
        List<String> forbidden = Arrays.asList(
                ".setCacheObject(",
                ".getCacheObject(",
                ".setCacheMapValue(",
                ".getCacheMapValue(",
                ".getMultiCacheMapValue(",
                ".setCacheMap(",
                ".deleteObject(",
                ".deleteCacheMapValue("
        );

        List<String> failures = scanJavaFiles()
                .filter(path -> !isRedisBoundary(path))
                .flatMap(path -> matchingLines(path, forbidden).stream())
                .collect(Collectors.toList());

        assertThat(failures).isEmpty();
    }

    @Test
    void sysMenuSchemaMatchesRuntimeEntityContract() throws IOException {
        String schema = readUtf8(SCHEMA_SQL);

        assertThat(schema).contains(
                "`path` varchar(200)",
                "`icon` varchar(100)",
                "`visible` tinyint(1)",
                "`sort` int"
        );
        assertThat(schema).doesNotContain("`menu_url`", "`menu_icon`", "`model_show`");
    }

    @Test
    void systemRoleAndOrganizationSchemaMatchesRuntimeEntityContract() throws IOException {
        String schema = readUtf8(SCHEMA_SQL);

        assertThat(schema).contains(
                "`role_name` varchar(50)",
                "`role_code` varchar(50)",
                "UNIQUE INDEX `idx_role_code`(`role_code` ASC)",
                "`org_name` varchar(100)",
                "`org_code` varchar(50)"
        );
        assertThat(schema).doesNotContain(
                "`name` varchar(50)",
                "`code` varchar(50)",
                "UNIQUE INDEX `idx_code`(`code` ASC)"
        );
    }

    @Test
    void mysqlRuntimeAndSchemaUseUtf8mb4Encoding() throws IOException {
        String config = readUtf8(NACOS_CONFIG).toLowerCase();
        String schema = readUtf8(SCHEMA_SQL).toLowerCase();

        assertThat(config.contains("characterencoding=utf8") || config.contains("characterencoding=utf-8"))
                .isTrue();
        assertThat(config).doesNotContain("characterencoding=utf8mb4");
        assertThat(schema).contains("set names utf8mb4");
        assertThat(schema).doesNotContain("collate utf8mb4_general_ci", "collate = utf8mb4_general_ci");
    }

    @Test
    void frontendTrendChartEndpointIsExposedByActiveController() throws IOException {
        Path controller = MAIN_JAVA.resolve(Paths.get("com", "siact", "hydrocore", "sec", "controller", "DataController.java"));
        String source = readUtf8(controller);

        assertThat(source).contains("package com.siact.hydrocore.sec.controller;");
        assertThat(hasActiveAnnotation(source, "@RestController")).isTrue();
        assertThat(hasActiveAnnotation(source, "@RequestMapping(\"/api/data\")")).isTrue();
        assertThat(hasActiveAnnotation(source, "@PostMapping(\"/queryCommonChartData\")")).isTrue();
        assertThat(source).contains("ApiResponse<CommonChartResultDto>");
    }

    private Stream<Path> scanJavaFiles() throws IOException {
        return Files.walk(MAIN_JAVA)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"));
    }

    private String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private boolean isRedisBoundary(Path path) {
        return path.endsWith(Paths.get("common", "redis", "RedisConfig.java"))
                || path.endsWith(Paths.get("common", "redis", "RedisService.java"));
    }

    private List<String> matchingLines(Path path, List<String> forbidden) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            return java.util.stream.IntStream.range(0, lines.size())
                    .filter(index -> forbidden.stream().anyMatch(lines.get(index)::contains))
                    .mapToObj(index -> path + ":" + (index + 1) + " " + lines.get(index).trim())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan " + path, e);
        }
    }

    private boolean hasActiveAnnotation(String source, String annotation) {
        return Pattern.compile("(?m)^\\s*" + Pattern.quote(annotation)).matcher(source).find();
    }
}

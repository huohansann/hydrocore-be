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

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeContractStaticScanTest {
    private static final Path MAIN_JAVA = Paths.get("src", "main", "java");

    @Test
    void apiRuntimeCodeDoesNotUseLegacyResponseWrappersOrAdvice() throws IOException {
        List<String> forbidden = Arrays.asList(
                "com.siact.hydrocore.common.R",
                "com.siact.hydrocore.common.result.R",
                "ResponseEntity<",
                "ResponseEntity.",
                "implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice"
        );

        List<String> failures = scanJavaFiles()
                .filter(path -> !path.endsWith(Paths.get("common", "R.java")))
                .filter(path -> !path.endsWith(Paths.get("common", "result", "R.java")))
                .filter(path -> !path.endsWith(Paths.get("common", "entity", "ResponseEntity.java")))
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

    private Stream<Path> scanJavaFiles() throws IOException {
        return Files.walk(MAIN_JAVA)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"));
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
}

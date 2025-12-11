package com.siact.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-11 8:57
 * @className : LogbackEvaluatorFilter
 * @description : Logback 自定义求值过滤器
 */
public class LogbackEvaluatorFilter extends Filter<ILoggingEvent> {
    private final List<Pattern> compiledPatterns = new ArrayList<>();
    private FilterReply onMatch = FilterReply.ACCEPT;
    private FilterReply onMismatch = FilterReply.DENY;

    public void setPattern(String pattern) {
        if (!StringUtils.isBlank(pattern)) compiledPatterns.add(Pattern.compile(pattern));
    }

    public void setPatterns(Patterns patterns) {
        if (!Objects.isNull(patterns)) {
            for (String value : patterns.getValues()) {
                compiledPatterns.add(Pattern.compile(value));
            }
        }
    }

    public void setOnMatch(String onMatch) {
        this.onMatch = FilterReply.valueOf(onMatch);
    }

    public void setOnMismatch(String onMismatch) {
        this.onMismatch = FilterReply.valueOf(onMismatch);
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted()) return FilterReply.NEUTRAL;
        if (Objects.isNull(event) || StringUtils.isEmpty(event.getLoggerName())) return onMismatch;

        String name = event.getLoggerName();
        for (Pattern pattern : compiledPatterns) {
            if (pattern.matcher(name).find()) return FilterReply.ACCEPT;
        }
        return onMismatch;
    }

    @Getter
    public static class Patterns {
        private final List<String> values = new ArrayList<>();

        public void addPattern(String pattern) {
            if (StringUtils.isNotBlank(pattern)) values.add(pattern);
        }
    }
}

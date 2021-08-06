/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.witchcraft.logging.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import com.palantir.witchcraft.api.logging.AuditLogV2;
import com.palantir.witchcraft.api.logging.DiagnosticLogV1;
import com.palantir.witchcraft.api.logging.EventLogV2;
import com.palantir.witchcraft.api.logging.MetricLogV1;
import com.palantir.witchcraft.api.logging.RequestLogV2;
import com.palantir.witchcraft.api.logging.ServiceLogV1;
import com.palantir.witchcraft.api.logging.TraceLogV1;
import com.palantir.witchcraft.api.logging.WrappedLogV1;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PreferSafeLogger", "Slf4jLogsafeArgs"}) // Logging for the IDE
public final class LogParser<T> {
    private static final Logger log = LoggerFactory.getLogger(LogParser.class);

    private static final String SERVICE_V1 = "service.1";
    private static final String REQUEST_V2 = "request.2";
    private static final String EVENT_V2 = "event.2";
    private static final String METRIC_V1 = "metric.1";
    private static final String TRACE_V1 = "trace.1";
    private static final String AUDIT_V2 = "audit.2";
    private static final String DIAGNOSTIC_V1 = "diagnostic.1";
    private static final String WRAPPED_V1 = "wrapped.1";
    private static final ImmutableList<String> LOG_TYPES = ImmutableList.of(
            SERVICE_V1, REQUEST_V2, EVENT_V2, METRIC_V1, TRACE_V1, AUDIT_V2, DIAGNOSTIC_V1, WRAPPED_V1);

    private static final String WITCHCRAFT_LOG_PATTERN_STRING = "\\{.*?\"type\"\\s*?:\\s*?\"("
            + LOG_TYPES.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")\".*?}";
    private static final Pattern WITCHCRAFT_LOG_PATTERN = Pattern.compile(WITCHCRAFT_LOG_PATTERN_STRING);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module().configureAbsentsAsNulls(true))
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.WRAP_EXCEPTIONS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final LogVisitor<T> logVisitor;
    private final WrappedLogDelegatingVisitor<T> wrappedLogDelegatingVisitor;

    public LogParser(LogVisitor<T> logVisitor) {
        this.logVisitor = logVisitor;
        this.wrappedLogDelegatingVisitor = new WrappedLogDelegatingVisitor<>(logVisitor);
    }

    public static boolean anyPossibleWitchcraftLogsInBlock(String blockOfText) {
        return WITCHCRAFT_LOG_PATTERN.matcher(blockOfText).find();
    }

    public Optional<T> tryParse(String logLine) {
        Matcher matcher = WITCHCRAFT_LOG_PATTERN.matcher(logLine);

        if (!matcher.matches()) {
            return Optional.empty();
        }

        String logType = matcher.group(1);

        switch (logType) {
            case SERVICE_V1:
                return applyToLogLine(logLine, ServiceLogV1.class, logVisitor::serviceV1);
            case REQUEST_V2:
                return applyToLogLine(logLine, RequestLogV2.class, logVisitor::requestV2);
            case EVENT_V2:
                return applyToLogLine(logLine, EventLogV2.class, logVisitor::eventV2);
            case METRIC_V1:
                return applyToLogLine(logLine, MetricLogV1.class, logVisitor::metricV1);
            case TRACE_V1:
                return applyToLogLine(logLine, TraceLogV1.class, logVisitor::traceV1);
            case AUDIT_V2:
                return applyToLogLine(logLine, AuditLogV2.class, logVisitor::auditV2);
            case DIAGNOSTIC_V1:
                return applyToLogLine(logLine, DiagnosticLogV1.class, logVisitor::diagnosticV1);
            case WRAPPED_V1:
                return applyToLogLine(logLine, WrappedLogV1.class, wrappedLogV1 -> wrappedLogV1
                        .getPayload()
                        .accept(wrappedLogDelegatingVisitor));
        }

        return Optional.empty();
    }

    private <L> Optional<T> applyToLogLine(String logLine, Class<L> clazz, Function<L, Optional<T>> function) {
        return parseJson(logLine, clazz).flatMap(function);
    }

    private static <L> Optional<L> parseJson(String logLine, Class<L> clazz) {
        try {
            return Optional.of(OBJECT_MAPPER.readValue(logLine, clazz));
        } catch (JsonProcessingException | RuntimeException e) {
            log.warn("Failed to deserialize witchcraft event for line '{}' into type {}", logLine, clazz, e);
            return Optional.empty();
        }
    }
}

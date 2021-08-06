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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class LogParserTest {
    private static final String EVENT_JSON = "{\"type\":\"event.2\",\"time\":\"2019-05-24T16:40:21.049Z\","
            + "\"eventName\":\"com.palantir.witchcraft.jvm.crash\","
            + "\"values\":{\"numJvmErrorLogs\":\"1\"},\"unsafeParams\":{},\"tags\":{}}";

    private static final String SERVICE_JSON = "{\"type\":\"service.1\",\"level\":\"ERROR\","
            + "\"time\":\"2019-05-09T15:32:37.692Z\",\"origin\":\"ROOT\","
            + "\"thread\":\"main\",\"message\":\"test good {}\","
            + "\"params\":{\"good\":\":-)\"},\"unsafeParams\":{},\"tags\":{}}";

    private static final String SERVICE_JSON_WITH_UNKNOWN_FIELD = "{\"type\":\"service.1\",\"level\":\"ERROR\","
            + "\"time\":\"2019-05-09T15:32:37.692Z\",\"origin\":\"ROOT\","
            + "\"thread\":\"main\",\"message\":\"test good {}\","
            + "\"params\":{\"good\":\":-)\"},\"unsafeParams\":{},\"unknownField\":\"value\"}";

    private static final String REQUEST_JSON = "{\"type\":\"request.2\",\"time\":\"2019-05-24T12:40:36.703-04:00\","
            + "\"method\":\"GET\",\"protocol\":\"HTTP/1.1\",\"path\":\"/api/sleep/{millis}\","
            + "\"params\":{\"host\":\"localhost:8443\",\"connection\":\"Keep-Alive\","
            + "\"accept-encoding\":\"gzip\",\"user-agent\":\"okhttp/3.13.1\"},"
            + "\"status\":503,\"requestSize\":0,\"responseSize\":78,\"duration\":1935,"
            + "\"traceId\":\"ba3200b6eb01999a\",\"unsafeParams\":{\"path\":\"/api/sleep/10\","
            + "\"millis\":\"10\"}}";

    private static final String METRIC_JSON = "{\"type\": \"metric.1\","
            + "\"time\":\"2019-05-24T16:40:52.162Z\","
            + "\"metricName\":\"jvm.heap\",\"metricType\":\"gauge\",\"values\":{\"size\":66274352},"
            + "\"tags\":{\"collection\":\"Metaspace\",\"collector\":\"PS Scavenge\","
            + "\"when\":\"after\"},\"unsafeParams\":{}}";

    private static final String TRACE_JSON = "{\"type\":\"trace.1\",\"time\":\"2019-05-24T16:40:40.95Z\","
            + "\"unsafeParams\":{},\"span\":{\"traceId\":\"2250486695021e19\",\"id\":\"c11b9a31555b7035\","
            + "\"name\":\"config-reload\",\"timestamp\":1558716040949000,\"duration\":618,"
            + "\"annotations\":[{\"timestamp\":1558716040949000,\"value\":\"lc\","
            + "\"endpoint\":{\"serviceName\":\"my-service\",\"ipv4\":\"10.193.122.103\"}}]}}";

    private static final String WRAPPED_SERVICE_JSON = "{\"type\":\"wrapped.1\",\"entityName\":\"foo\","
            + "\"entityVersion\":\"1.2.3\",\"payload\":{\"type\":\"serviceLogV1\",\"serviceLogV1\":" + SERVICE_JSON
            + "}}";

    @SuppressWarnings("unchecked")
    private final LogVisitor<String> logVisitor = mock(
            LogVisitor.class, invocation -> Optional.of(invocation.getMethod().getName()));

    private final LogParser<String> logParser = new LogParser<>(logVisitor);

    @Test
    void fastPredicateTest_nonWitchcraft() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("foobar")).isFalse();
    }

    @Test
    void fastPredicateTest_newlineBrokenWitchcraftLog() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("{\n" + METRIC_JSON.substring(1)))
                .describedAs("Newlines are illegal in Witchcraft logs, don't attempt to match")
                .isFalse();
    }

    @Test
    void fastPredicateTest_metric() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock(METRIC_JSON)).isTrue();
    }

    @Test
    void fastPredicateTest_metricNewUnsupported() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("{\"type\":\"metric.5\","
                        + "\"time\":\"2019-05-24T16:40:52.162Z\","
                        + "\"metricName\":\"jvm.heap\",\"metricType\":\"gauge\",\"values\":{\"size\":66274352},"
                        + "\"tags\":{\"collection\":\"Metaspace\",\"collector\":\"PS Scavenge\","
                        + "\"when\":\"after\"},\"unsafeParams\":{}}"))
                .isFalse();
    }

    @Test
    void fastPredicateTest_typeWithSpaces() {
        // space after '"type": ', slightly different from the default compact formatting
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("{\"type\": \"metric.1\","
                        + "\"time\":\"2019-05-24T16:40:52.162Z\","
                        + "\"metricName\":\"jvm.heap\",\"metricType\":\"gauge\",\"values\":{\"size\":66274352},"
                        + "\"tags\":{\"collection\":\"Metaspace\",\"collector\":\"PS Scavenge\","
                        + "\"when\":\"after\"},\"unsafeParams\":{}}"))
                .isTrue();
    }

    @Test
    void parse_event_logs() {
        assertThat(logParser.tryParse(EVENT_JSON)).hasValue("eventV2");
    }

    @Test
    void parse_service_logs() {
        assertThat(logParser.tryParse(SERVICE_JSON)).hasValue("serviceV1");
    }

    @Test
    void parse_service_logs_with_unknown_field() {
        assertThat(logParser.tryParse(SERVICE_JSON_WITH_UNKNOWN_FIELD)).hasValue("serviceV1");
    }

    @Test
    void parse_request_logs() {
        assertThat(logParser.tryParse(REQUEST_JSON)).hasValue("requestV2");
    }

    @Test
    void parse_metric_logs() {
        assertThat(logParser.tryParse(METRIC_JSON)).hasValue("metricV1");
    }

    @Test
    void parse_trace_logs() {
        assertThat(logParser.tryParse(TRACE_JSON)).hasValue("traceV1");
    }

    @Test
    void parse_wrapped_logs() {
        assertThat(logParser.tryParse(WRAPPED_SERVICE_JSON)).hasValue("serviceV1");
    }

    @Test
    void not_parse_partial_witchcraft_logs() {
        assertThat(logParser.tryParse(SERVICE_JSON.substring(5))).isEmpty();
    }

    @Test
    void not_parse_broken_witchcraft_logs() {
        assertThat(logParser.tryParse(SERVICE_JSON.replace("message", "mmmm"))).isEmpty();
    }

    @Test
    void not_parse_partial_witchcraft_logs_with_extra_data() {
        assertThat(logParser.tryParse("some other stuff " + SERVICE_JSON)).isEmpty();
    }
}

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

package com.palantir.witchcraft.logging.idea;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Pair;
import com.palantir.witchcraft.logging.format.LogParser;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

public final class WitchcraftLogFormatFilterTest {
    private static final String EVENT_JSON = "{\"type\":\"event.2\",\"time\":\"2019-05-24T16:40:21.049Z\","
            + "\"eventName\":\"com.palantir.witchcraft.jvm.crash\","
            + "\"values\":{\"numJvmErrorLogs\":\"1\"},\"unsafeParams\":{},\"tags\":{}}";
    private static final String EVENT_FORMATTED =
            "[2019-05-24T16:40:21.049Z] com.palantir.witchcraft.jvm.crash " + "(numJvmErrorLogs: 1)";

    private static final String SERVICE_JSON = "{\"type\":\"service.1\",\"level\":\"ERROR\","
            + "\"time\":\"2019-05-09T15:32:37.692Z\",\"origin\":\"ROOT\","
            + "\"thread\":\"main\",\"message\":\"test good {}\","
            + "\"params\":{\"good\":\":-)\"},\"unsafeParams\":{},\"tags\":{}}";
    private static final String SERVICE_FORMATTED = "ERROR [2019-05-09T15:32:37.692Z] ROOT: test good {} (good: :-))";

    private static final String REQUEST_JSON = "{\"type\":\"request.2\",\"time\":\"2019-05-24T12:40:36.703-04:00\","
            + "\"method\":\"GET\",\"protocol\":\"HTTP/1.1\",\"path\":\"/api/sleep/{millis}\","
            + "\"params\":{\"host\":\"localhost:8443\",\"connection\":\"Keep-Alive\","
            + "\"accept-encoding\":\"gzip\",\"user-agent\":\"okhttp/3.13.1\"},"
            + "\"status\":503,\"requestSize\":0,\"responseSize\":78,\"duration\":1935,"
            + "\"traceId\":\"ba3200b6eb01999a\",\"unsafeParams\":{\"path\":\"/api/sleep/10\","
            + "\"millis\":\"10\"}}";
    private static final String REQUEST_FORMATTED =
            "[2019-05-24T16:40:36.703Z] \"GET /api/sleep/10 HTTP/1.1\" " + "503 78 1935";

    private static final String METRIC_JSON = "{\"type\": \"metric.1\","
            + "\"time\":\"2019-05-24T16:40:52.162Z\","
            + "\"metricName\":\"jvm.heap\",\"metricType\":\"gauge\",\"values\":{\"size\":66274352},"
            + "\"tags\":{\"collection\":\"Metaspace\",\"collector\":\"PS Scavenge\","
            + "\"when\":\"after\"},\"unsafeParams\":{}}";
    private static final String METRIC_FORMATTED = "[2019-05-24T16:40:52.162Z] METRIC jvm.heap gauge (size: 66274352) "
            + "(collection: Metaspace, collector: PS Scavenge, when: after)";

    private static final String TRACE_JSON = "{\"type\":\"trace.1\",\"time\":\"2019-05-24T16:40:40.95Z\","
            + "\"unsafeParams\":{},\"span\":{\"traceId\":\"2250486695021e19\",\"id\":\"c11b9a31555b7035\","
            + "\"name\":\"config-reload\",\"timestamp\":1558716040949000,\"duration\":618,"
            + "\"annotations\":[{\"timestamp\":1558716040949000,\"value\":\"lc\","
            + "\"endpoint\":{\"serviceName\":\"my-service\",\"ipv4\":\"10.193.122.103\"}}]}}";
    private static final String TRACE_FORMATTED = "[2019-05-24T16:40:40.950Z] traceId: 2250486695021e19 "
            + "id: c11b9a31555b7035 name: config-reload duration: 618 microseconds";

    private static final String NEWLINE = "\n";

    private WitchcraftLogSettings settings = WitchcraftLogSettings.builder()
            .showEventLogs(true)
            .showMetricLogs(true)
            .showRequestLogs(true)
            .showTraceLogs(true)
            .build();
    private final InputFilter filter = new WitchcraftLogFilter(WitchcraftLogFormatter.INSTANCE, () -> settings);

    @Test
    public void passesThroughNonWitchcraftLines() {
        assertThat(runFilter("foo"))
                .describedAs("unmatched lines should result in null, meaning no modification")
                .isNull();
    }

    @Test
    public void formatEvent() {
        assertThat(runFilter(EVENT_JSON)).isEqualTo(EVENT_FORMATTED);
    }

    @Test
    public void testEventContentType() {
        assertThat(runFilterWithType(EVENT_JSON))
                .extracting(pair -> pair.second)
                .containsOnly(WitchcraftConsoleViewContentTypes.EVENT_TYPE);
    }

    @Test
    public void formatServiceLine() {
        assertThat(runFilter(SERVICE_JSON)).isEqualTo(SERVICE_FORMATTED);
    }

    @Test
    public void formatServiceLine_slf4jInterpolation() {
        assertThat(runFilter("{\"type\":\"service.1\",\"level\":\"ERROR\","
                        + "\"time\":\"2019-05-09T15:32:37.692Z\",\"origin\":\"ROOT\","
                        + "\"thread\":\"main\",\"message\":\"Hello, {}!\","
                        + "\"params\":{},\"unsafeParams\":{\"0\": \"World\"},\"tags\":{}}"))
                .isEqualTo("ERROR [2019-05-09T15:32:37.692Z] ROOT: Hello, World! (0: World)");
    }

    @Test
    public void formatRequestLine() {
        assertThat(runFilter(REQUEST_JSON)).isEqualTo(REQUEST_FORMATTED);
    }

    @Test
    public void requestLogContentType() {
        assertThat(runFilterWithType(REQUEST_JSON))
                .extracting(pair -> pair.second)
                .containsOnly(WitchcraftConsoleViewContentTypes.REQUEST_TYPE);
    }

    @Test
    public void formatMetricLine() {
        assertThat(runFilter(METRIC_JSON)).isEqualTo(METRIC_FORMATTED);
    }

    @Test
    public void formatTrace() {
        assertThat(runFilter(TRACE_JSON)).isEqualTo(TRACE_FORMATTED);
    }

    @Test
    public void suppressTraceWhenDisabled() {
        settings = WitchcraftLogSettings.builder()
                .from(settings)
                .showTraceLogs(false)
                .build();

        assertThat(runFilter(TRACE_JSON + NEWLINE))
                .describedAs("Returns empty to suppress normal output")
                .isEmpty();
    }

    @Test
    public void suppressMultiline() {
        settings = WitchcraftLogSettings.builder()
                .from(settings)
                .showTraceLogs(false)
                .build();

        assertThat(runFilter(EVENT_JSON + NEWLINE + TRACE_JSON + NEWLINE + NEWLINE + SERVICE_JSON))
                .describedAs("Returns only unsuppressed logs, removes exactly one newline")
                .isEqualTo(EVENT_FORMATTED + NEWLINE + NEWLINE + SERVICE_FORMATTED);
    }

    @Test
    public void fastPredicateTest_nonWitchcraft() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("foobar")).isFalse();
    }

    @Test
    public void fastPredicateTest_newlineBrokenWitchcraftLog() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("{\n" + METRIC_JSON.substring(1)))
                .describedAs("Newlines are illegal in Witchcraft logs, don't attempt to match")
                .isFalse();
    }

    @Test
    public void fastPredicateTest_metric() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock(METRIC_JSON)).isTrue();
    }

    @Test
    public void fastPredicateTest_metricNewUnsupported() {
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("{\"type\":\"metric.5\","
                        + "\"time\":\"2019-05-24T16:40:52.162Z\","
                        + "\"metricName\":\"jvm.heap\",\"metricType\":\"gauge\",\"values\":{\"size\":66274352},"
                        + "\"tags\":{\"collection\":\"Metaspace\",\"collector\":\"PS Scavenge\","
                        + "\"when\":\"after\"},\"unsafeParams\":{}}"))
                .isFalse();
    }

    @Test
    public void fastPredicateTest_typeWithSpaces() {
        // space after '"type": ', slightly different from the default compact formatting
        assertThat(LogParser.anyPossibleWitchcraftLogsInBlock("{\"type\": \"metric.1\","
                        + "\"time\":\"2019-05-24T16:40:52.162Z\","
                        + "\"metricName\":\"jvm.heap\",\"metricType\":\"gauge\",\"values\":{\"size\":66274352},"
                        + "\"tags\":{\"collection\":\"Metaspace\",\"collector\":\"PS Scavenge\","
                        + "\"when\":\"after\"},\"unsafeParams\":{}}"))
                .isTrue();
    }

    @Test
    public void testMultiline() {
        assertThat(runFilter(METRIC_JSON + NEWLINE + METRIC_JSON))
                .isEqualTo(METRIC_FORMATTED + NEWLINE + METRIC_FORMATTED);
    }

    @Nullable
    private String runFilter(String input) {
        List<Pair<String, ConsoleViewContentType>> pairs = runFilterWithType(input);
        if (pairs == null) {
            return null;
        }

        return pairs.stream().map(pair -> pair.first).collect(Collectors.joining());
    }

    @Nullable
    private List<Pair<String, ConsoleViewContentType>> runFilterWithType(String input) {
        return filter.applyFilter(input, ConsoleViewContentType.NORMAL_OUTPUT);
    }
}

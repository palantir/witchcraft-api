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

import com.palantir.witchcraft.api.logging.MetricLogV1;
import org.junit.jupiter.api.Test;

class MetricLogFormatterTest {
    @Test
    void formats_correctly() {
        String formatted = MetricLogFormatter.format(MetricLogV1.builder()
                .type("metric.1")
                .time(TestData.XMAS_2019)
                .metricName("name")
                .metricType("type")
                .values("value", 3)
                .tags("tag", "foo")
                .unsafeParams("unsafe", "bad")
                .build());

        assertThat(formatted).isEqualTo("[2019-12-25T01:02:03Z] METRIC name type (value: 3) (tag: foo) (unsafe: bad)");
    }
}

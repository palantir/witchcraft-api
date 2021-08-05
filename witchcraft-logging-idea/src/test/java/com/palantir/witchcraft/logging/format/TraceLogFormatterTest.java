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

import com.palantir.conjure.java.lib.SafeLong;
import com.palantir.witchcraft.api.logging.Span;
import com.palantir.witchcraft.api.logging.TraceLogV1;
import org.junit.jupiter.api.Test;

class TraceLogFormatterTest {
    @Test
    void formats_correctly() {
        String formatted = TraceLogFormatter.format(TraceLogV1.builder()
                .type("trace.1")
                .time(TestData.XMAS_2019)
                .span(Span.builder()
                        .traceId("abdefghijklmno")
                        .id("id")
                        .name("name")
                        .timestamp(SafeLong.of(999))
                        .duration(SafeLong.of(31))
                        .build())
                .unsafeParams("unsafe", "bad")
                .build());

        assertThat(formatted)
                .isEqualTo(
                        "[2019-12-25T01:02:03Z] traceId: abdefghijklmno id: id name: name duration: 31 microseconds");
    }
}

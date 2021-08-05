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
import com.palantir.witchcraft.api.logging.RequestLogV2;
import org.junit.jupiter.api.Test;

class RequestLogFormatterTest {
    @Test
    void formats_correctly() {
        String formatted = RequestLogFormatter.format(RequestLogV2.builder()
                .type("request.1")
                .time(TestData.XMAS_2019)
                .protocol("http")
                .path("/some/path/{param}")
                .status(203)
                .requestSize(SafeLong.of(20))
                .responseSize(SafeLong.of(40))
                .duration(SafeLong.of(99))
                .method("GET")
                .params("param", "value")
                .build());

        assertThat(formatted).isEqualTo("[2019-12-25T01:02:03Z] \"GET /some/path/value http\" 203 40 99");
    }
}

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

import com.palantir.witchcraft.api.logging.EventLogV2;
import com.palantir.witchcraft.api.logging.MetricLogV1;
import com.palantir.witchcraft.api.logging.RequestLogV2;
import com.palantir.witchcraft.api.logging.ServiceLogV1;
import com.palantir.witchcraft.api.logging.TraceLogV1;
import java.util.Optional;

public enum LogFormatter implements LogVisitor<String> {
    INSTANCE;

    @Override
    public Optional<String> serviceV1(ServiceLogV1 serviceLogV1) {
        return Optional.of(ServiceLogFormatter.format(serviceLogV1));
    }

    @Override
    public Optional<String> requestV2(RequestLogV2 requestLogV2) {
        return Optional.of(RequestLogFormatter.format(requestLogV2));
    }

    @Override
    public Optional<String> eventV2(EventLogV2 eventLogV2) {
        return Optional.of(EventLogFormatter.format(eventLogV2));
    }

    @Override
    public Optional<String> metricV1(MetricLogV1 metricLogV1) {
        return Optional.of(MetricLogFormatter.format(metricLogV1));
    }

    @Override
    public Optional<String> traceV1(TraceLogV1 traceLogV1) {
        return Optional.of(TraceLogFormatter.format(traceLogV1));
    }
}

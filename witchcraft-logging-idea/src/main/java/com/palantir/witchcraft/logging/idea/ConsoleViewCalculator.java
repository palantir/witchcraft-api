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

import com.intellij.execution.ui.ConsoleViewContentType;
import com.palantir.witchcraft.api.logging.EventLogV2;
import com.palantir.witchcraft.api.logging.MetricLogV1;
import com.palantir.witchcraft.api.logging.RequestLogV2;
import com.palantir.witchcraft.api.logging.ServiceLogV1;
import com.palantir.witchcraft.api.logging.TraceLogV1;
import com.palantir.witchcraft.logging.format.LogVisitor;
import java.util.Optional;

enum ConsoleViewCalculator implements LogVisitor<ConsoleViewContentType> {
    INSTANCE;

    @Override
    public Optional<ConsoleViewContentType> serviceV1(ServiceLogV1 serviceLogV1) {
        return Optional.of(WitchcraftConsoleViewContentTypes.SERVICE_TYPES.getOrDefault(
                serviceLogV1.getLevel(), WitchcraftConsoleViewContentTypes.DEFAULT_TYPE));
    }

    @Override
    public Optional<ConsoleViewContentType> requestV2(RequestLogV2 _requestLogV2) {
        return Optional.of(WitchcraftConsoleViewContentTypes.REQUEST_TYPE);
    }

    @Override
    public Optional<ConsoleViewContentType> eventV2(EventLogV2 _eventLogV2) {
        return Optional.of(WitchcraftConsoleViewContentTypes.EVENT_TYPE);
    }

    @Override
    public Optional<ConsoleViewContentType> metricV1(MetricLogV1 _metricLogV1) {
        return Optional.of(WitchcraftConsoleViewContentTypes.METRIC_TYPE);
    }

    @Override
    public Optional<ConsoleViewContentType> traceV1(TraceLogV1 _traceLogV1) {
        return Optional.of(WitchcraftConsoleViewContentTypes.TRACE_TYPE);
    }
}

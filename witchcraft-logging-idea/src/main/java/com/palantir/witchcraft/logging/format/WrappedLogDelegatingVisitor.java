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

import com.palantir.witchcraft.api.logging.AuditLogV2;
import com.palantir.witchcraft.api.logging.DiagnosticLogV1;
import com.palantir.witchcraft.api.logging.EventLogV2;
import com.palantir.witchcraft.api.logging.MetricLogV1;
import com.palantir.witchcraft.api.logging.RequestLogV2;
import com.palantir.witchcraft.api.logging.ServiceLogV1;
import com.palantir.witchcraft.api.logging.TraceLogV1;
import com.palantir.witchcraft.api.logging.WrappedLogV1Payload;
import java.util.Optional;

final class WrappedLogDelegatingVisitor<T> implements WrappedLogV1Payload.Visitor<Optional<T>> {
    private final LogVisitor<T> logVisitor;

    WrappedLogDelegatingVisitor(LogVisitor<T> logVisitor) {
        this.logVisitor = logVisitor;
    }

    @Override
    public Optional<T> visitServiceLogV1(ServiceLogV1 value) {
        return logVisitor.serviceV1(value);
    }

    @Override
    public Optional<T> visitRequestLogV2(RequestLogV2 value) {
        return logVisitor.requestV2(value);
    }

    @Override
    public Optional<T> visitTraceLogV1(TraceLogV1 value) {
        return logVisitor.traceV1(value);
    }

    @Override
    public Optional<T> visitEventLogV2(EventLogV2 value) {
        return logVisitor.eventV2(value);
    }

    @Override
    public Optional<T> visitMetricLogV1(MetricLogV1 value) {
        return logVisitor.metricV1(value);
    }

    @Override
    public Optional<T> visitAuditLogV2(AuditLogV2 value) {
        return logVisitor.auditV2(value);
    }

    @Override
    public Optional<T> visitDiagnosticLogV1(DiagnosticLogV1 value) {
        return logVisitor.diagnosticV1(value);
    }

    @Override
    public Optional<T> visitUnknown(String _unknownType) {
        return Optional.empty();
    }
}

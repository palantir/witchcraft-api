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
import java.util.Optional;
import java.util.function.Supplier;

final class SupplierLogVisitor<T> implements LogVisitor<T> {
    private final Supplier<T> supplier;

    SupplierLogVisitor(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Optional<T> serviceV1(ServiceLogV1 _serviceLogV1) {
        return get();
    }

    @Override
    public Optional<T> requestV2(RequestLogV2 _requestLogV2) {
        return get();
    }

    @Override
    public Optional<T> eventV2(EventLogV2 _eventLogV2) {
        return get();
    }

    @Override
    public Optional<T> metricV1(MetricLogV1 _metricLogV1) {
        return get();
    }

    @Override
    public Optional<T> traceV1(TraceLogV1 _traceLogV1) {
        return get();
    }

    @Override
    public Optional<T> auditV2(AuditLogV2 _auditLogV2) {
        return get();
    }

    @Override
    public Optional<T> diagnosticV1(DiagnosticLogV1 _diagnosticLogV1) {
        return get();
    }

    private Optional<T> get() {
        return Optional.of(supplier.get());
    }
}

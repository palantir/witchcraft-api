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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface LogVisitor<T> {
    default Optional<T> serviceV1(ServiceLogV1 _serviceLogV1) {
        return defaultValue();
    }

    default Optional<T> requestV2(RequestLogV2 _requestLogV2) {
        return defaultValue();
    }

    default Optional<T> eventV2(EventLogV2 _eventLogV2) {
        return defaultValue();
    }

    default Optional<T> metricV1(MetricLogV1 _metricLogV1) {
        return defaultValue();
    }

    default Optional<T> traceV1(TraceLogV1 _traceLogV1) {
        return defaultValue();
    }

    default Optional<T> auditV2(AuditLogV2 _auditLogV2) {
        return defaultValue();
    }

    default Optional<T> diagnosticV1(DiagnosticLogV1 _diagnosticLogV1) {
        return defaultValue();
    }

    default Optional<T> defaultValue() {
        return Optional.empty();
    }

    /**
     * Combine this {@link LogVisitor} with another {@link LogVisitor} to produce a composite short-circuiting
     * {@link LogVisitor}.
     *
     * <p/><em>If the {@code otherLogVisitor} doesn't return a value, then this composite doesn't return a
     * value either.</em>
     *
     * <p/>For each log given to this composite, this log visitor is visited and if it returns a value the second log
     * visitor is visited, and then the value from each is combined using the given function.
     */
    default <U, R> LogVisitor<R> combineWith(LogVisitor<U> otherLogVisitor, BiFunction<T, U, R> combiner) {
        return new CombineWithLogVisitor<>(this, otherLogVisitor, combiner);
    }

    /**
     * Combine this {@link LogVisitor} with another {@link LogVisitor} to produce a composite {@link LogVisitor}.
     *
     * <p/><em>If the {@code otherLogVisitor} doesn't return a value, then this composite behaves the same as the
     * original visitor.</em>
     *
     * <p/>For each log given to this composite, this log visitor is visited and if it returns a value the second log
     * visitor is visited, and then the value from each is handed off to the given {@code effect} consumer.
     */
    default <U> LogVisitor<T> combineWithEffect(LogVisitor<U> otherLogVisitor, BiConsumer<T, U> effect) {
        return new CombineWithLogVisitor<>(this, LogVisitors.liftOptional(otherLogVisitor), (original, maybeOther) -> {
            maybeOther.ifPresent(other -> effect.accept(original, other));
            return original;
        });
    }

    /**
     * Produce a {@link LogVisitor} which for every log type just returns a value from the supplier.
     */
    static <T> LogVisitor<T> fromSupplier(Supplier<T> supplier) {
        return new SupplierLogVisitor<>(supplier);
    }
}

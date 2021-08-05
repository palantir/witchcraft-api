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

import java.lang.reflect.Proxy;
import java.util.Optional;

final class LogVisitors {

    /**
     * A log visitor that always returns <em>something</em>, though that something is now an Optional indicating
     * whether the given {@code visitor} returned something or not.
     */
    @SuppressWarnings("unchecked")
    static <T> LogVisitor<Optional<T>> liftOptional(LogVisitor<T> visitor) {
        return (LogVisitor<Optional<T>>) Proxy.newProxyInstance(
                visitor.getClass().getClassLoader(), new Class[] {LogVisitor.class}, (_proxy, method, args) -> {
                    if (method.getReturnType() == Optional.class) {
                        return Optional.of(method.invoke(visitor, args));
                    }
                    return method.invoke(visitor, args);
                });
    }

    private LogVisitors() {}
}

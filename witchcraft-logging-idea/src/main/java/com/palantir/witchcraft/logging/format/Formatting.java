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

import com.google.common.base.CharMatcher;
import java.util.Map;
import java.util.function.Consumer;

/** Utility functionality shared between {@link LogFormatter} implementations. */
final class Formatting {

    static final CharMatcher NEWLINE_MATCHER = CharMatcher.is('\n');

    private static final ThreadLocal<StringBuilder> REUSABLE_STRING_BUILDER =
            ThreadLocal.withInitial(() -> new StringBuilder(1024));

    static void niceMap(Map<String, ?> params, StringBuilder sb) {
        sb.append('(');
        formatParamsTo(params, sb);
        if (!params.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(')');
    }

    static void formatParamsTo(Map<String, ?> params, StringBuilder sb) {
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sb.append(key).append(": ").append(safeString(value)).append(", ");
        }
    }

    static String safeString(Object value) {
        try {
            return String.valueOf(value);
        } catch (RuntimeException e) {
            // Fallback if toString throws
            return value.getClass().getSimpleName() + '@' + System.identityHashCode(value);
        }
    }

    static String withStringBuilder(Consumer<StringBuilder> function) {
        StringBuilder builder = REUSABLE_STRING_BUILDER.get();
        builder.setLength(0);
        function.accept(builder);
        String result = builder.toString();
        if (builder.length() > 1024 * 16) {
            // Buffer has grown too large, allow the instance to be collected
            REUSABLE_STRING_BUILDER.remove();
        } else {
            builder.setLength(0);
        }
        return result;
    }

    private Formatting() {}
}

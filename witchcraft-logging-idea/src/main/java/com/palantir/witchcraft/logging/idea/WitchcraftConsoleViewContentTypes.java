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

import com.google.common.collect.ImmutableMap;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Pair;
import com.palantir.witchcraft.api.logging.LogLevel;
import java.awt.Color;
import java.awt.Font;

/** {@link ConsoleViewContentType} definitions used by this plugin. */
final class WitchcraftConsoleViewContentTypes {

    static final ConsoleViewContentType DEFAULT_TYPE =
            createViewType("WITCHCRAFT_DEFAULT", ConsoleViewContentType.NORMAL_OUTPUT_KEY);
    static final ImmutableMap<LogLevel, ConsoleViewContentType> SERVICE_TYPES =
            ImmutableMap.<LogLevel, ConsoleViewContentType>builder()
                    .put(LogLevel.FATAL, createViewType("WITCHCRAFT_SERVICE_FATAL", Color.RED))
                    .put(LogLevel.ERROR, createViewType("WITCHCRAFT_SERVICE_ERROR", Color.RED))
                    .put(LogLevel.WARN, createViewType("WITCHCRAFT_SERVICE_WARN", Color.YELLOW))
                    .put(
                            LogLevel.INFO,
                            createViewType("WITCHCRAFT_SERVICE_INFO", ConsoleViewContentType.NORMAL_OUTPUT_KEY))
                    .put(LogLevel.DEBUG, createViewType("WITCHCRAFT_SERVICE_DEBUG", Color.CYAN))
                    .put(LogLevel.TRACE, createViewType("WITCHCRAFT_SERVICE_TRACE", Color.CYAN))
                    .build();
    static final ConsoleViewContentType EVENT_TYPE = createViewType("WITCHCRAFT_EVENT", Color.WHITE);
    static final ConsoleViewContentType METRIC_TYPE = createViewType("WITCHCRAFT_METRIC", Color.DARK_GRAY);
    static final ConsoleViewContentType REQUEST_TYPE = createViewType("WITCHCRAFT_REQUEST", Color.WHITE);
    static final ConsoleViewContentType TRACE_TYPE = createViewType("WITCHCRAFT_TRACE", Color.WHITE);
    // Marker content type for newlines applied by the formatter
    static final ConsoleViewContentType NEWLINE_TYPE = createViewType("WITCHCRAFT_NEWLINE", Color.DARK_GRAY);
    static final Pair<String, ConsoleViewContentType> NEWLINE = Pair.create("\n", NEWLINE_TYPE);

    private WitchcraftConsoleViewContentTypes() {}

    private static ConsoleViewContentType createViewType(String name, Color color) {
        return createViewType(
                name,
                TextAttributesKey.createTempTextAttributesKey(
                        name, new TextAttributes(color, null, null, null, Font.PLAIN)));
    }

    private static ConsoleViewContentType createViewType(String name, TextAttributesKey fallback) {
        return new ConsoleViewContentType(name, TextAttributesKey.createTextAttributesKey(name, fallback));
    }
}

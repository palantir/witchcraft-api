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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Pair;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class WitchcraftLogFilter implements InputFilter {

    private final InputFilter delegate;
    private final ImmutableMap<ConsoleViewContentType, BooleanSupplier> displayFilter;

    WitchcraftLogFilter(InputFilter delegate, Supplier<WitchcraftLogSettings> settings) {
        this.delegate = delegate;
        this.displayFilter = ImmutableMap.of(
                WitchcraftConsoleViewContentTypes.EVENT_TYPE,
                        () -> settings.get().getShowEventLogs(),
                WitchcraftConsoleViewContentTypes.METRIC_TYPE,
                        () -> settings.get().getShowMetricLogs(),
                WitchcraftConsoleViewContentTypes.REQUEST_TYPE,
                        () -> settings.get().getShowRequestLogs(),
                WitchcraftConsoleViewContentTypes.TRACE_TYPE,
                        () -> settings.get().getShowTraceLogs());
    }

    @Nullable
    @Override
    public List<Pair<String, ConsoleViewContentType>> applyFilter(
            @NotNull String text, @NotNull ConsoleViewContentType contentType) {
        List<Pair<String, ConsoleViewContentType>> delegateResult = delegate.applyFilter(text, contentType);
        if (delegateResult == null || !containsWitchcraftData(delegateResult)) {
            return delegateResult;
        }
        // When multiple lines are returned, the result may include a Pair representing a newline after a parsed
        // Witchcraft line. If we filter an event, we must also filter the trailing newline character.
        boolean removeNextLineIfNewline = false;
        ImmutableList.Builder<Pair<String, ConsoleViewContentType>> result =
                ImmutableList.builderWithExpectedSize(delegateResult.size());
        for (Pair<String, ConsoleViewContentType> item : delegateResult) {
            if (removeNextLineIfNewline) {
                removeNextLineIfNewline = false;
                if (WitchcraftConsoleViewContentTypes.NEWLINE.equals(item)) {
                    continue;
                }
            }
            if (displayFilter.getOrDefault(item.getSecond(), () -> true).getAsBoolean()) {
                result.add(item);
            } else {
                // When results are filtered, we must also remove the associated newline.
                removeNextLineIfNewline = true;
            }
        }
        return result.build();
    }

    private static boolean containsWitchcraftData(List<Pair<String, ConsoleViewContentType>> lines) {
        for (Pair<String, ConsoleViewContentType> item : lines) {
            // The null check is likely unnecessarily defensive, the goal is to avoid breaking any non-witchcraft
            // application, which would degrade trust in the plugin. I'm less worried about breaking witchcraft
            // applications because there's a direct relationship to the plugin, and would be less frustrating
            // to debug.
            if (item != null && item.getSecond().toString().startsWith("WITCHCRAFT_")) {
                return true;
            }
        }
        return false;
    }
}

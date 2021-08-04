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

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/** Intellij plugin implementation. */
public final class WitchcraftLogFormatFilterProvider implements ConsoleInputFilterProvider {
    @NotNull
    @Override
    public InputFilter[] getDefaultFilters(@NotNull Project project) {
        WitchcraftLogSettingsManager settingsManager = WitchcraftLogSettingsManager.getInstance(project);
        return new InputFilter[] {new WitchcraftLogFilter(WitchcraftLogFormatter.INSTANCE, settingsManager::getSettings)
        };
    }
}

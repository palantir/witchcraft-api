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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import org.jetbrains.annotations.NotNull;

@State(name = "WitchcraftLogSettings")
final class WitchcraftLogSettingsManagerImpl
        implements WitchcraftLogSettingsManager, PersistentStateComponent<ModifiableWitchcraftLogSettings> {
    private static final WitchcraftLogSettings DEFAULT_SETTINGS =
            WitchcraftLogSettings.builder().build();
    private volatile WitchcraftLogSettings settings = DEFAULT_SETTINGS;

    @Override
    public WitchcraftLogSettings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(WitchcraftLogSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public ModifiableWitchcraftLogSettings getState() {
        return ModifiableWitchcraftLogSettings.create().from(settings);
    }

    @Override
    public void loadState(@NotNull ModifiableWitchcraftLogSettings state) {
        this.settings = WitchcraftLogSettings.builder().from(state).build();
    }
}

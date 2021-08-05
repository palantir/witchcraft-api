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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nls;

public final class WitchcraftLogSettingsPanel implements Configurable {
    private JCheckBox showEventLogsCheckBox;
    private JCheckBox showRequestLogsCheckBox;
    private JCheckBox showTraceLogsCheckBox;
    private JCheckBox showMetricLogsCheckBox;
    private JPanel panel;

    private final WitchcraftLogSettingsManager settingsManager;

    public WitchcraftLogSettingsPanel(Project project) {
        this(WitchcraftLogSettingsManager.getInstance(project));
    }

    public WitchcraftLogSettingsPanel(WitchcraftLogSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Witchcraft Log Display";
    }

    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return !deriveSettings().equals(settingsManager.getSettings());
    }

    private WitchcraftLogSettings deriveSettings() {
        return WitchcraftLogSettings.builder()
                .showEventLogs(showEventLogsCheckBox.isSelected())
                .showRequestLogs(showRequestLogsCheckBox.isSelected())
                .showTraceLogs(showTraceLogsCheckBox.isSelected())
                .showMetricLogs(showMetricLogsCheckBox.isSelected())
                .build();
    }

    @Override
    public void apply() {
        settingsManager.setSettings(deriveSettings());
    }

    @Override
    public void reset() {
        WitchcraftLogSettings settings = settingsManager.getSettings();
        showEventLogsCheckBox.setSelected(settings.getShowEventLogs());
        showRequestLogsCheckBox.setSelected(settings.getShowRequestLogs());
        showTraceLogsCheckBox.setSelected(settings.getShowTraceLogs());
        showMetricLogsCheckBox.setSelected(settings.getShowMetricLogs());
    }
}

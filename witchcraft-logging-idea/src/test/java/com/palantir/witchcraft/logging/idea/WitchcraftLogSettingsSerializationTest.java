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

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.util.xmlb.XmlSerializer;
import java.util.Map;
import java.util.stream.Collectors;
import org.jdom.Attribute;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

public class WitchcraftLogSettingsSerializationTest {
    @Test
    public void testSerialize() {
        Element element = XmlSerializer.serialize(ModifiableWitchcraftLogSettings.create());
        assertThat(element.getName()).isEqualTo("WitchcraftLogSettings");

        assertThat(element.getChildren()).allSatisfy(child -> {
            assertThat(child.getName()).isEqualTo("option");
            assertThat(child.getChildren()).isEmpty();
            assertThat(child.getAttributes()).extracting(Attribute::getName).containsExactlyInAnyOrder("name", "value");
        });

        assertThat(getOptions(element))
                .hasSize(4)
                .containsEntry("showEventLogs", "true")
                .containsEntry("showMetricLogs", "false")
                .containsEntry("showRequestLogs", "true")
                .containsEntry("showTraceLogs", "true");
    }

    @Test
    public void testDeserializeEmptyUsesDefaults() {
        ModifiableWitchcraftLogSettings defaults = ModifiableWitchcraftLogSettings.create();

        Element element = new Element("WitchcraftLogSettings");
        ModifiableWitchcraftLogSettings settings =
                XmlSerializer.deserialize(element, ModifiableWitchcraftLogSettings.class);
        assertThat(settings).isEqualTo(defaults);
    }

    @Test
    public void testDeserializeOverridesDefaults() {
        ModifiableWitchcraftLogSettings nonDefaults = ModifiableWitchcraftLogSettings.create();
        nonDefaults.setShowEventLogs(!nonDefaults.getShowEventLogs());

        Element element = new Element("WitchcraftLogSettings");
        setOption(element, "showEventLogs", Boolean.toString(nonDefaults.getShowEventLogs()));
        ModifiableWitchcraftLogSettings settings =
                XmlSerializer.deserialize(element, ModifiableWitchcraftLogSettings.class);
        assertThat(settings).isEqualTo(nonDefaults);
    }

    private static Map<String, String> getOptions(Element element) {
        return element.getChildren("option").stream()
                .collect(Collectors.toMap(
                        option -> option.getAttributeValue("name"), option -> option.getAttributeValue("value")));
    }

    private static void setOption(Element element, String name, String value) {
        Element option = new Element("option");
        option.setAttribute("name", name);
        option.setAttribute("value", value);
        element.addContent(option);
    }
}

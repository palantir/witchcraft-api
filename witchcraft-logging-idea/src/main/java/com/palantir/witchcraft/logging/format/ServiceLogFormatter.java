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

import com.google.common.base.Strings;
import com.palantir.witchcraft.api.logging.ServiceLogV1;
import java.time.format.DateTimeFormatter;
import org.slf4j.helpers.MessageFormatter;

final class ServiceLogFormatter {
    private ServiceLogFormatter() {}

    static String format(ServiceLogV1 service) {
        return Formatting.withStringBuilder(buffer -> {
            buffer.append(service.getLevel());
            while (buffer.length() < 6) {
                buffer.append(' ');
            }
            buffer.append('[');
            DateTimeFormatter.ISO_INSTANT.formatTo(service.getTime(), buffer);
            buffer.append("] ")
                    .append(service.getOrigin().orElse("<nil>"))
                    .append(": ")
                    .append(getMessage(service));
            if (!service.getParams().isEmpty() || !service.getUnsafeParams().isEmpty()) {
                buffer.append(" (");
                Formatting.formatParamsTo(service.getParams(), buffer);
                Formatting.formatParamsTo(service.getUnsafeParams(), buffer);
                // Reset trailing separator
                buffer.setLength(buffer.length() - 2);
                buffer.append(')');
            }
            service.getStacktrace()
                    .map(input -> Strings.emptyToNull(Formatting.NEWLINE_MATCHER.trimFrom(input)))
                    .ifPresent(stackTrace -> buffer.append('\n').append(stackTrace));
        });
    }

    private static String getMessage(ServiceLogV1 service) {
        String formatString = service.getMessage();
        // If placeholders are found, attempt slf4j-style interpolation
        int placeholders = countPlaceholders(formatString);
        if (placeholders > 0) {
            Object[] parameters = new Object[placeholders];
            for (int i = 0; i < placeholders; i++) {
                parameters[i] = service.getUnsafeParams()
                        // Use the placeholder string by default to avoid modifying non-existent parameters.
                        // This can occur if only some parameters are wrapped with log-safe args.
                        .getOrDefault("" + i, "{}");
            }
            // Use the slf4j provided utility directly
            return MessageFormatter.arrayFormat(formatString, parameters).getMessage();
        }
        return formatString;
    }

    private static int countPlaceholders(String formatString) {
        int count = 0;
        for (int i = 1; i < formatString.length(); i++) {
            if (formatString.charAt(i - 1) == '{' && formatString.charAt(i) == '}') {
                count++;
            }
        }
        return count;
    }
}

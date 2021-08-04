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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Pair;
import com.palantir.witchcraft.logging.format.LogFormatter;
import com.palantir.witchcraft.logging.format.LogParser;
import java.util.Iterator;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

enum WitchcraftLogFormatter implements InputFilter {
    INSTANCE;

    private static final LogParser<Pair<String, ConsoleViewContentType>> LOG_PARSER =
            new LogParser<>(LogFormatter.INSTANCE.combineWith(ConsoleViewCalculator.INSTANCE, Pair::createNonNull));

    private static final Splitter SPLITTER = Splitter.on('\n');

    @Override
    public ImmutableList<Pair<String, ConsoleViewContentType>> applyFilter(
            @NotNull String text, @NotNull ConsoleViewContentType contentType) {
        if (!LogParser.anyPossibleWitchcraftLogsInBlock(text)) {
            // Return fast if there are no witchcraft logs
            return null;
        }

        boolean witchcraftLogParsed = false;
        ImmutableList.Builder<Pair<String, ConsoleViewContentType>> result = ImmutableList.builder();

        Iterator<String> lineIterator = SPLITTER.split(text).iterator();
        while (lineIterator.hasNext()) {
            String line = lineIterator.next();
            Optional<Pair<String, ConsoleViewContentType>> maybeParsed = LOG_PARSER.tryParse(line);

            if (maybeParsed.isPresent()) {
                witchcraftLogParsed = true;
                result.add(maybeParsed.get());
            } else {
                result.add(Pair.createNonNull(line, contentType));
            }

            // If this is not the last piece, add a newline
            if (lineIterator.hasNext()) {
                result.add(WitchcraftConsoleViewContentTypes.NEWLINE);
            }
        }

        if (!witchcraftLogParsed) {
            // When nothing has been decoded, return null based on InputFilter.applyFilter documentation
            return null;
        }

        return result.build();
    }
}

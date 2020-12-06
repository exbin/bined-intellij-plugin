/*
 * Copyright (C) ExBin Project
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
package org.exbin.xbup.core.util;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utilities for stream data manipulations.
 *
 * @version 0.2.1 2020/08/17
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class StringUtils {

    public static final String ENCODING_UTF8 = "UTF-8";

    private StringUtils() {
    }

    /**
     * Compares two strings including nulls.
     *
     * @param string first string
     * @param matchString second string
     * @return true if both null or equals
     */
    public static boolean stringsEquals(@Nullable String string, @Nullable String matchString) {
        if (string == null) {
            return matchString == null;
        }

        return string.equals(matchString);
    }

    /**
     * Compares two strings ignoring case including nulls.
     *
     * @param string first string
     * @param matchString second string
     * @return true if both null or equals ignoring case sensitivity
     */
    public static boolean stringsEqualsIgnoreCase(@Nullable String string, @Nullable String matchString) {
        if (string == null) {
            return matchString == null;
        }

        return string.equalsIgnoreCase(matchString);
    }
}

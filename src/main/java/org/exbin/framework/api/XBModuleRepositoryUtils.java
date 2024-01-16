/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.framework.api;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * XBUP framework modules repository utilities class.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class XBModuleRepositoryUtils {

    private static final String API_CLASS_EXT = "Api";
    private static final String API_PACKAGE_EXT = ".api";

    /**
     * Returns module identifier for class representation.
     *
     * @param moduleClass module class
     * @return string ID
     */
    @Nonnull
    public static String getModuleIdByApi(Class moduleClass) {
        String packageName = moduleClass.getPackage().getName();
        String className = moduleClass.getSimpleName();

        return (packageName.endsWith(API_PACKAGE_EXT) ? packageName.substring(0, packageName.length() - 3) : packageName + ".")
                + (className.endsWith(API_CLASS_EXT) ? className.substring(0, className.length() - 3) : className);
    }
}

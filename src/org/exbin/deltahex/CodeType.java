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
package org.exbin.deltahex;

/**
 * Enumeration of supported code types.
 *
 * @version 0.1.1 2016/08/31
 * @author ExBin Project (http://exbin.org)
 */
public enum CodeType {

    BINARY(8), OCTAL(3), DECIMAL(3), HEXADECIMAL(2);

    private final int maxDigits;

    private CodeType(int maxDigits) {
        this.maxDigits = maxDigits;
    }

    /**
     * Maximum number of digits per single byte.
     *
     * @return number of digits
     */
    public int getMaxDigits() {
        return maxDigits;
    }
}

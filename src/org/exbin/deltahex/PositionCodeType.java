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
 * Enumeration of supported position code types.
 *
 * @version 0.1.1 2016/08/31
 * @author ExBin Project (http://exbin.org)
 */
public enum PositionCodeType {

    OCTAL(8), DECIMAL(10), HEXADECIMAL(16);

    private final int base;
    private final double baseLog;

    private PositionCodeType(int base) {
        this.base = base;
        baseLog = Math.log(base);
    }

    public int getBase() {
        return base;
    }

    public double getBaseLog() {
        return baseLog;
    }
}

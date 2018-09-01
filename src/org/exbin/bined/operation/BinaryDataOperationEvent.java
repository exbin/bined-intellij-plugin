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
package org.exbin.bined.operation;

import javax.annotation.Nonnull;

/**
 * Operation execution event.
 *
 * @version 0.2.0 2018/02/13
 * @author ExBin Project (https://exbin.org)
 */
public class BinaryDataOperationEvent {

    @Nonnull
    private BinaryDataOperation operation;

    public BinaryDataOperationEvent(@Nonnull BinaryDataOperation operation) {
        this.operation = operation;
    }

    @Nonnull
    public BinaryDataOperation getOperation() {
        return operation;
    }

    @Nonnull
    public void setOperation(BinaryDataOperation operation) {
        this.operation = operation;
    }
}

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
package org.exbin.framework.bined.macro.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Macro record.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class MacroRecord {

    private String name;
    private final List<String> steps = new ArrayList<>();

    public MacroRecord() {
        name = "";
    }

    public MacroRecord(String name) {
        this.name = name;
    }

    /**
     * Copy constructor.
     *
     * @param record record
     */
    public MacroRecord(MacroRecord record) {
        MacroRecord.this.setRecord(record);
    }

    public void setRecord(MacroRecord record) {
        if (record != this) {
            name = record.name;
            steps.clear();
            steps.addAll(record.steps);
        }
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nonnull
    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        if (steps != this.steps) {
            this.steps.clear();
            this.steps.addAll(steps);
        }
    }

    public void setStep(int index, String step) {
        steps.set(index, step);
    }

    public void addStep(String step) {
        steps.add(step);
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    @Nonnull
    public Optional<String> getLastStep() {
        if (steps.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(steps.get(steps.size() - 1));
    }
}

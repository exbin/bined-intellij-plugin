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
package org.exbin.deltahex.swing;

import java.awt.event.KeyEvent;

/**
 * Interface for code area data manipulation.
 *
 * @version 0.1.2 2016/12/19
 * @author ExBin Project (http://exbin.org)
 */
public interface CodeAreaCommandHandler {

    /**
     * Notifies command handler about end of sequence appendable commands.
     */
    void sequenceBreak();

    /**
     * Keyboard key was pressed.
     *
     * @param keyEvent key event
     */
    void keyPressed(KeyEvent keyEvent);

    /**
     * Keyboard key was typed.
     *
     * @param keyEvent key event
     */
    void keyTyped(KeyEvent keyEvent);

    /**
     * Backspace key was pressed.
     */
    void backSpacePressed();

    /**
     * Delete key was pressed.
     */
    void deletePressed();

    /**
     * Deletes selection.
     */
    void delete();

    /**
     * Copies selection to clipboard.
     */
    void copy();

    /**
     * Copies selection to clipboard as code string.
     */
    void copyAsCode();

    /**
     * Cuts selection to clipboard.
     */
    void cut();

    /**
     * Pastes content of clipboard to cursor area.
     */
    void paste();

    /**
     * Pastes content of clipboard to cursor area analyzing string code.
     */
    void pasteFromCode();

    /**
     * Returns true if paste action is possible.
     *
     * @return true if paste is possible
     */
    boolean canPaste();
}

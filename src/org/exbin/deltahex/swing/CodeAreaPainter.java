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

import java.awt.Graphics;

/**
 * Hexadecimal editor painter.
 *
 * @version 0.1.1 2016/09/02
 * @author ExBin Project (http://exbin.org)
 */
public interface CodeAreaPainter {

    /**
     * Paints overall hexadecimal editor parts.
     *
     * @param g graphics
     */
    void paintOverall(Graphics g);

    /**
     * Paints header for hexadecimal editor.
     *
     * @param g graphics
     */
    void paintHeader(Graphics g);

    /**
     * Paints background.
     *
     * @param g graphics
     */
    void paintBackground(Graphics g);

    /**
     * Paints line number.
     *
     * @param g graphics
     */
    void paintLineNumbers(Graphics g);

    /**
     * Paints main hexadecimal data section of the component.
     *
     * @param g graphics
     */
    void paintMainArea(Graphics g);

    /**
     * Paints cursor symbol.
     *
     * @param g graphics
     */
    void paintCursor(Graphics g);
}

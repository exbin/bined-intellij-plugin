/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.framework.editor.text.service.impl;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JTextArea;
import org.exbin.framework.editor.text.service.*;

/**
 * Word wrapping options interface.
 *
 * @version 0.2.1 2019/07/17
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class TextServiceImpl implements TextSearchService {

    @Nullable
    @Override
    public FoundMatch findText(JTextArea textArea, FindTextParameters findTextParameters) {
        String text = textArea.getText();
        int pos = findTextParameters.getStartFrom();
        String findText = findTextParameters.getFindText();
        pos = text.indexOf(findText, pos);
        if (pos >= 0) {
            int toPos;
            if (findTextParameters.isShallReplace()) {
                String replaceText = findTextParameters.getReplaceText();
                textArea.replaceRange(replaceText, pos, pos + findText.length());
                toPos = pos + replaceText.length();
            } else {
                toPos = pos + findText.length();
            }
            return new FoundMatch(pos, toPos);
        }

        return null;
    }
}

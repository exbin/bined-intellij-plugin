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
package org.exbin.framework.bined.options;

import java.awt.Font;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable.RowWrappingMode;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.extended.capability.PositionCodeTypeCapable;
import org.exbin.bined.extended.capability.ShowUnprintablesCapable;
import org.exbin.bined.highlight.swing.extended.ExtendedHighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.preferences.CodeAreaPreferences;
import org.exbin.framework.gui.options.api.OptionsData;

/**
 * Code area options.
 *
 * @version 0.2.1 2019/07/20
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaOptions implements OptionsData {

    private Font codeFont = null;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private boolean showUnprintables = true;
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    private PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    private boolean codeColorization = true;
    private boolean useDefaultFont = true;
    private RowWrappingMode rowWrappingMode;
    private int maxBytesPerRow;
    private int minRowPositionLength;
    private int maxRowPositionLength;

    public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    @Nonnull
    public Font getCodeFont() {
        return codeFont;
    }

    public void setCodeFont(Font codeFont) {
        this.codeFont = codeFont;
    }

    @Nonnull
    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
    }

    public boolean isShowUnprintables() {
        return showUnprintables;
    }

    public void setShowUnprintables(boolean showUnprintables) {
        this.showUnprintables = showUnprintables;
    }

    @Nonnull
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
    }

    @Nonnull
    public PositionCodeType getPositionCodeType() {
        return positionCodeType;
    }

    public void setPositionCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = positionCodeType;
    }

    @Nonnull
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(CodeAreaViewMode viewMode) {
        this.viewMode = viewMode;
    }

    public boolean isCodeColorization() {
        return codeColorization;
    }

    public void setCodeColorization(boolean codeColorization) {
        this.codeColorization = codeColorization;
    }

    public boolean isUseDefaultFont() {
        return useDefaultFont;
    }

    public void setUseDefaultFont(boolean useDefaultFont) {
        this.useDefaultFont = useDefaultFont;
    }

    @Nonnull
    public RowWrappingMode getRowWrappingMode() {
        return rowWrappingMode;
    }

    public void setRowWrappingMode(RowWrappingMode rowWrappingMode) {
        this.rowWrappingMode = rowWrappingMode;
    }

    public int getMaxBytesPerRow() {
        return maxBytesPerRow;
    }

    public void setMaxBytesPerRow(int maxBytesPerRow) {
        this.maxBytesPerRow = maxBytesPerRow;
    }

    public int getMinRowPositionLength() {
        return minRowPositionLength;
    }

    public void setMinRowPositionLength(int minRowPositionLength) {
        this.minRowPositionLength = minRowPositionLength;
    }

    public int getMaxRowPositionLength() {
        return maxRowPositionLength;
    }

    public void setMaxRowPositionLength(int maxRowPositionLength) {
        this.maxRowPositionLength = maxRowPositionLength;
    }

    public void loadFromParameters(CodeAreaPreferences preferences) {
        codeFont = preferences.getCodeFont(DEFAULT_FONT);
        codeType = preferences.getCodeType();
        showUnprintables = preferences.isShowNonprintables();
        codeCharactersCase = preferences.getCodeCharactersCase();
        positionCodeType = preferences.getPositionCodeType();
        viewMode = preferences.getViewMode();
        codeColorization = preferences.isCodeColorization();
        useDefaultFont = preferences.isUseDefaultFont();
        rowWrappingMode = preferences.getRowWrappingMode();
        maxBytesPerRow = preferences.getMaxBytesPerRow();
        minRowPositionLength = preferences.getMinRowPositionLength();
        maxRowPositionLength = preferences.getMaxRowPositionLength();
    }

    public void saveToParameters(CodeAreaPreferences preferences) {
        preferences.setCodeFont(codeFont);
        preferences.setCodeType(codeType);
        preferences.setShowUnprintables(showUnprintables);
        preferences.setCodeCharactersCase(codeCharactersCase);
        preferences.setPositionCodeType(positionCodeType);
        preferences.setViewMode(viewMode);
        preferences.setCodeColorization(codeColorization);
        preferences.setUseDefaultFont(useDefaultFont);
        preferences.setRowWrappingMode(rowWrappingMode);
        preferences.setMaxBytesPerRow(maxBytesPerRow);
        preferences.setMinRowPositionLength(minRowPositionLength);
        preferences.setMaxRowPositionLength(maxRowPositionLength);
    }

    public void applyFromCodeArea(ExtCodeArea codeArea) {
        codeFont = ((FontCapable) codeArea).getCodeFont();
        codeType = ((CodeTypeCapable) codeArea).getCodeType();
        showUnprintables = ((ShowUnprintablesCapable) codeArea).isShowUnprintables();
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        positionCodeType = ((PositionCodeTypeCapable) codeArea).getPositionCodeType();
        viewMode = ((ViewModeCapable) codeArea).getViewMode();
        codeColorization = ((ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter()).isNonAsciiHighlightingEnabled();
        rowWrappingMode = codeArea.getRowWrapping();
        maxBytesPerRow = codeArea.getMaxBytesPerRow();
        minRowPositionLength = codeArea.getMinRowPositionLength();
        maxRowPositionLength = codeArea.getMaxRowPositionLength();
    }

    public void applyToCodeArea(ExtCodeArea codeArea) {
        ((FontCapable) codeArea).setCodeFont(useDefaultFont ? DEFAULT_FONT : codeFont);
        ((CodeTypeCapable) codeArea).setCodeType(codeType);
        ((ShowUnprintablesCapable) codeArea).setShowUnprintables(showUnprintables);
        ((CodeCharactersCaseCapable) codeArea).setCodeCharactersCase(codeCharactersCase);
        ((PositionCodeTypeCapable) codeArea).setPositionCodeType(positionCodeType);
        ((ViewModeCapable) codeArea).setViewMode(viewMode);
        ((ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter()).setNonAsciiHighlightingEnabled(codeColorization);
        codeArea.setRowWrapping(rowWrappingMode);
        codeArea.setMaxBytesPerRow(maxBytesPerRow);
        codeArea.setMinRowPositionLength(minRowPositionLength);
        codeArea.setMaxRowPositionLength(maxRowPositionLength);
    }

    public void setOptions(CodeAreaOptions codeAreaOptions) {
        codeFont = codeAreaOptions.codeFont;
        codeType = codeAreaOptions.codeType;
        showUnprintables = codeAreaOptions.showUnprintables;
        codeCharactersCase = codeAreaOptions.codeCharactersCase;
        positionCodeType = codeAreaOptions.positionCodeType;
        viewMode = codeAreaOptions.viewMode;
        codeColorization = codeAreaOptions.codeColorization;
        useDefaultFont = codeAreaOptions.useDefaultFont;
        rowWrappingMode = codeAreaOptions.rowWrappingMode;
        maxBytesPerRow = codeAreaOptions.maxBytesPerRow;
        minRowPositionLength = codeAreaOptions.minRowPositionLength;
        maxRowPositionLength = codeAreaOptions.maxRowPositionLength;
    }
}

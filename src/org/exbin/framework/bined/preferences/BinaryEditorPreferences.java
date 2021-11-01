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
package org.exbin.framework.bined.preferences;

import org.exbin.framework.api.Preferences;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.swing.extended.layout.DefaultExtendedCodeAreaLayoutProfile;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaDecorations;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.editor.text.preferences.TextEncodingPreferences;
import org.exbin.framework.editor.text.preferences.TextFontPreferences;
import org.exbin.xbup.core.util.StringUtils;

/**
 * Binary editor preferences.
 *
 * @version 0.2.1 2019/07/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryEditorPreferences {

    private final static String PREFERENCES_VERSION = "version";
    private final static String PREFERENCES_VERSION_VALUE = "0.2.1";

    private final Preferences preferences;

    private final EditorPreferences editorPreferences;
    private final StatusPreferences statusPreferences;
    private final CodeAreaPreferences codeAreaPreferences;
    private final TextEncodingPreferences encodingPreferences;
    private final TextFontPreferences fontPreferences;
    private final CodeAreaLayoutPreferences layoutPreferences;
    private final CodeAreaThemePreferences themePreferences;
    private final CodeAreaColorPreferences colorPreferences;

    public BinaryEditorPreferences(Preferences preferences) {
        this.preferences = preferences;

        editorPreferences = new EditorPreferences(preferences);
        statusPreferences = new StatusPreferences(preferences);
        codeAreaPreferences = new CodeAreaPreferences(preferences);
        encodingPreferences = new TextEncodingPreferences(preferences);
        fontPreferences = new TextFontPreferences(preferences);
        layoutPreferences = new CodeAreaLayoutPreferences(preferences);
        themePreferences = new CodeAreaThemePreferences(preferences);
        colorPreferences = new CodeAreaColorPreferences(preferences);

        final String legacyDef = "LEGACY";
        String storedVersion = preferences.get(PREFERENCES_VERSION, legacyDef);
        if ("0.2.0".equals(storedVersion)) {
            convertPreferences_0_2_0();
        } else if (legacyDef.equals(storedVersion)) {
            try {
                importLegacyPreferences();
            } finally {
                preferences.put(PREFERENCES_VERSION, PREFERENCES_VERSION_VALUE);
                preferences.flush();
            }
        }
    }

    @Nonnull
    public Preferences getPreferences() {
        return preferences;
    }

    @Nonnull
    public EditorPreferences getEditorPreferences() {
        return editorPreferences;
    }

    @Nonnull
    public StatusPreferences getStatusPreferences() {
        return statusPreferences;
    }

    @Nonnull
    public CodeAreaPreferences getCodeAreaPreferences() {
        return codeAreaPreferences;
    }

    @Nonnull
    public TextEncodingPreferences getEncodingPreferences() {
        return encodingPreferences;
    }

    @Nonnull
    public TextFontPreferences getFontPreferences() {
        return fontPreferences;
    }

    @Nonnull
    public CodeAreaLayoutPreferences getLayoutPreferences() {
        return layoutPreferences;
    }

    @Nonnull
    public CodeAreaThemePreferences getThemePreferences() {
        return themePreferences;
    }

    @Nonnull
    public CodeAreaColorPreferences getColorPreferences() {
        return colorPreferences;
    }

    private void importLegacyPreferences() {
        LegacyPreferences legacyPreferences = new LegacyPreferences(preferences);
        codeAreaPreferences.setCodeType(legacyPreferences.getCodeType());
        codeAreaPreferences.setRowWrappingMode(legacyPreferences.isLineWrapping() ? RowWrappingMode.WRAPPING : RowWrappingMode.NO_WRAPPING);
        codeAreaPreferences.setShowUnprintables(legacyPreferences.isShowNonprintables());
        codeAreaPreferences.setCodeCharactersCase(legacyPreferences.getCodeCharactersCase());
        codeAreaPreferences.setPositionCodeType(legacyPreferences.getPositionCodeType());
        codeAreaPreferences.setViewMode(legacyPreferences.getViewMode());
        codeAreaPreferences.setPaintRowPosBackground(legacyPreferences.isPaintRowPosBackground());
        codeAreaPreferences.setCodeColorization(legacyPreferences.isCodeColorization());

        editorPreferences.setFileHandlingMode(legacyPreferences.isDeltaMemoryMode() ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY);
        editorPreferences.setShowValuesPanel(legacyPreferences.isShowValuesPanel());

        List<String> layoutProfiles = new ArrayList<>();
        layoutProfiles.add("Imported profile");
        DefaultExtendedCodeAreaLayoutProfile layoutProfile = new DefaultExtendedCodeAreaLayoutProfile();
        layoutProfile.setShowHeader(legacyPreferences.isShowHeader());
        layoutProfile.setShowRowPosition(legacyPreferences.isShowLineNumbers());
        layoutProfile.setSpaceGroupSize(legacyPreferences.getByteGroupSize());
        layoutProfile.setDoubleSpaceGroupSize(legacyPreferences.getSpaceGroupSize());
        layoutPreferences.setLayoutProfile(0, layoutProfile);
        layoutPreferences.setLayoutProfilesList(layoutProfiles);

        List<String> themeProfiles = new ArrayList<>();
        themeProfiles.add("Imported profile");
        ExtendedCodeAreaThemeProfile themeProfile = new ExtendedCodeAreaThemeProfile();
        themeProfile.setBackgroundPaintMode(legacyPreferences.getBackgroundPaintMode());
        themeProfile.setPaintRowPosBackground(legacyPreferences.isPaintRowPosBackground());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.HEADER_LINE, legacyPreferences.isDecorationHeaderLine());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.ROW_POSITION_LINE, legacyPreferences.isDecorationLineNumLine());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.SPLIT_LINE, legacyPreferences.isDecorationPreviewLine());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.BOX_LINES, legacyPreferences.isDecorationBox());
        themePreferences.setThemeProfile(0, themeProfile);
        themePreferences.setThemeProfilesList(themeProfiles);

        encodingPreferences.setSelectedEncoding(legacyPreferences.getSelectedEncoding());
        encodingPreferences.setEncodings(new ArrayList<>(legacyPreferences.getEncodings()));
        Collection<String> legacyEncodings = legacyPreferences.getEncodings();
        List<String> encodings = new ArrayList<>(legacyEncodings);
        if (!encodings.isEmpty() && !encodings.contains(StringUtils.ENCODING_UTF8)) {
            encodings.add(StringUtils.ENCODING_UTF8);
        }
        encodingPreferences.setEncodings(encodings);
        fontPreferences.setUseDefaultFont(legacyPreferences.isUseDefaultFont());
        fontPreferences.setFont(legacyPreferences.getCodeFont(CodeAreaPreferences.DEFAULT_FONT));

        preferences.flush();
    }

    private void convertPreferences_0_2_0() {
        String codeType = preferences.get(CodeAreaPreferences.PREFERENCES_VIEW_MODE, "DUAL");
        if ("HEXADECIMAL".equals(codeType)) {
            codeAreaPreferences.setViewMode(CodeAreaViewMode.CODE_MATRIX);
        } else if ("PREVIEW".equals(codeType)) {
            codeAreaPreferences.setViewMode(CodeAreaViewMode.TEXT_PREVIEW);
        }
        preferences.flush();
    }
}

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

import com.intellij.ide.util.PropertiesComponent;
import org.exbin.bined.intellij.FileHandlingMode;
import org.exbin.bined.swing.extended.layout.DefaultExtendedCodeAreaLayoutProfile;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaDecorations;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.Preferences;
import org.exbin.framework.PreferencesWrapper;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.editor.text.EncodingsHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Hexadecimal editor preferences.
 *
 * @version 0.2.0 2019/04/07
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryEditorPreferences {

    public static final String PLUGIN_PREFIX = "BinEdPlugin.";

    private final static String PREFERENCES_VERSION = "version";
    private final static String PREFERENCES_VERSION_VALUE = "0.2.0";

    private final Preferences preferences;

    private final EditorParameters editorParameters;
    private final StatusParameters statusParameters;
    private final CodeAreaParameters codeAreaParameters;
    private final CharsetParameters charsetParameters;
    private final LayoutParameters layoutParameters;
    private final ThemeParameters themeParameters;
    private final ColorParameters colorParameters;

    public BinaryEditorPreferences(Preferences preferences) {
        this.preferences = preferences;

        editorParameters = new EditorParameters(preferences);
        statusParameters = new StatusParameters(preferences);
        codeAreaParameters = new CodeAreaParameters(preferences);
        charsetParameters = new CharsetParameters(preferences);
        layoutParameters = new LayoutParameters(preferences);
        themeParameters = new ThemeParameters(preferences);
        colorParameters = new ColorParameters(preferences);

        String storedVersion = preferences.get(PREFERENCES_VERSION, "");
        if ("".equals(storedVersion)) {
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
    public EditorParameters getEditorParameters() {
        return editorParameters;
    }

    @Nonnull
    public StatusParameters getStatusParameters() {
        return statusParameters;
    }

    @Nonnull
    public CodeAreaParameters getCodeAreaParameters() {
        return codeAreaParameters;
    }

    public CharsetParameters getCharsetParameters() {
        return charsetParameters;
    }

    @Nonnull
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Nonnull
    public ThemeParameters getThemeParameters() {
        return themeParameters;
    }

    @Nonnull
    public ColorParameters getColorParameters() {
        return colorParameters;
    }

    private void importLegacyPreferences() {
        LegacyPreferences legacyPreferences = new LegacyPreferences(new PreferencesWrapper(PropertiesComponent.getInstance(), ""));
        codeAreaParameters.setSelectedEncoding(legacyPreferences.getSelectedEncoding());
        codeAreaParameters.setEncodings(new ArrayList<>(legacyPreferences.getEncodings()));
        codeAreaParameters.setUseDefaultFont(legacyPreferences.isUseDefaultFont());
        codeAreaParameters.setCodeFont(legacyPreferences.getCodeFont(CodeAreaOptions.DEFAULT_FONT));
        codeAreaParameters.setCodeType(legacyPreferences.getCodeType());
        codeAreaParameters.setRowWrapping(legacyPreferences.isLineWrapping());
        codeAreaParameters.setShowUnprintables(legacyPreferences.isShowNonprintables());
        codeAreaParameters.setCodeCharactersCase(legacyPreferences.getCodeCharactersCase());
        codeAreaParameters.setPositionCodeType(legacyPreferences.getPositionCodeType());
        codeAreaParameters.setViewMode(legacyPreferences.getViewMode());
        codeAreaParameters.setPaintRowPosBackground(legacyPreferences.isPaintRowPosBackground());
        codeAreaParameters.setCodeColorization(legacyPreferences.isCodeColorization());

        editorParameters.setFileHandlingMode(legacyPreferences.isDeltaMemoryMode() ? FileHandlingMode.DELTA.name() : FileHandlingMode.MEMORY.name());
        editorParameters.setShowValuesPanel(legacyPreferences.isShowValuesPanel());

        List<String> layoutProfiles = new ArrayList<>();
        layoutProfiles.add("Imported profile");
        DefaultExtendedCodeAreaLayoutProfile layoutProfile = new DefaultExtendedCodeAreaLayoutProfile();
        layoutProfile.setShowHeader(legacyPreferences.isShowHeader());
        layoutProfile.setShowRowPosition(legacyPreferences.isShowLineNumbers());
        layoutProfile.setSpaceGroupSize(legacyPreferences.getByteGroupSize());
        layoutProfile.setDoubleSpaceGroupSize(legacyPreferences.getSpaceGroupSize());
        layoutParameters.setLayoutProfile(0, layoutProfile);
        layoutParameters.setLayoutProfilesList(layoutProfiles);

        List<String> themeProfiles = new ArrayList<>();
        themeProfiles.add("Imported profile");
        ExtendedCodeAreaThemeProfile themeProfile = new ExtendedCodeAreaThemeProfile();
        themeProfile.setBackgroundPaintMode(legacyPreferences.getBackgroundPaintMode());
        themeProfile.setPaintRowPosBackground(legacyPreferences.isPaintRowPosBackground());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.HEADER_LINE, legacyPreferences.isDecorationHeaderLine());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.ROW_POSITION_LINE, legacyPreferences.isDecorationLineNumLine());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.SPLIT_LINE, legacyPreferences.isDecorationPreviewLine());
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.BOX_LINES, legacyPreferences.isDecorationBox());
        themeParameters.setThemeProfile(0, themeProfile);
        themeParameters.setThemeProfilesList(themeProfiles);

        Collection<String> legacyEncodings = legacyPreferences.getEncodings();
        List<String> encodings = new ArrayList<>(legacyEncodings);
        if (!encodings.isEmpty() && !encodings.contains(EncodingsHandler.ENCODING_UTF8)) {
            encodings.add(EncodingsHandler.ENCODING_UTF8);
        }
        charsetParameters.setEncodings(encodings);

        preferences.flush();
    }
}

/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.bined.intellij.diff.gui;

import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.openapi.editor.Document;
import com.intellij.ui.components.JBPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.array.ByteArrayData;
import org.exbin.auxiliary.binary_data.array.paged.ByteArrayPagedData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.jaguif.component.BinEdCodeAreaAssessor;
import org.exbin.bined.jaguif.component.BinEdComponentExtension;
import org.exbin.bined.jaguif.component.BinEdDataComponent.UpdateType;
import org.exbin.bined.jaguif.component.BinaryDataComponent;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.jaguif.editor.settings.BinaryEditorOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaColorOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaLayoutOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaThemeOptions;
import org.exbin.bined.jaguif.viewer.settings.BinaryEncodingSettingsApplier;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaOptions;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaStatusOptions;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaViewerSettingsApplier;
import org.exbin.bined.jaguif.viewer.status.gui.BinaryDataSizeComponent;
import org.exbin.bined.operation.command.BinaryDataUndoRedo;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.section.capability.PositionCodeTypeCapable;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.CharAssessorPainterCapable;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.jaguif.App;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.context.api.ContextComponent;
import org.exbin.jaguif.context.api.ContextModuleApi;
import org.exbin.jaguif.context.api.ContextRegistration;
import org.exbin.jaguif.context.api.ContextUpdateManagement;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.language.api.LanguageModuleApi;
import org.exbin.jaguif.options.api.OptionsModuleApi;
import org.exbin.jaguif.options.api.OptionsStorage;
import org.exbin.jaguif.options.settings.action.SettingsAction;
import org.exbin.jaguif.options.settings.api.OptionsSettingsManagement;
import org.exbin.jaguif.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.jaguif.statusbar.api.StatusBar;
import org.exbin.jaguif.statusbar.api.StatusBarComponent;
import org.exbin.jaguif.statusbar.api.StatusBarModuleApi;
import org.exbin.jaguif.text.encoding.CharsetEncodingState;
import org.exbin.jaguif.text.encoding.CharsetListEncodingState;
import org.exbin.jaguif.text.encoding.ContextEncoding;
import org.exbin.jaguif.text.encoding.settings.TextEncodingOptions;
import org.exbin.jaguif.text.font.settings.TextFontOptions;
import org.exbin.jaguif.utils.DesktopUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BinEd diff support provider to compare binary files.
 */
@NullMarked
public class BinEdDiffPanel extends JBPanel {

    protected final SectCodeAreaDiffPanel diffPanel = new SectCodeAreaDiffPanel();

    protected final Font defaultFont;
    protected final SectionCodeAreaLayoutProfile defaultLayoutProfile;
    protected final SectionCodeAreaThemeProfile defaultThemeProfile;
    protected final CodeAreaColorsProfile defaultColorProfile;
    protected List<String> encodings = new ArrayList<>();

    protected DiffContextComponent leftContextComponent;
    protected DiffContextComponent rightContextComponent;
    protected ActiveContextManagement leftContextManager;
    protected ActiveContextManagement rightContextManager;
    protected ContextRegistration leftContextRegistrator;
    protected ContextRegistration rightContextRegistrator;
    protected final BinEdToolbarPanel toolbarPanel;
    protected final StatusBar leftStatusBar;
    protected final StatusBar rightStatusBar;

    public BinEdDiffPanel() {
        setLayout(new java.awt.BorderLayout());

        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();

        CodeAreaPainter leftPainter = leftCodeArea.getPainter();
        BinEdCodeAreaAssessor codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable) leftPainter).getColorAssessor(), ((CharAssessorPainterCapable) leftPainter).getCharAssessor());
        ((ColorAssessorPainterCapable) leftPainter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable) leftPainter).setCharAssessor(codeAreaAssessor);
        CodeAreaPainter rightPainter = rightCodeArea.getPainter();
        codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable) rightPainter).getColorAssessor(), ((CharAssessorPainterCapable) rightPainter).getCharAssessor());
        ((ColorAssessorPainterCapable) rightPainter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable) rightPainter).setCharAssessor(codeAreaAssessor);

        defaultLayoutProfile = leftCodeArea.getLayoutProfile();
        defaultThemeProfile = leftCodeArea.getThemeProfile();
        defaultColorProfile = leftCodeArea.getColorsProfile();
        toolbarPanel = new BinEdToolbarPanel();
        leftContextComponent = new DiffContextComponent(leftCodeArea);
        rightContextComponent = new DiffContextComponent(rightCodeArea);
        StatusBarModuleApi statusBarModule = App.getModule(StatusBarModuleApi.class);
        ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
        leftContextManager = contextModule.createContextManager();
        leftContextManager.changeActiveState(ContextComponent.class, leftContextComponent);
        rightContextManager = contextModule.createContextManager();
        rightContextManager.changeActiveState(ContextComponent.class, rightContextComponent);
        attachContext(leftCodeArea, leftContextComponent, leftContextManager);
        attachContext(rightCodeArea, rightContextComponent, rightContextManager);
        ContextUpdateManagement leftUpdateManagement = contextModule.createContextUpdateManagement(leftContextManager);
        leftUpdateManagement.addGroup(BinedComponentModule.BINARY_STATUS_BAR_ID);
        ContextUpdateManagement rightUpdateManagement = contextModule.createContextUpdateManagement(rightContextManager);
        rightUpdateManagement.addGroup(BinedComponentModule.BINARY_STATUS_BAR_ID);
        leftContextRegistrator = contextModule.createContextRegistrator(BinedComponentModule.BINARY_STATUS_BAR_ID, leftUpdateManagement, leftContextManager);
        rightContextRegistrator = contextModule.createContextRegistrator(BinedComponentModule.BINARY_STATUS_BAR_ID, rightUpdateManagement, rightContextManager);
        leftStatusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, leftContextRegistrator);
        rightStatusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, rightContextRegistrator);
        toolbarPanel.setTargetComponent(diffPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
            @Override
            public CodeType getCodeType() {
                return leftCodeArea.getCodeType();
            }

            @Override
            public void setCodeType(CodeType codeType) {
                leftCodeArea.setCodeType(codeType);
                rightCodeArea.setCodeType(codeType);
            }

            @Override
            public boolean isShowNonprintables() {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) leftCodeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                return CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).isShowNonprintables();
            }

            @Override
            public void setShowNonprintables(boolean showNonprintables) {
                ColorAssessorPainterCapable leftPainter = (ColorAssessorPainterCapable) leftCodeArea.getPainter();
                NonprintablesCodeAreaAssessor leftNonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(leftPainter, NonprintablesCodeAreaAssessor.class);
                CodeAreaUtils.requireNonNull(leftNonprintablesCodeAreaAssessor).setShowNonprintables(showNonprintables);
                ColorAssessorPainterCapable rightPainter = (ColorAssessorPainterCapable) rightCodeArea.getPainter();
                NonprintablesCodeAreaAssessor rightNonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(rightPainter, NonprintablesCodeAreaAssessor.class);
                CodeAreaUtils.requireNonNull(rightNonprintablesCodeAreaAssessor).setShowNonprintables(showNonprintables);
            }

            @Override
            public void repaint() {
                diffPanel.repaint();
            }
        });
        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        SettingsAction settingsAction = (SettingsAction) optionsSettingsModule.createSettingsAction();
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        settingsAction.setDialogParentComponent(() -> frameModule.getFrame());
        AbstractAction wrapperAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsAction.actionPerformed(e);
                // TODO Options are not applied due to no active file handler is present
                toolbarPanel.applyFromCodeArea();
            }
        };
        toolbarPanel.setOptionsAction(wrapperAction);
        toolbarPanel.setOnlineHelpAction(createOnlineHelpAction());

        // Load settings
        OptionsSettingsManagement settingsManager = optionsSettingsModule.getMainSettingsManager();
        TextEncodingOptions options = settingsManager.getSettingsOptionsProvider().getSettingsOptions(TextEncodingOptions.class);
        encodings = options.getEncodings();
        init();
    }

    private void init() {
        this.add(toolbarPanel, BorderLayout.NORTH);

        initialLoadFromPreferences();
        BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
        JPopupMenu codeAreaPopupMenu = binedComponentModule.createCodeAreaPopupMenu();
        diffPanel.getLeftCodeArea().setComponentPopupMenu(codeAreaPopupMenu);
        diffPanel.getRightCodeArea().setComponentPopupMenu(codeAreaPopupMenu);

        diffPanel.getLeftPanel().add(leftStatusBar.getComponent(), BorderLayout.SOUTH);
        diffPanel.getRightPanel().add(rightStatusBar.getComponent(), BorderLayout.SOUTH);
        this.add(diffPanel, BorderLayout.CENTER);
        diffPanel.revalidate();
        diffPanel.repaint();
        revalidate();
        repaint();
    }

    public void setDiffContent(ContentDiffRequest request) {
        List<DiffContent> contents = request.getContents();
        if (!contents.isEmpty()) {
            BinaryData leftData = getDiffBinaryData(request, 0);
            if (leftData != null) {
                diffPanel.setLeftContentData(leftData);
            }

            BinaryData rightData = getDiffBinaryData(request, 1);
            if (rightData != null) {
                diffPanel.setRightContentData(rightData);
            }
        }
    }

    @Nullable
    private static BinaryData getDiffBinaryData(ContentDiffRequest request, int index) {
        List<DiffContent> contents = request.getContents();
        if (contents.size() > index) {
            DiffContent diffContent = contents.get(index);
            if (diffContent instanceof FileContent) {
                PagedData pageData = new ByteArrayPagedData();
                try {
                    byte[] fileContent = ((FileContent) diffContent).getFile().contentsToByteArray();
                    pageData.insert(0, fileContent);
                    return pageData;
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to read file content", e);
                }
            }
            if (diffContent instanceof DocumentContent) {
                Document document = ((DocumentContent) diffContent).getDocument();
                return new ByteArrayData(document.getText().getBytes(StandardCharsets.UTF_8));
            }
        }

        return null;
    }

    private static void attachContext(SectCodeArea codeArea, ContextComponent contextComponent, ActiveContextManagement contextManagement) {
        contextManagement.changeActiveState(ContextComponent.class, contextComponent);
        codeArea.addDataChangedListener(() -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.DATA_CONTENT);
        });

        ((SelectionCapable) codeArea).addSelectionChangedListener(() -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.SELECTION);
        });

        ((CaretCapable) codeArea).addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.CURSOR_POSITION);
        });

        ((EditModeCapable) codeArea).addEditModeChangedListener((EditMode mode, EditOperation operation) -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.EDIT_MODE);
        });

        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        BinaryEncodingSettingsApplier settingsApplier = new BinaryEncodingSettingsApplier();
        settingsApplier.applySettings(
                contextManagement,
                optionsSettingsModule.getMainSettingsManager().getSettingsOptionsProvider());
    }

    private void initialLoadFromPreferences() {
        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        OptionsStorage options = optionsModule.getAppOptions();

        applyOptions(options, diffPanel.getLeftCodeArea());
        applyOptions(options, diffPanel.getRightCodeArea());

        CodeAreaStatusOptions statusOptions = new CodeAreaStatusOptions(options);
//        leftStatusPanel.loadFromOptions(statusOptions);
//        rightStatusPanel.loadFromOptions(statusOptions);
        toolbarPanel.applyFromCodeArea();
        toolbarPanel.loadFromOptions(options);
    }

    private void applyOptions(OptionsStorage optionsStorage, SectCodeArea codeArea) {
        CodeAreaViewerSettingsApplier.applyToCodeArea(new CodeAreaOptions(optionsStorage), codeArea);

        TextEncodingOptions encodingOptions = new TextEncodingOptions(optionsStorage);
        ((CharsetCapable) codeArea).setCharset(Charset.forName(encodingOptions
                .getSelectedEncoding()));
        encodings = encodingOptions.getEncodings();
        TextFontOptions fontOptions = new TextFontOptions(optionsStorage);
        ((FontCapable) codeArea).setCodeFont(fontOptions.isUseDefaultFont() ?
                defaultFont :
                fontOptions.getFont(defaultFont));

        BinaryEditorOptions editorOptions = new BinaryEditorOptions(optionsStorage);
        //        switchShowValuesPanel(editorOptions.isShowValuesPanel());
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).setEnterKeyHandlingMode(editorOptions.getEnterKeyHandlingMode());
        }

        CodeAreaLayoutOptions layoutOptions = new CodeAreaLayoutOptions(optionsStorage);
        int selectedLayoutProfile = layoutOptions.getSelectedProfile();
        if (selectedLayoutProfile >= 0) {
            codeArea.setLayoutProfile(layoutOptions.getLayoutProfile(selectedLayoutProfile));
        } else {
            codeArea.setLayoutProfile(defaultLayoutProfile);
        }

        CodeAreaThemeOptions themeOptions = new CodeAreaThemeOptions(optionsStorage);
        int selectedThemeProfile = themeOptions.getSelectedProfile();
        if (selectedThemeProfile >= 0) {
            codeArea.setThemeProfile(themeOptions.getThemeProfile(selectedThemeProfile));
        } else {
            codeArea.setThemeProfile(defaultThemeProfile);
        }

        CodeAreaColorOptions colorOptions = new CodeAreaColorOptions(optionsStorage);
        int selectedColorProfile = colorOptions.getSelectedProfile();
        if (selectedColorProfile >= 0) {
            codeArea.setColorsProfile(colorOptions.getColorsProfile(selectedColorProfile));
        } else {
            codeArea.setColorsProfile(defaultColorProfile);
        }
    }

    private AbstractAction createOnlineHelpAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LanguageModuleApi languageModuleApi = App.getModule(LanguageModuleApi.class);
                DesktopUtils.openDesktopURL(languageModuleApi.getAppBundle().getString("online_help_url"));
            }
        };
    }

    public void setLeftContentData(BinaryData contentData) {
        diffPanel.setLeftContentData(contentData);
        updateDataSize(leftStatusBar, contentData.getDataSize());
    }

    public void setRightContentData(BinaryData contentData) {
        diffPanel.setRightContentData(contentData);
        updateDataSize(rightStatusBar, contentData.getDataSize());
    }

    private void updateDataSize(StatusBar statusBar, long dataSize) {
        BinaryDataSizeComponent dataSizeComponent = null;
        for (int i = 0; i < statusBar.getItemsCount(); i++) {
            StatusBarComponent component = statusBar.getItem(i);
            if (component instanceof BinaryDataSizeComponent) {
                dataSizeComponent = (BinaryDataSizeComponent) component;
                break;
            }
        }
        if (dataSizeComponent != null) {
            dataSizeComponent.setOriginalDataSize(dataSize);
        }
    }

    public class DiffContextComponent implements BinaryDataComponent, CharsetEncodingState, CharsetListEncodingState {

        SectCodeArea codeArea;
        ActiveContextManagement contextManagement;

        public DiffContextComponent(SectCodeArea codeArea) {
            this.codeArea = codeArea;
        }

        @Override
        public CodeAreaCore getCodeArea() {
            return codeArea;
        }

        @Override
        public Optional<BinaryDataUndoRedo> getUndoRedo() {
            return Optional.empty();
        }

        @Override
        public <T extends BinEdComponentExtension> T getComponentExtension(Class<T> clazz) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Component getComponent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CodeType getCodeType() {
            return codeArea.getCodeType();
        }

        @Override
        public void setCodeType(CodeType codeType) {
            codeArea.setCodeType(codeType);
            contextManagement.updateActiveState(ContextComponent.class, this, org.exbin.bined.jaguif.component.CodeTypeState.UpdateType.CODE_TYPE);
        }

        public PositionCodeType getPositionCodeType() {
            return codeArea.getPositionCodeType();
        }

        public void setPositionCodeType(PositionCodeType positionCodeType) {
            codeArea.setPositionCodeType(positionCodeType);
            contextManagement.updateActiveState(ContextComponent.class, this, org.exbin.bined.jaguif.component.CodeTypeState.UpdateType.POSITION_CODE_TYPE);
        }

        @Override
        public CodeCharactersCase getCodeCharactersCase() {
            return codeArea.getCodeCharactersCase();
        }

        @Override
        public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
            codeArea.setCodeCharactersCase(codeCharactersCase);
            contextManagement.updateActiveState(ContextComponent.class, this, org.exbin.bined.jaguif.component.CodeTypeState.UpdateType.HEX_CHARACTERS_CASE);
        }

        public boolean isShowNonprintables() {
            ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
            NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
            return nonprintablesCodeAreaAssessor != null && nonprintablesCodeAreaAssessor.isShowNonprintables();
        }

        public void setShowNonprintables(boolean showNonprintables) {
            ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
            NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
            if (nonprintablesCodeAreaAssessor != null) {
                nonprintablesCodeAreaAssessor.setShowNonprintables(showNonprintables);
                this.codeArea.repaint();
                if (this.contextManagement != null) {
                    this.contextManagement.updateActiveState(ContextComponent.class, this, org.exbin.bined.jaguif.component.NonprintablesState.UpdateType.NONPRINTABLES);
                }
            }
        }

        @Override
        public Optional<ActiveContextManagement> getContextManagement() {
            return Optional.of(contextManagement);
        }

        @Override
        public <T extends StatusBarComponent> Optional<T> getStatusBarComponent(Class<T> aClass) {
            return Optional.empty();
        }

        @Override
        public void setEditOperation(EditOperation editOperation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getEncoding() {
            return ((CharsetCapable) getCodeArea()).getCharset().toString();
        }

        @Override
        public void setEncoding(String encodingName) {
            ((CharsetCapable) getCodeArea()).setCharset(Charset.forName(encodingName));
        }

        @Override
        public List<String> getEncodings() {
            return encodings;
        }

        @Override
        public void setEncodings(List<String> encodings) {
            BinEdDiffPanel.this.encodings.clear();
            BinEdDiffPanel.this.encodings.addAll(encodings);
            leftContextManager.updateActiveState(ContextEncoding.class, leftContextComponent, CharsetListEncodingState.UpdateType.ENCODING_LIST);
            rightContextManager.updateActiveState(ContextEncoding.class, rightContextComponent, CharsetListEncodingState.UpdateType.ENCODING_LIST);
        }
    }
}

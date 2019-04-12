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
package org.exbin.framework.editor.text;

import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import org.exbin.framework.editor.text.panel.AddEncodingPanel;
import org.exbin.framework.editor.text.panel.TextEncodingPanel;
import org.exbin.framework.editor.text.panel.TextEncodingPanelApi;
import org.exbin.framework.gui.utils.ActionUtils;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.gui.utils.handler.DefaultControlHandler;
import org.exbin.framework.gui.utils.handler.OptionsControlHandler;
import org.exbin.framework.gui.utils.panel.DefaultControlPanel;
import org.exbin.framework.gui.utils.panel.OptionsControlPanel;

/**
 * Encodings handler.
 *
 * @version 0.2.0 2018/12/31
 * @author ExBin Project (http://exbin.org)
 */
public class EncodingsHandler implements TextEncodingPanelApi {

    private final ResourceBundle resourceBundle;

    private TextEncodingStatusApi textEncodingStatus;
    private List<String> encodings = null;
    private String selectedEncoding;
    private ActionListener encodingActionListener;
    private ButtonGroup encodingButtonGroup;
    private javax.swing.JMenu toolsEncodingMenu;
    private javax.swing.JRadioButtonMenuItem utfEncodingRadioButtonMenuItem;
    private ActionListener utfEncodingActionListener;

    public static final String ENCODING_UTF8 = "UTF-8";

    public static final String DEFAULT_ENCODING_TEXT = "UTF-8 (default)";
    public static final String ENCODING_TOOLTIP_PREFIX = "Set encoding ";

    private Action manageEncodingsAction;
    private BinaryEditorPreferences preferences;

    public EncodingsHandler(TextEncodingStatusApi textEncodingStatus) {
        resourceBundle = LanguageUtils.getResourceBundleByClass(EncodingsHandler.class);
        this.textEncodingStatus = textEncodingStatus;
        init();
        EncodingsHandler.this.rebuildEncodings();
    }

    private void init() {
        encodings = new ArrayList<>();
        encodingButtonGroup = new ButtonGroup();

        encodingActionListener = (ActionEvent e) -> {
            setSelectedEncoding(((JRadioButtonMenuItem) e.getSource()).getText());
        };

        utfEncodingRadioButtonMenuItem = new JRadioButtonMenuItem();
        utfEncodingRadioButtonMenuItem.setSelected(true);
        utfEncodingRadioButtonMenuItem.setText(DEFAULT_ENCODING_TEXT);
        utfEncodingRadioButtonMenuItem.setToolTipText(ENCODING_TOOLTIP_PREFIX + ENCODING_UTF8);
        utfEncodingActionListener = (java.awt.event.ActionEvent evt) -> setSelectedEncoding(ENCODING_UTF8);
        utfEncodingRadioButtonMenuItem.addActionListener(utfEncodingActionListener);

        encodingButtonGroup.add(utfEncodingRadioButtonMenuItem);
        manageEncodingsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final TextEncodingPanel textEncodingPanel = new TextEncodingPanel();
                textEncodingPanel.setHandler(EncodingsHandler.this);
                textEncodingPanel.setEncodingList(encodings);
                final OptionsControlPanel optionsControlPanel = new OptionsControlPanel();
                JPanel dialogPanel = WindowUtils.createDialogPanel(textEncodingPanel, optionsControlPanel);
                final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, null, "Manage Encodings", Dialog.ModalityType.APPLICATION_MODAL);
                optionsControlPanel.setHandler((OptionsControlHandler.ControlActionType actionType) -> {
                    if (actionType != OptionsControlHandler.ControlActionType.CANCEL) {
                        encodings = textEncodingPanel.getEncodingList();
                        rebuildEncodings();
                        if (actionType == OptionsControlHandler.ControlActionType.SAVE) {
                            preferences.getCodeAreaParameters().setEncodings(encodings);
                        }
                    }

                    dialog.close();
                });
                textEncodingPanel.setAddEncodingsOperation((List<String> usedEncodings) -> {
                    final List<String> result = new ArrayList<>();
                    final AddEncodingPanel addEncodingPanel = new AddEncodingPanel();
                    addEncodingPanel.setUsedEncodings(usedEncodings);
                    DefaultControlPanel encodingsControlPanel = new DefaultControlPanel(addEncodingPanel.getResourceBundle());
                    JPanel encodingDialogPanel = WindowUtils.createDialogPanel(addEncodingPanel, encodingsControlPanel);
                    final DialogWrapper addEncodingDialog = WindowUtils.createDialog(encodingDialogPanel, null, "Add Encodings", Dialog.ModalityType.APPLICATION_MODAL);
                    encodingsControlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                        if (actionType == DefaultControlHandler.ControlActionType.OK) {
                            result.addAll(addEncodingPanel.getEncodings());
                        }

                        addEncodingDialog.close();
                    });
                    addEncodingDialog.show();
                    return result;
                });
                dialog.show();
            }
        };
        ActionUtils.setupAction(manageEncodingsAction, resourceBundle, "manageEncodingsAction");
        manageEncodingsAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);
        manageEncodingsAction.putValue(Action.NAME, manageEncodingsAction.getValue(Action.NAME) + ActionUtils.DIALOG_MENUITEM_EXT);

        toolsEncodingMenu = new JMenu();
        toolsEncodingMenu.addSeparator();
        toolsEncodingMenu.add(manageEncodingsAction);
        toolsEncodingMenu.setText(resourceBundle.getString("toolsEncodingMenu.text"));
        toolsEncodingMenu.setToolTipText(resourceBundle.getString("toolsEncodingMenu.shortDescription"));
    }

    @Override
    public List<String> getEncodings() {
        return encodings;
    }

    @Override
    public void setEncodings(List<String> encodings) {
        this.encodings = encodings;
    }

    @Override
    public String getSelectedEncoding() {
        return selectedEncoding;
    }

    @Override
    public void setSelectedEncoding(String encoding) {
        if (encoding != null) {
            selectedEncoding = encoding;
            textEncodingStatus.setEncoding(encoding);
        }
    }

    public void setTextEncodingStatus(TextEncodingStatusApi textEncodingStatus) {
        this.textEncodingStatus = textEncodingStatus;
    }

    public JMenu getToolsEncodingMenu() {
        return toolsEncodingMenu;
    }

    public void rebuildEncodings() {
        for (int i = toolsEncodingMenu.getItemCount() - 2; i >= 0; i--) {
            toolsEncodingMenu.remove(i);
        }

        if (encodings.isEmpty()) {
            toolsEncodingMenu.add(utfEncodingRadioButtonMenuItem, 0);
            selectedEncoding = ENCODING_UTF8;
            utfEncodingRadioButtonMenuItem.setSelected(true);
        } else {
            int selectedEncodingIndex = encodings.indexOf(getSelectedEncoding());
            for (int index = 0; index < encodings.size(); index++) {
                String encoding = encodings.get(index);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(encoding, index == selectedEncodingIndex);
                item.addActionListener(encodingActionListener);
                item.setToolTipText(ENCODING_TOOLTIP_PREFIX + encoding);
                toolsEncodingMenu.add(item, index);
                encodingButtonGroup.add(item);
            }
        }
    }

    private void updateEncodingsSelection(int menuIndex) {
        if (menuIndex > 0) {
            menuIndex++;
        }
        JMenuItem item = toolsEncodingMenu.getItem(menuIndex);
        item.setSelected(true);
    }

    public void loadFromPreferences(BinaryEditorPreferences preferences) {
        this.preferences = preferences;
        selectedEncoding = preferences.getCodeAreaParameters().getSelectedEncoding();
        encodings.clear();
        encodings.addAll(preferences.getCodeAreaParameters().getEncodings());
        rebuildEncodings();
    }

    public void cycleEncodings() {
        int menuIndex = 0;
        if (!encodings.isEmpty()) {
            int selectedEncodingIndex = encodings.indexOf(getSelectedEncoding());
            if (selectedEncodingIndex < 0 || selectedEncodingIndex == encodings.size() - 1) {
                setSelectedEncoding(encodings.get(0));
            } else {
                setSelectedEncoding(encodings.get(selectedEncodingIndex + 1));
                menuIndex = selectedEncodingIndex;
            }
        }

        updateEncodingsSelection(menuIndex);
    }

    public void popupEncodingsMenu(MouseEvent mouseEvent) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (encodings.isEmpty()) {
            JRadioButtonMenuItem utfEncoding = new JRadioButtonMenuItem(DEFAULT_ENCODING_TEXT, ENCODING_UTF8.equals(selectedEncoding));
            utfEncoding.setToolTipText(ENCODING_TOOLTIP_PREFIX + ENCODING_UTF8);
            utfEncoding.addActionListener(utfEncodingActionListener);
            popupMenu.add(utfEncoding);
        } else {
            int selectedEncodingIndex = encodings.indexOf(getSelectedEncoding());
            for (int index = 0; index < encodings.size(); index++) {
                String encoding = encodings.get(index);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(encoding, index == selectedEncodingIndex);
                item.addActionListener(encodingActionListener);
                item.setToolTipText(ENCODING_TOOLTIP_PREFIX + encoding);
                popupMenu.add(item, index);
            }
        }

        popupMenu.add(new JSeparator());
        popupMenu.add(manageEncodingsAction);

        popupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
    }
}

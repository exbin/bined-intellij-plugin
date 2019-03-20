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
import org.exbin.framework.editor.text.panel.AddEncodingPanel;
import org.exbin.framework.editor.text.panel.TextEncodingPanel;
import org.exbin.framework.editor.text.panel.TextEncodingPanelApi;
import org.exbin.framework.gui.utils.ActionUtils;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.handler.DefaultControlHandler;
import org.exbin.framework.gui.utils.handler.OptionsControlHandler;
import org.exbin.framework.gui.utils.panel.DefaultControlPanel;
import org.exbin.framework.gui.utils.panel.OptionsControlPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    private ActionListener encodingActionListener;
    private ButtonGroup encodingButtonGroup;
    private javax.swing.JMenu toolsEncodingMenu;
    private javax.swing.JRadioButtonMenuItem utfEncodingRadioButtonMenuItem;
    private ActionListener utfEncodingActionListener;

    public static final String ENCODING_UTF8 = "UTF-8";

    public static final String UTF_ENCODING_TEXT = "UTF-8 (default)";
    public static final String UTF_ENCODING_TOOLTIP = "Set encoding UTF-8";

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
        utfEncodingRadioButtonMenuItem.setText(UTF_ENCODING_TEXT);
        utfEncodingRadioButtonMenuItem.setToolTipText(UTF_ENCODING_TOOLTIP);
        utfEncodingActionListener = (java.awt.event.ActionEvent evt) -> {
            setSelectedEncoding(ENCODING_UTF8);
        };
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
                final Dialog dialog = WindowUtils.createDialog(dialogPanel, null, Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setTitle("Manage Encodings");
                optionsControlPanel.setHandler((OptionsControlHandler.ControlActionType actionType) -> {
                    if (actionType != OptionsControlHandler.ControlActionType.CANCEL) {
                        encodings = textEncodingPanel.getEncodingList();
                        rebuildEncodings();
                        if (actionType == OptionsControlHandler.ControlActionType.SAVE) {
                            preferences.getCodeAreaParameters().setEncodings(encodings);
                        }
                    }

                    WindowUtils.closeWindow(dialog);
                });
                textEncodingPanel.setAddEncodingsOperation((List<String> usedEncodings) -> {
                    final List<String> result = new ArrayList<>();
                    final AddEncodingPanel addEncodingPanel = new AddEncodingPanel();
                    addEncodingPanel.setUsedEncodings(usedEncodings);
                    DefaultControlPanel encodingsControlPanel = new DefaultControlPanel(addEncodingPanel.getResourceBundle());
                    JPanel dialogPanel1 = WindowUtils.createDialogPanel(addEncodingPanel, encodingsControlPanel);
                    final Dialog addEncodingDialog = WindowUtils.createDialog(dialogPanel1, null, Dialog.ModalityType.APPLICATION_MODAL);
                    dialog.setTitle("Add Encodings");
                    encodingsControlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                        if (actionType == DefaultControlHandler.ControlActionType.OK) {
                            result.addAll(addEncodingPanel.getEncodings());
                        }

                        WindowUtils.closeWindow(addEncodingDialog);
                    });
                    addEncodingDialog.setVisible(true);
                    return result;
                });
                dialog.setVisible(true);
            }
        };
        ActionUtils.setupAction(manageEncodingsAction, resourceBundle, "manageEncodingsAction");
        manageEncodingsAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);
        manageEncodingsAction.putValue(Action.NAME, manageEncodingsAction.getValue(Action.NAME) + ActionUtils.DIALOG_MENUITEM_EXT);

        toolsEncodingMenu = new JMenu();
        toolsEncodingMenu.add(utfEncodingRadioButtonMenuItem);
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
        return textEncodingStatus.getEncoding(); // ((TextCharsetApi) editorProvider.getPanel()).getCharset().name();
    }

    @Override
    public void setSelectedEncoding(String encoding) {
        if (encoding != null) {
//            ((TextCharsetApi) editorProvider.getPanel()).setCharset(Charset.forName(encoding));
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
        String encodingToolTip = "Set encoding ";
        for (int i = toolsEncodingMenu.getItemCount() - 2; i > 1; i--) {
            toolsEncodingMenu.remove(i);
        }

        if (encodings.size() > 0) {
            int selectedEncoding = encodings.indexOf(getSelectedEncoding());
            if (selectedEncoding < 0) {
                setSelectedEncoding(ENCODING_UTF8);
                utfEncodingRadioButtonMenuItem.setSelected(true);
            }
            toolsEncodingMenu.add(new JSeparator(), 1);
            for (int index = 0; index < encodings.size(); index++) {
                String encoding = encodings.get(index);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(encoding, false);
                item.addActionListener(encodingActionListener);
                item.setToolTipText(encodingToolTip + encoding);
                toolsEncodingMenu.add(item, index + 2);
                encodingButtonGroup.add(item);
                if (index == selectedEncoding) {
                    item.setSelected(true);
                }
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
        setSelectedEncoding(preferences.getCodeAreaParameters().getSelectedEncoding());
        encodings.clear();
        encodings.addAll(preferences.getCodeAreaParameters().getEncodings());
        rebuildEncodings();
    }

    public void cycleEncodings() {
        int menuIndex = 0;
        if (encodings.size() > 0) {
            int selectedEncoding = encodings.indexOf(getSelectedEncoding());
            if (selectedEncoding < 0) {
                setSelectedEncoding(encodings.get(0));
                menuIndex = 1;
            } else if (selectedEncoding < encodings.size() - 1) {
                setSelectedEncoding(encodings.get(selectedEncoding + 1));
                menuIndex = selectedEncoding + 2;
            } else {
                setSelectedEncoding(ENCODING_UTF8);
            }
        }

        updateEncodingsSelection(menuIndex);
    }

    public void popupEncodingsMenu(MouseEvent mouseEvent) {
        JPopupMenu popupMenu = new JPopupMenu();

        int selectedEncoding = encodings.indexOf(getSelectedEncoding());
        String encodingToolTip = "Set encoding ";
        JRadioButtonMenuItem utfEncoding = new JRadioButtonMenuItem("", false);
        utfEncoding.setText(UTF_ENCODING_TEXT);
        utfEncoding.setToolTipText(UTF_ENCODING_TOOLTIP);
        utfEncoding.addActionListener(utfEncodingActionListener);
        if (selectedEncoding < 0) {
            utfEncoding.setSelected(true);
        }
        popupMenu.add(utfEncoding);
        if (encodings.size() > 0) {

            popupMenu.add(new JSeparator(), 1);
            for (int index = 0; index < encodings.size(); index++) {
                String encoding = encodings.get(index);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(encoding, false);
                item.addActionListener(encodingActionListener);
                item.setToolTipText(encodingToolTip + encoding);
                popupMenu.add(item, index + 2);
                if (index == selectedEncoding) {
                    item.setSelected(true);
                }
            }
        }

        popupMenu.add(new JSeparator());
        popupMenu.add(manageEncodingsAction);

        popupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
    }
}

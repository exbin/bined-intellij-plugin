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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import org.exbin.framework.editor.text.gui.AddEncodingPanel;
import org.exbin.framework.editor.text.options.gui.TextEncodingPanel;
import org.exbin.framework.editor.text.preferences.TextEncodingPreferences;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.utils.handler.DefaultControlHandler;
import org.exbin.framework.utils.handler.OptionsControlHandler;
import org.exbin.framework.utils.gui.DefaultControlPanel;
import org.exbin.framework.utils.gui.OptionsControlPanel;
import org.exbin.framework.editor.text.service.TextEncodingService;
import org.exbin.framework.editor.text.service.impl.TextEncodingServiceImpl;
import org.exbin.xbup.core.util.StringUtils;

/**
 * Encodings handler.
 *
 * @version 0.2.1 2019/07/29
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EncodingsHandler {

    private final ResourceBundle resourceBundle;

    private ActionListener encodingActionListener;
    private ButtonGroup encodingButtonGroup;
    private javax.swing.JMenu toolsEncodingMenu;
    private javax.swing.JRadioButtonMenuItem utfEncodingRadioButtonMenuItem;
    private ActionListener utfEncodingActionListener;
    private Component parentComponent = null;

    public static final String DEFAULT_ENCODING_TEXT = "UTF-8 (default)";
    public static final String ENCODING_TOOLTIP_PREFIX = "Set encoding ";

    private Action manageEncodingsAction;
    private TextEncodingPreferences preferences;

    private final TextEncodingService textEncodingService = new TextEncodingServiceImpl();

    public EncodingsHandler() {
        resourceBundle = LanguageUtils.getResourceBundleByClass(EncodingsHandler.class);
    }

    public void init() {
        encodingButtonGroup = new ButtonGroup();

        encodingActionListener = (ActionEvent e) -> {
            textEncodingService.setSelectedEncoding(((JRadioButtonMenuItem) e.getSource()).getText());
        };

        utfEncodingRadioButtonMenuItem = new JRadioButtonMenuItem();
        utfEncodingRadioButtonMenuItem.setSelected(true);
        utfEncodingRadioButtonMenuItem.setText(DEFAULT_ENCODING_TEXT);
        utfEncodingRadioButtonMenuItem.setToolTipText(ENCODING_TOOLTIP_PREFIX + StringUtils.ENCODING_UTF8);
        utfEncodingActionListener = (java.awt.event.ActionEvent evt) -> textEncodingService.setSelectedEncoding(StringUtils.ENCODING_UTF8);
        utfEncodingRadioButtonMenuItem.addActionListener(utfEncodingActionListener);

        encodingButtonGroup.add(utfEncodingRadioButtonMenuItem);
        manageEncodingsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final TextEncodingPanel textEncodingPanel = new TextEncodingPanel();
                textEncodingPanel.setPreferredSize(new Dimension(536, 358));
                textEncodingPanel.setEncodingList(textEncodingService.getEncodings());
                final OptionsControlPanel optionsControlPanel = new OptionsControlPanel();
                JPanel dialogPanel = WindowUtils.createDialogPanel(textEncodingPanel, optionsControlPanel);
                final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Manage Encodings", Dialog.ModalityType.APPLICATION_MODAL);
                optionsControlPanel.setHandler((OptionsControlHandler.ControlActionType actionType) -> {
                    if (actionType != OptionsControlHandler.ControlActionType.CANCEL) {
                        textEncodingService.setEncodings(textEncodingPanel.getEncodingList());
                        rebuildEncodings();
                        if (actionType == OptionsControlHandler.ControlActionType.SAVE) {
                            preferences.setEncodings(textEncodingPanel.getEncodingList());
                        }
                    }

                    dialog.close();
                    dialog.dispose();
                });
                textEncodingPanel.setAddEncodingsOperation((List<String> usedEncodings) -> {
                    final List<String> result = new ArrayList<>();
                    final AddEncodingPanel addEncodingPanel = new AddEncodingPanel();
                    addEncodingPanel.setUsedEncodings(usedEncodings);
                    DefaultControlPanel encodingsControlPanel = new DefaultControlPanel(addEncodingPanel.getResourceBundle());
                    JPanel encodingDialogPanel = WindowUtils.createDialogPanel(addEncodingPanel, encodingsControlPanel);
                    final DialogWrapper addEncodingDialog = WindowUtils.createDialog(encodingDialogPanel, parentComponent, "Add Encodings", Dialog.ModalityType.APPLICATION_MODAL);
                    encodingsControlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                        if (actionType == DefaultControlHandler.ControlActionType.OK) {
                            result.addAll(addEncodingPanel.getEncodings());
                        }

                        addEncodingDialog.close();
                        addEncodingDialog.dispose();
                    });
                    addEncodingDialog.showCentered(addEncodingPanel);
                    return result;
                });
                dialog.showCentered(parentComponent);
            }
        };
        ActionUtils.setupAction(manageEncodingsAction, resourceBundle, "manageEncodingsAction");
        manageEncodingsAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);

        toolsEncodingMenu = new JMenu();
        toolsEncodingMenu.addSeparator();
        toolsEncodingMenu.add(ActionUtils.actionToMenuItem(manageEncodingsAction));
        toolsEncodingMenu.setText(resourceBundle.getString("toolsEncodingMenu.text"));
        toolsEncodingMenu.setToolTipText(resourceBundle.getString("toolsEncodingMenu.shortDescription"));
        EncodingsHandler.this.rebuildEncodings();
    }

    public void setTextEncodingStatus(TextEncodingStatusApi textEncodingStatus) {
        textEncodingService.setTextEncodingStatus(textEncodingStatus);
    }

    @Nonnull
    public JMenu getToolsEncodingMenu() {
        return toolsEncodingMenu;
    }

    public void rebuildEncodings() {
        for (int i = toolsEncodingMenu.getItemCount() - 3; i >= 0; i--) {
            encodingButtonGroup.remove(toolsEncodingMenu.getItem(i));
            toolsEncodingMenu.remove(i);
        }

        List<String> encodings = textEncodingService.getEncodings();
        if (encodings.isEmpty()) {
            toolsEncodingMenu.add(utfEncodingRadioButtonMenuItem, 0);
            textEncodingService.setSelectedEncoding(StringUtils.ENCODING_UTF8);
            utfEncodingRadioButtonMenuItem.setSelected(true);
        } else {
            int selectedEncodingIndex = encodings.indexOf(textEncodingService.getSelectedEncoding());
            for (int index = 0; index < encodings.size(); index++) {
                String encoding = encodings.get(index);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(encoding);
                item.addActionListener(encodingActionListener);
                item.setToolTipText(ENCODING_TOOLTIP_PREFIX + encoding);
                toolsEncodingMenu.add(item, index);
                encodingButtonGroup.add(item);
                if (index == selectedEncodingIndex) {
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

    public void loadFromPreferences(TextEncodingPreferences preferences) {
        this.preferences = preferences;
        textEncodingService.loadFromPreferences(preferences);
        rebuildEncodings();
    }

    public void cycleEncodings() {
        int menuIndex = 0;
        List<String> encodings = textEncodingService.getEncodings();
        if (!encodings.isEmpty()) {
            int selectedEncodingIndex = encodings.indexOf(textEncodingService.getSelectedEncoding());
            if (selectedEncodingIndex < 0 || selectedEncodingIndex == encodings.size() - 1) {
                textEncodingService.setSelectedEncoding(encodings.get(0));
            } else {
                textEncodingService.setSelectedEncoding(encodings.get(selectedEncodingIndex + 1));
                menuIndex = selectedEncodingIndex;
            }
        }

        updateEncodingsSelection(menuIndex);
    }

    public void popupEncodingsMenu(MouseEvent mouseEvent) {
        JPopupMenu popupMenu = new JPopupMenu();

        List<String> encodings = textEncodingService.getEncodings();
        String selectedEncoding = textEncodingService.getSelectedEncoding();
        if (encodings.isEmpty()) {
            JRadioButtonMenuItem utfEncoding = new JRadioButtonMenuItem(DEFAULT_ENCODING_TEXT, StringUtils.ENCODING_UTF8.equals(selectedEncoding));
            utfEncoding.setToolTipText(ENCODING_TOOLTIP_PREFIX + StringUtils.ENCODING_UTF8);
            utfEncoding.addActionListener(utfEncodingActionListener);
            popupMenu.add(utfEncoding);
        } else {
            int selectedEncodingIndex = encodings.indexOf(selectedEncoding);
            for (int index = 0; index < encodings.size(); index++) {
                String encoding = encodings.get(index);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(encoding, index == selectedEncodingIndex);
                item.addActionListener(encodingActionListener);
                item.setToolTipText(ENCODING_TOOLTIP_PREFIX + encoding);
                popupMenu.add(item, index);
            }
        }

        popupMenu.add(new JSeparator());
        popupMenu.add(ActionUtils.actionToMenuItem(manageEncodingsAction));

        popupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
    }

    public void setEncodings(List<String> encodings) {
        textEncodingService.setEncodings(encodings);
    }

    public void setParentComponent(Component component) {
        parentComponent = component;
    }
}

/*
 * Copyright (C) ExBin Project
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
package org.exbin.bined.intellij.main;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import com.intellij.ide.util.PropertiesComponent;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.framework.api.Preferences;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.api.XBApplicationModuleRepository;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.utils.handler.OkCancelService;

/**
 * Application wrapper.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class Application implements XBApplication {

    public static final String RESOURCES_DIALOG_TITLE = "dialog.title";

    private BinedModule binedModule = new BinedModule();
    private Preferences preferences = new IntelliJPreferencesWrapper(PropertiesComponent.getInstance(), BinEdIntelliJPlugin.PLUGIN_PREFIX);

    private FrameModuleApi frameModule = new FrameModuleApi() {
        @Override
        public void createMainMenu() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void notifyFrameUpdated() {
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public DialogWrapper createDialog() {
            return createDialog(null, null);
        }

        @Nonnull
        @Override
        public DialogWrapper createDialog(@Nullable JPanel panel) {
            return createDialog(getFrame(), Dialog.ModalityType.APPLICATION_MODAL, panel, null);
        }

        @Nonnull
        @Override
        public DialogWrapper createDialog(@Nullable JPanel panel, @Nullable JPanel controlPanel) {
            return createDialog(getFrame(), Dialog.ModalityType.APPLICATION_MODAL, panel, controlPanel);
        }

        @Nonnull
        @Override
        public DialogWrapper createDialog(Component parentComponent, Dialog.ModalityType modalityType, @Nullable JPanel panel) {
            return createDialog(parentComponent, modalityType, panel, null);
        }

        @Nonnull
        @Override
        public DialogWrapper createDialog(Component parentComponent, Dialog.ModalityType modalityType, @Nullable JPanel panel, @Nullable JPanel controlPanel) {
            JPanel dialogPanel = controlPanel != null ? WindowUtils.createDialogPanel(panel, controlPanel) : panel;

            DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "", modalityType);
            Optional<Image> applicationIcon = Application.this.getApplicationIcon();
            if (applicationIcon.isPresent()) {
                ((JDialog) dialog.getWindow()).setIconImage(applicationIcon.get());
            }
            if (controlPanel instanceof OkCancelService) {
                JButton defaultButton = ((OkCancelService) controlPanel).getDefaultButton();
                if (defaultButton != null) {
                    JRootPane rootPane = SwingUtilities.getRootPane(dialog.getWindow());
                    if (rootPane != null) {
                        rootPane.setDefaultButton(defaultButton);
                    }
                }
            }
            return dialog;
        }

        @Override
        public void setDialogTitle(DialogWrapper dialog, ResourceBundle resourceBundle) {
            ((JDialog) dialog.getWindow()).setTitle(resourceBundle.getString(RESOURCES_DIALOG_TITLE));
        }

        @Override
        public Frame getFrame() {
            return null;
        }

        @Override
        public Action getExitAction() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void registerExitAction() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void registerBarsVisibilityActions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void registerToolBarVisibilityActions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void registerStatusBarVisibilityActions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void registerStatusBar(String moduleId, String statusBarId, JPanel panel) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void switchStatusBar(String statusBarId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void loadFramePosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveFramePosition() {
            throw new UnsupportedOperationException();
        }
    };

    public Application() {
    }

    @Nonnull
    @Override
    public Preferences getAppPreferences() {
        return preferences;
    }

    @Nonnull
    public Optional<Image> getApplicationIcon() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public XBApplicationModuleRepository getModuleRepository() {
        return new XBApplicationModuleRepository() {
            @Override
            public Object getModuleRecordById(String moduleId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object getModuleById(String moduleId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T getModuleByInterface(Class<T> interfaceClass) {
                if (interfaceClass.equals(BinedModule.class)) {
                    return (T) binedModule;
                } else if (interfaceClass.equals(FrameModuleApi.class)) {
                    return (T) frameModule;
                }
                throw new UnsupportedOperationException();
            }
        };
    }
}

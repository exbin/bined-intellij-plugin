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
package org.exbin.framework.frame.api;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JPanel;
import org.exbin.framework.api.XBApplicationModule;
import org.exbin.framework.utils.WindowUtils.DialogWrapper;

/**
 * Interface for framework frame module.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface FrameModuleApi extends XBApplicationModule {

    /**
     * Creates and initializes main menu and toolbar.
     */
    void createMainMenu();

    /**
     * Notifies frame was updated.
     */
    void notifyFrameUpdated();

    /**
     * Creates basic dialog and sets it up.
     *
     * @return dialog
     */
    @Nonnull
    DialogWrapper createDialog();

    /**
     * Creates basic dialog and sets it up.
     *
     * @param panel panel
     * @return dialog
     */
    @Nonnull
    DialogWrapper createDialog(@Nullable JPanel panel);

    /**
     * Creates basic dialog and sets it up.
     *
     * @param panel panel
     * @param controlPanel control panel
     * @return dialog
     */
    @Nonnull
    DialogWrapper createDialog(@Nullable JPanel panel, @Nullable JPanel controlPanel);

    /**
     * Creates basic dialog and sets it up.
     *
     * @param parentComponent parent component
     * @param modalityType modality type
     * @param panel panel
     * @return dialog
     */
    @Nonnull
    DialogWrapper createDialog(@Nullable Component parentComponent, Dialog.ModalityType modalityType, @Nullable JPanel panel);

    /**
     * Creates basic dialog and sets it up.
     *
     * @param parentComponent parent component
     * @param controlPanel control panel
     * @param modalityType modality type
     * @param panel panel
     * @return dialog
     */
    @Nonnull
    DialogWrapper createDialog(@Nullable Component parentComponent, Dialog.ModalityType modalityType, @Nullable JPanel panel, @Nullable JPanel controlPanel);

    /**
     * Returns frame instance.
     *
     * @return frame
     */
    @Nonnull
    Frame getFrame();

    /**
     * Returns exit action.
     *
     * @return exit action
     */
    @Nonnull
    Action getExitAction();

    /**
     * Registers exit action in default menu location.
     */
    void registerExitAction();

    void registerBarsVisibilityActions();

    void registerToolBarVisibilityActions();

    void registerStatusBarVisibilityActions();

    /**
     * Registers new status bar with unique ID.
     *
     * @param moduleId module id
     * @param statusBarId statusbar id
     * @param panel panel
     */
    void registerStatusBar(String moduleId, String statusBarId, JPanel panel);

    /**
     * Switches to status bar with specific ID.
     *
     * @param statusBarId statusbar id
     */
    void switchStatusBar(String statusBarId);

    void loadFramePosition();

    void saveFramePosition();

    void setDialogTitle(DialogWrapper dialog, ResourceBundle resourceBundle);
}

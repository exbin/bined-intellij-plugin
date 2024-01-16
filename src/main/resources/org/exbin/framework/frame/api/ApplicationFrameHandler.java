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
import java.awt.Dimension;
import java.awt.Frame;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.utils.gui.WindowHeaderPanel;

/**
 * Interface for application frame.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface ApplicationFrameHandler {

    /**
     * Gets current frame.
     *
     * @return frame
     */
    @Nonnull
    Frame getFrame();

    /**
     * Returns tool bar visibility.
     *
     * @return true if toolbar visible
     */
    boolean isToolBarVisible();

    /**
     * Sets tool bar visibility.
     *
     * @param toolBarVisible toolbar visible
     */
    void setToolBarVisible(boolean toolBarVisible);

    /**
     * Returns status bar visibility.
     *
     * @return true if status visible
     */
    boolean isStatusBarVisible();

    /**
     * Sets status bar visibility.
     *
     * @param statusBarVisible statusbar visible
     */
    void setStatusBarVisible(boolean statusBarVisible);

    /**
     * Returns tool bar captions visibility.
     *
     * @return true if toolbar captions visible
     */
    boolean isToolBarCaptionsVisible();

    /**
     * Sets tool bar captions visibility.
     *
     * @param captionsVisible captions visible
     */
    void setToolBarCaptionsVisible(boolean captionsVisible);

    /**
     * Sets base appplication handler to be used as source of configuration.
     *
     * @param app base application handler
     */
    void setApplication(XBApplication app);

    /**
     * Sets content of central area of the frame.
     *
     * @param component component to use
     */
    void setMainPanel(Component component);

    /**
     * Loads main menu for the frame.
     *
     * @param application application
     */
    void loadMainMenu(XBApplication application);

    /**
     * Loads main tool bar for the frame.
     *
     * @param application application
     */
    void loadMainToolBar(XBApplication application);

    /**
     * Shows this frame.
     */
    void showFrame();

    /**
     * Sets default frame size.
     *
     * @param windowSize window size
     */
    void setDefaultSize(Dimension windowSize);

    /**
     * Sets window header decoration provider.
     *
     * @param windowHeaderDecorationProvider window header decoration provider
     */
    void setWindowHeaderDecorationProvider(WindowHeaderPanel.WindowHeaderDecorationProvider windowHeaderDecorationProvider);
}

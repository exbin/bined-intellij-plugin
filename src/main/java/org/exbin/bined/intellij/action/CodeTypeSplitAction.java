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
package org.exbin.bined.intellij.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionGroupWrapper;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.actionSystem.UpdateSession;
import com.intellij.openapi.actionSystem.ex.ActionButtonLook;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.actionSystem.impl.MenuItemPresentationFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.SimpleMessageBusConnection;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Split button action derived from {@link com.intellij.openapi.actionSystem.SplitButtonAction}.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeTypeSplitAction extends ActionGroupWrapper implements CustomComponentAction {

    private static final Key<AnAction> FIRST_ACTION = Key.create("firstAction");
    private int selectedIndex = -1;
    private CodeTypeSplitAction.SplitButton splitButton = null;

    public CodeTypeSplitAction(ActionGroup actionGroup) {
        super(actionGroup);
        setPopup(true);
    }

    @Nonnull
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public @NotNull ActionGroup getActionGroup() {
        return getDelegate();
    }

    @Nonnull
    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        splitButton = new CodeTypeSplitAction.SplitButton(this, presentation, place, getDelegate());
        splitButton.setSelectedIndex(selectedIndex);
        return splitButton;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        if (splitButton != null) {
            splitButton.setSelectedIndex(selectedIndex);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        UpdateSession session = e.getUpdateSession();
        Presentation presentation = e.getPresentation();
        SplitButton splitButton = ObjectUtils.tryCast(presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY), SplitButton.class);

        Presentation groupPresentation = session.presentation(getDelegate()); // do not remove (RemDev)
        AnAction action = splitButton != null ? splitButton.selectedAction : getFirstEnabledAction(e);
        if (action != null) {
            Presentation actionPresentation = session.presentation(action);
            presentation.copyFrom(actionPresentation, splitButton);
            presentation.setEnabledAndVisible(true);
        }
        else {
            e.getPresentation().copyFrom(groupPresentation, splitButton);
        }
        presentation.putClientProperty(FIRST_ACTION, splitButton != null ? null : action);
    }

    @Override
    public void updateCustomComponent(@NotNull JComponent component, @NotNull Presentation presentation) {
        if (!(component instanceof SplitButton splitButton)) return;
        boolean shouldRepaint = splitButton.actionEnabled != presentation.isEnabled();
        splitButton.actionEnabled = presentation.isEnabled();
        if (shouldRepaint) splitButton.repaint();
    }

    private @Nullable AnAction getFirstEnabledAction(@NotNull AnActionEvent e) {
        UpdateSession session = e.getUpdateSession();
        var children = session.children(getDelegate());
        var firstEnabled = ContainerUtil.find(children, a -> session.presentation(a).isEnabled());
        return firstEnabled != null ? firstEnabled : ContainerUtil.getFirstItem(children);
    }

    @ParametersAreNonnullByDefault
    private static class SplitButton extends ActionButton implements AnActionListener {
        private enum MousePressType {
            Action, Popup, None
        }

        private static final Icon ARROW_DOWN = AllIcons.General.ButtonDropTriangle;

        private final ActionGroup myActionGroup;
        private AnAction selectedAction;
        private int selectedIndex = 0;
        private boolean actionEnabled = true;
        private MousePressType mousePressType = MousePressType.None;
        private SimpleMessageBusConnection myConnection;

        private SplitButton(@NotNull AnAction action, @NotNull Presentation presentation, String place, ActionGroup actionGroup) {
            super(action, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
            myActionGroup = actionGroup;
            selectedAction = presentation.getClientProperty(FIRST_ACTION);
        }

        private void copyPresentation(Presentation presentation) {
            myPresentation.copyFrom(presentation, this);
            actionEnabled = presentation.isEnabled();
            myPresentation.setEnabled(true);
        }

        @Override
        public @NotNull Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width += ARROW_DOWN.getIconWidth() + scale(7);
            return size;
        }

        private boolean selectedActionEnabled() {
            return selectedAction != null && actionEnabled;
        }

        @Override
        public void paintComponent(Graphics g) {
            ActionButtonLook look = getButtonLook();
            if (selectedActionEnabled() || !isUnderDarcula()) {
                int state = getPopState();
                if (state == PUSHED) state = POPPED;
                look.paintBackground(g, this, state);
            }

            Rectangle baseRect = new Rectangle(getSize());
            JBInsets.removeFrom(baseRect, getInsets());

            if (getPopState() == PUSHED && mousePressType != MousePressType.None && selectedActionEnabled() || isToggleActionPushed()) {
                int arrowWidth = ARROW_DOWN.getIconWidth() + scale(7);

                Shape clip = g.getClip();
                Area buttonClip = new Area(clip);
                Rectangle execButtonRect = new Rectangle(baseRect.x, baseRect.y, baseRect.width - arrowWidth, baseRect.height);
                if (mousePressType == MousePressType.Action || isToggleActionPushed()) {
                    buttonClip.intersect(new Area(execButtonRect));
                }
                else if (mousePressType == MousePressType.Popup) {
                    Rectangle arrowButtonRect = new Rectangle(execButtonRect.x + execButtonRect.width, baseRect.y, arrowWidth, baseRect.height);
                    buttonClip.intersect(new Area(arrowButtonRect));
                }

                g.setClip(buttonClip);
                look.paintBackground(g, this, PUSHED);
                g.setClip(clip);
            }

            int x = baseRect.x + baseRect.width - scale(3) - ARROW_DOWN.getIconWidth();
            int y = baseRect.y + (baseRect.height - ARROW_DOWN.getIconHeight()) / 2 + scale(1);
            ARROW_DOWN.paintIcon(this, g, x, y);

            x -= scale(4);
            int popState = getPopState();
            if (popState == POPPED || popState == PUSHED) {
                g.setColor(JBUI.CurrentTheme.ActionButton.hoverSeparatorColor());
                g.fillRect(x, baseRect.y, scale(1), baseRect.height);
            }

            Icon actionIcon = getIcon();
            if (!selectedActionEnabled()) {
                Icon disabledIcon = myPresentation.getDisabledIcon();
                actionIcon = disabledIcon != null || actionIcon == null ? disabledIcon : IconLoader.getDisabledIcon(actionIcon);
                if (actionIcon == null) {
                    actionIcon = getFallbackIcon(false);
                }
            }

            x = baseRect.x + (x - baseRect.x -  actionIcon.getIconWidth()) / 2;
            y = baseRect.y + (baseRect.height - actionIcon.getIconHeight()) / 2;
            look.paintIcon(g, this, actionIcon, x, y);
        }

        private boolean isToggleActionPushed() {
            return selectedAction instanceof Toggleable && Toggleable.isSelected(myPresentation);
        }

        @Override
        protected void onMousePressed(@NotNull MouseEvent e) {
            Rectangle baseRect = new Rectangle(getSize());
            JBInsets.removeFrom(baseRect, getInsets());
            int arrowWidth = ARROW_DOWN.getIconWidth() + scale(7);

            Rectangle execButtonRect = new Rectangle(baseRect.x, baseRect.y, baseRect.width - arrowWidth, baseRect.height);
            Rectangle arrowButtonRect = new Rectangle(execButtonRect.x + execButtonRect.width, baseRect.y, arrowWidth, baseRect.height);

            Point p = e.getPoint();
            mousePressType = execButtonRect.contains(p) ? MousePressType.Action :
                    arrowButtonRect.contains(p) ? MousePressType.Popup :
                            MousePressType.None;
        }

        @Override
        protected void actionPerformed(@NotNull AnActionEvent event) {
            HelpTooltip.hide(this);

            if (mousePressType == MousePressType.Popup || !selectedActionEnabled()) {
                showActionGroupPopup(myActionGroup, event);
            }
            else {
                // Cycle actions
                final AnActionEvent newEvent = AnActionEvent.createFromInputEvent(event.getInputEvent(), myPlace, event.getPresentation(), getDataContext());
                AnAction[] actions = myActionGroup.getChildren(null);
                if (selectedIndex >= actions.length -1) {
                    selectedIndex = 0;
                } else {
                    selectedIndex++;
                }
                selectedAction = actions[selectedIndex];
                myPresentation.setIcon(selectedAction.getTemplatePresentation().getIcon());
                myPresentation.setText(selectedAction.getTemplatePresentation().getText());
                ApplicationManager.getApplication().invokeLater(() -> {
                    selectedAction.actionPerformed(newEvent);
                });
            }
        }

        public void setSelectedIndex(int selectedIndex) {
            this.selectedIndex = selectedIndex;
            AnAction[] actions = myActionGroup.getChildren(null);
            selectedAction = actions[selectedIndex];
            myPresentation.setIcon(selectedAction.getTemplatePresentation().getIcon());
            myPresentation.setText(selectedAction.getTemplatePresentation().getText());
        }

        @Override
        protected void showActionGroupPopup(@NotNull ActionGroup actionGroup, @NotNull AnActionEvent event) {
            if (myPopupState.isRecentlyHidden()) return; // do not show new popup
            ActionManagerImpl am = (ActionManagerImpl) ActionManager.getInstance();
            ActionPopupMenu popupMenu = am.createActionPopupMenu(event.getPlace(), actionGroup, new MenuItemPresentationFactory() {
                @Override
                protected void processPresentation(@NotNull Presentation presentation) {
                    super.processPresentation(presentation);
                    if (StringUtil.defaultIfEmpty(presentation.getText(), "").equals(myPresentation.getText()) &&
                            StringUtil.defaultIfEmpty(presentation.getDescription(), "").equals(myPresentation.getDescription())) {
                        presentation.setEnabled(selectedActionEnabled());
                    }
                }
            });
            popupMenu.setTargetComponent(this);

            JPopupMenu menu = popupMenu.getComponent();
            myPopupState.prepareToShow(menu);
            if (event.isFromActionToolbar()) {
                menu.show(this, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.width + getInsets().left, getHeight());
            }
            else {
                JBPopupMenu.showAtRight(this, menu);
            }

            HelpTooltip.setMasterPopupOpenCondition(this, () -> !menu.isVisible());
        }

        @Override
        public void addNotify() {
            super.addNotify();
            DataContext context = DataManager.getInstance().getDataContext(getParent());
            Disposable parentDisposable = Objects.requireNonNullElse(CommonDataKeys.PROJECT.getData(context), ApplicationManager.getApplication());
            myConnection = ApplicationManager.getApplication().getMessageBus().connect(parentDisposable);
            myConnection.subscribe(AnActionListener.TOPIC, new AnActionListener() {
                @Override
                public void beforeActionPerformed(@NotNull AnAction action, @NotNull AnActionEvent event) {
                    if (event.getDataContext().getData(PlatformCoreDataKeys.CONTEXT_COMPONENT) == SplitButton.this) {
                        selectedAction = action;
                        copyPresentation(event.getPresentation());
                        repaint();
                    }
                }
            });
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            if (myConnection != null) {
                myConnection.disconnect();
                myConnection = null;
            }
        }
    }

    private static boolean isUnderDarcula() {
        return UIManager.getLookAndFeel().getName().contains("Darcula");
    }

    /**
     * Best effort attempt to detect scale...
     */
    public static int scale(int i) {
        try {
            return JBUI.scale(i);
        } catch (Exception ex) {
            Logger.getLogger(CodeTypeSplitAction.class.getName()).log(Level.SEVERE, "Unable to use JBUI class", ex);
        }
        return i;
    }
}

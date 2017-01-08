/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.framework.gui.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import org.exbin.framework.gui.utils.panel.WindowHeaderPanel;

/**
 * Utility static methods usable for windows and dialogs.
 *
 * @version 0.2.0 2016/12/27
 * @author ExBin Project (http://exbin.org)
 */
public class WindowUtils {

    private static final int BUTTON_CLICK_TIME = 150;
    private static LookAndFeel lookAndFeel = null;

    public static void addHeaderPanel(JDialog dialog, ResourceBundle resourceBundle) {
        addHeaderPanel(dialog, resourceBundle.getString("header.title"), resourceBundle.getString("header.description"), resourceBundle.getString("header.icon"));
    }

    public static void addHeaderPanel(JDialog dialog, String headerTitle, String headerDescription, String headerIcon) {
        WindowHeaderPanel headerPanel = new WindowHeaderPanel();
        headerPanel.setTitle(headerTitle);
        headerPanel.setDescription(headerDescription);
        if (!headerIcon.isEmpty()) {
            headerPanel.setIcon(new ImageIcon(dialog.getClass().getResource(headerIcon)));
        }
        if (dialog instanceof WindowHeaderPanel.WindowHeaderDecorationProvider) {
            ((WindowHeaderPanel.WindowHeaderDecorationProvider) dialog).setHeaderDecoration(headerPanel);
        } else {
            Frame frame = getFrame(dialog);
            if (frame instanceof WindowHeaderPanel.WindowHeaderDecorationProvider) {
                ((WindowHeaderPanel.WindowHeaderDecorationProvider) frame).setHeaderDecoration(headerPanel);
            }
        }
        int height = dialog.getHeight() + headerPanel.getPreferredSize().height;
        dialog.getContentPane().add(headerPanel, java.awt.BorderLayout.PAGE_START);
        dialog.setSize(dialog.getWidth(), height);
    }

    private WindowUtils() {
    }

    public static void invokeWindow(final Window window) {
        if (lookAndFeel != null) {
            try {
                javax.swing.UIManager.setLookAndFeel(lookAndFeel);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(WindowUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (window instanceof JDialog) {
                    ((JDialog) window).setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                }

                window.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                window.setVisible(true);
            }
        });
    }

    public static JDialog createDialog(final Component component, Window parent, Dialog.ModalityType modalityType) {
        JDialog dialog = new JDialog(parent, modalityType);
        Dimension size = component.getPreferredSize();
        dialog.add(component);
        dialog.setSize(size.width + 8, size.height + 24);
        return dialog;
    }

    public static JDialog createDialog(final Component component) {
        JDialog dialog = new JDialog();
        Dimension size = component.getPreferredSize();
        dialog.add(component);
        dialog.setSize(size.width + 8, size.height + 24);
        return dialog;
    }

    public static void invokeDialog(final Component component) {
        JDialog dialog = createDialog(component);
        invokeWindow(dialog);
    }

    public static void initWindow(Window window) {
//        if (window.getParent() instanceof XBEditorFrame) {
//            window.setIconImage(((XBEditorFrame) window.getParent()).getMainFrameManagement().getFrameIcon());
//        }
    }

    public static LookAndFeel getLookAndFeel() {
        return lookAndFeel;
    }

    public static void setLookAndFeel(LookAndFeel lookAndFeel) {
        WindowUtils.lookAndFeel = lookAndFeel;
    }

    public static void closeWindow(Window window) {
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }

    public static JDialog createBasicDialog() {
        JDialog dialog = new JDialog(new javax.swing.JFrame(), true);
        dialog.setSize(640, 480);
        dialog.setLocationByPlatform(true);
        return dialog;
    }

    /**
     * Find frame component for given component.
     *
     * @param component instantiated component
     * @return frame instance if found
     */
    public static Frame getFrame(Component component) {
        Component parentComponent = SwingUtilities.getWindowAncestor(component);
        while (!(parentComponent == null || parentComponent instanceof Frame)) {
            parentComponent = parentComponent.getParent();
        }
        return (Frame) parentComponent;
    }

    /**
     * Assign ESCAPE/ENTER key for all focusable components recursively.
     *
     * @param component target component
     * @param closeButton button which will be used for closing operation
     */
    public static void assignGlobalKeyListener(Container component, final JButton closeButton) {
        assignGlobalKeyListener(component, closeButton, closeButton);
    }

    /**
     * Assign ESCAPE/ENTER key for all focusable components recursively.
     *
     * @param component target component
     * @param okButton button which will be used for default ENTER
     * @param cancelButton button which will be used for closing operation
     */
    public static void assignGlobalKeyListener(Container component, final JButton okButton, final JButton cancelButton) {
        assignGlobalKeyListener(component, new OkCancelListener() {
            @Override
            public void okEvent() {
                doButtonClick(okButton);
            }

            @Override
            public void cancelEvent() {
                doButtonClick(cancelButton);
            }
        });
    }

    /**
     * Assign ESCAPE/ENTER key for all focusable components recursively.
     *
     * @param component target component
     * @param listener ok and cancel event listener
     */
    public static void assignGlobalKeyListener(Container component, final OkCancelListener listener) {
        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    boolean performOkAction = true;

                    if (evt.getSource() instanceof JButton) {
                        ((JButton) evt.getSource()).doClick(BUTTON_CLICK_TIME);
                        performOkAction = false;
                    } else if (evt.getSource() instanceof JTextArea) {
                        performOkAction = !((JTextArea) evt.getSource()).isEditable();
                    } else if (evt.getSource() instanceof JTextPane) {
                        performOkAction = !((JTextPane) evt.getSource()).isEditable();
                    } else if (evt.getSource() instanceof JEditorPane) {
                        performOkAction = !((JEditorPane) evt.getSource()).isEditable();
                    }

                    if (performOkAction && listener != null) {
                        listener.okEvent();
                    }

                } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE && listener != null) {
                    listener.cancelEvent();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        };

        assignGlobalKeyListener(component, keyListener);
    }

    /**
     * Assign key listener for all focusable components recursively.
     *
     * @param component target component
     * @param keyListener key lisneter
     */
    public static void assignGlobalKeyListener(Container component, KeyListener keyListener) {
        Component[] comps = component.getComponents();
        for (Component item : comps) {
            if (item.isFocusable()) {
                item.addKeyListener(keyListener);
            }

            if (item instanceof Container) {
                assignGlobalKeyListener((Container) item, keyListener);
            }
        }
    }

    /**
     * Performs visually visible click on the button component.
     *
     * @param button button component
     */
    public static void doButtonClick(JButton button) {
        button.doClick(BUTTON_CLICK_TIME);
    }

    public static WindowPosition getWindowPosition(Window window) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();
        int windowX = window.getX();
        int windowY = window.getY();
        int screenX = 0;
        int screenY = 0;
        int screenWidth = 0;
        int screenHeight = 0;
        int screenIndex = 0;
        for (GraphicsDevice screen : screenDevices) {
            Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            if (bounds.contains(windowX, windowY)) {
                screenX = bounds.x;
                screenY = bounds.y;
                screenWidth = bounds.width;
                screenHeight = bounds.height;
                break;
            }
            screenIndex++;
        }
        WindowPosition position = new WindowPosition();
        position.setScreenIndex(screenIndex);
        position.setScreenWidth(screenWidth);
        position.setScreenHeight(screenHeight);
        position.setRelativeX(window.getX() - screenX);
        position.setRelativeY(window.getY() - screenY);
        position.setWidth(window.getWidth());
        position.setHeight(window.getHeight());
        position.setMaximized(window instanceof Frame ? (((Frame) window).getExtendedState() & JFrame.MAXIMIZED_BOTH) > 0 : false);
        return position;
    }

    public static void setWindowPosition(Window window, WindowPosition position) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();
        GraphicsDevice device;
        if (screenDevices.length > position.getScreenIndex()) {
            device = screenDevices[position.getScreenIndex()];
        } else {
            device = ge.getDefaultScreenDevice();
        }
        Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
        double absoluteX = position.getScreenWidth() > 0
                ? screenBounds.x + position.getRelativeX() * screenBounds.width / position.getScreenWidth()
                : screenBounds.x + position.getRelativeX();
        double absoluteY = position.getScreenHeight() > 0
                ? screenBounds.y + position.getRelativeY() * screenBounds.height / position.getScreenHeight()
                : screenBounds.y + position.getRelativeY();
        double widthX = position.getScreenWidth() > 0
                ? position.getWidth() * screenBounds.width / position.getScreenWidth()
                : position.getWidth();
        double widthY = position.getScreenHeight() > 0
                ? position.getHeight() * screenBounds.height / position.getScreenHeight()
                : position.getHeight();
        if (position.isMaximized()) {
            window.setLocation((int) absoluteX, (int) absoluteY);
            if (window instanceof Frame) {
                ((Frame) window).setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                // TODO if (window instanceof JDialog) 
            }
        } else {
            window.setBounds((int) absoluteX, (int) absoluteY, (int) widthX, (int) widthY);
        }
    }

    /**
     * Creates panel for given main and control panel.
     *
     * @param mainPanel main panel
     * @param controlPanel control panel
     * @return panel
     */
    public static JPanel createDialogPanel(JPanel mainPanel, JPanel controlPanel) {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(mainPanel, BorderLayout.CENTER);
        dialogPanel.add(controlPanel, BorderLayout.SOUTH);
        return dialogPanel;
    }

    public static interface OkCancelListener {

        void okEvent();

        void cancelEvent();
    }
}

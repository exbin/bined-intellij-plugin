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
package org.exbin.framework.gui.menu.component;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/**
 * Drop down button.
 *
 * @version 0.2.1 2019/08/18
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DropDownButton extends JButton {

    private final DropDownButtonPanel buttonPanel;
    private final JPopupMenu popupMenu;

    public DropDownButton(Action action, JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
        buttonPanel = new DropDownButtonPanel();

        init(action);
    }

    private void init(Action action) {
        setFocusable(false);
        setActionText((String) action.getValue(Action.NAME));
        addActionListener(action);

        setMargin(new Insets(0, 0, 0, 0));
        add(buttonPanel);
        JLabel actionButton = buttonPanel.getActionButton();
        JButton menuButton = buttonPanel.getMenuButton();

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
            }

            @Override
            public void mousePressed(MouseEvent me) {
                if (me.getSource() == actionButton) {
                    menuButton.setSelected(true);
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (me.getSource() == actionButton) {
                    menuButton.setSelected(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                setRolloverBorder();
            }

            @Override
            public void mouseExited(MouseEvent me) {
                unsetRolloverBorder();
            }
        };

        actionButton.addMouseListener(ma);
        menuButton.addMouseListener(ma);

        menuButton.addActionListener((ActionEvent ae) -> {
            popupMenu.show(actionButton, 0, actionButton.getSize().height);
        });
    }

    protected void setRolloverBorder() {
        JButton menuButton = buttonPanel.getMenuButton();
        menuButton.setBorderPainted(true);
    }

    protected void unsetRolloverBorder() {
        JButton menuButton = buttonPanel.getMenuButton();
        menuButton.setBorderPainted(false);
    }

    public void setActionText(String value) {
        buttonPanel.getActionButton().setText(" " + value + " ");
    }

    public void setActionTooltip(String text) {
        buttonPanel.getActionButton().setToolTipText(text);
        buttonPanel.getMenuButton().setToolTipText(text);
    }
}

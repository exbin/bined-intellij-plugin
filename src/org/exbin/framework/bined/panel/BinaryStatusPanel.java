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
package org.exbin.framework.bined.panel;

import org.exbin.framework.bined.StatusDocumentSizeFormat;
import org.exbin.framework.bined.StatusCursorPositionFormat;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JToolTip;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.options.impl.StatusOptionsImpl;
import org.exbin.framework.bined.preferences.StatusPreferences;

/**
 * Binary editor status panel.
 *
 * @version 0.2.1 2019/06/18
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryStatusPanel extends javax.swing.JPanel implements BinaryStatusApi, TextEncodingStatusApi {

    public static final String INSERT_EDITATION_MODE_LABEL = "INS";
    public static final String OVERWRITE_EDITATION_MODE_LABEL = "OVR";
    public static final String READONLY_EDITATION_MODE_LABEL = "RO";
    public static final String INPLACE_EDITATION_MODE_LABEL = "INP";

    public static final String OCTAL_CODE_TYPE_LABEL = "OCT";
    public static final String DECIMAL_CODE_TYPE_LABEL = "DEC";
    public static final String HEXADECIMAL_CODE_TYPE_LABEL = "HEX";

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinaryStatusPanel.class);

    private StatusPreferences statusParameters;
    private StatusControlHandler statusControlHandler;

    private StatusCursorPositionFormat cursorPositionFormat = new StatusCursorPositionFormat();
    private StatusDocumentSizeFormat documentSizeFormat = new StatusDocumentSizeFormat();
    private int octalSpaceGroupSize = StatusOptionsImpl.DEFAULT_OCTAL_SPACE_GROUP_SIZE;
    private int decimalSpaceGroupSize = StatusOptionsImpl.DEFAULT_DECIMAL_SPACE_GROUP_SIZE;
    private int hexadecimalSpaceGroupSize = StatusOptionsImpl.DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE;

    private EditationOperation editationOperation;
    private CodeAreaCaretPosition caretPosition;
    private long documentSize;
    private long initialDocumentSize;

    public BinaryStatusPanel() {
        initComponents();
    }

    public void loadFromPreferences(StatusPreferences statusParameters) {
        this.statusParameters = statusParameters;
        cursorPositionFormat.setCodeType(statusParameters.getCursorPositionCodeType());
        cursorPositionFormat.setShowOffset(statusParameters.isCursorShowOffset());
        documentSizeFormat.setCodeType(statusParameters.getDocumentSizeCodeType());
        documentSizeFormat.setShowRelative(statusParameters.isDocumentSizeShowRelative());
        octalSpaceGroupSize = statusParameters.getOctalSpaceGroupSize();
        decimalSpaceGroupSize = statusParameters.getDecimalSpaceGroupSize();
        hexadecimalSpaceGroupSize = statusParameters.getHexadecimalSpaceGroupSize();
        updateStatus();
    }

    public void updateStatus() {
        updateCaretPosition();
        updateCursorPositionToolTip();
        updateDocumentSize();
        updateDocumentSizeToolTip();

        switch (cursorPositionFormat.getCodeType()) {
            case OCTAL: {
                octalCursorPositionModeRadioButtonMenuItem.setSelected(true);
                break;
            }
            case DECIMAL: {
                decimalCursorPositionModeRadioButtonMenuItem.setSelected(true);
                break;
            }
            case HEXADECIMAL: {
                hexadecimalCursorPositionModeRadioButtonMenuItem.setSelected(true);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type " + cursorPositionFormat.getCodeType());
        }
        cursorPositionShowOffsetCheckBoxMenuItem.setSelected(cursorPositionFormat.isShowOffset());

        switch (documentSizeFormat.getCodeType()) {
            case OCTAL: {
                octalDocumentSizeModeRadioButtonMenuItem.setSelected(true);
                break;
            }
            case DECIMAL: {
                decimalDocumentSizeModeRadioButtonMenuItem.setSelected(true);
                break;
            }
            case HEXADECIMAL: {
                hexadecimalDocumentSizeModeRadioButtonMenuItem.setSelected(true);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type " + documentSizeFormat.getCodeType());
        }
        documentSizeShowRelativeCheckBoxMenuItem.setSelected(documentSizeFormat.isShowRelative());
    }

    public void setStatusOptions(StatusOptions statusOptions) {
        cursorPositionFormat = statusOptions.getCursorPositionFormat();
        documentSizeFormat = statusOptions.getDocumentSizeFormat();
        octalSpaceGroupSize = statusOptions.getOctalSpaceGroupSize();
        decimalSpaceGroupSize = statusOptions.getDecimalSpaceGroupSize();
        hexadecimalSpaceGroupSize = statusOptions.getHexadecimalSpaceGroupSize();
        updateStatus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        positionPopupMenu = new javax.swing.JPopupMenu();
        cursorPositionCodeTypeMenu = new javax.swing.JMenu();
        octalCursorPositionModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        decimalCursorPositionModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        hexadecimalCursorPositionModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        cursorPositionShowOffsetCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        positionCopyMenuItem = new javax.swing.JMenuItem();
        positionGoToMenuItem = new javax.swing.JMenuItem();
        documentSizePopupMenu = new javax.swing.JPopupMenu();
        documentSizeCodeTypeMenu = new javax.swing.JMenu();
        octalDocumentSizeModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        decimalDocumentSizeModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        hexadecimalDocumentSizeModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        documentSizeShowRelativeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        documentSizeCopyMenuItem = new javax.swing.JMenuItem();
        memoryModePopupMenu = new javax.swing.JPopupMenu();
        deltaMemoryModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        ramMemoryModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        memoryModeButtonGroup = new javax.swing.ButtonGroup();
        documentSizeModeButtonGroup = new javax.swing.ButtonGroup();
        cursorPositionModeButtonGroup = new javax.swing.ButtonGroup();
        memoryModeLabel = new javax.swing.JLabel();
        documentSizeLabel = new javax.swing.JLabel() {
            @Override
            public JToolTip createToolTip() {
                updateDocumentSizeToolTip();
                return super.createToolTip();
            }
        }
        ;
        cursorPositionLabel = new javax.swing.JLabel() {
            @Override
            public JToolTip createToolTip() {
                updateCursorPositionToolTip();
                return super.createToolTip();
            }
        }
        ;
        editationModeLabel = new javax.swing.JLabel();
        encodingLabel = new javax.swing.JLabel();

        positionPopupMenu.setName("positionPopupMenu"); // NOI18N

        cursorPositionCodeTypeMenu.setText(resourceBundle.getString("cursorPositionCodeTypeMenu.text")); // NOI18N
        cursorPositionCodeTypeMenu.setName("cursorPositionCodeTypeMenu"); // NOI18N

        cursorPositionModeButtonGroup.add(octalCursorPositionModeRadioButtonMenuItem);
        octalCursorPositionModeRadioButtonMenuItem.setText(resourceBundle.getString("octalCursorPositionModeRadioButtonMenuItem.text")); // NOI18N
        octalCursorPositionModeRadioButtonMenuItem.setName("octalCursorPositionModeRadioButtonMenuItem"); // NOI18N
        octalCursorPositionModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                octalCursorPositionModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        cursorPositionCodeTypeMenu.add(octalCursorPositionModeRadioButtonMenuItem);

        cursorPositionModeButtonGroup.add(decimalCursorPositionModeRadioButtonMenuItem);
        decimalCursorPositionModeRadioButtonMenuItem.setSelected(true);
        decimalCursorPositionModeRadioButtonMenuItem.setText(resourceBundle.getString("decimalCursorPositionModeRadioButtonMenuItem.text")); // NOI18N
        decimalCursorPositionModeRadioButtonMenuItem.setName("decimalCursorPositionModeRadioButtonMenuItem"); // NOI18N
        decimalCursorPositionModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decimalCursorPositionModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        cursorPositionCodeTypeMenu.add(decimalCursorPositionModeRadioButtonMenuItem);

        cursorPositionModeButtonGroup.add(hexadecimalCursorPositionModeRadioButtonMenuItem);
        hexadecimalCursorPositionModeRadioButtonMenuItem.setText(resourceBundle.getString("hexadecimalCursorPositionModeRadioButtonMenuItem.text")); // NOI18N
        hexadecimalCursorPositionModeRadioButtonMenuItem.setName("hexadecimalCursorPositionModeRadioButtonMenuItem"); // NOI18N
        hexadecimalCursorPositionModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexadecimalCursorPositionModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        cursorPositionCodeTypeMenu.add(hexadecimalCursorPositionModeRadioButtonMenuItem);

        positionPopupMenu.add(cursorPositionCodeTypeMenu);

        cursorPositionShowOffsetCheckBoxMenuItem.setSelected(true);
        cursorPositionShowOffsetCheckBoxMenuItem.setText(resourceBundle.getString("cursorPositionShowOffsetCheckBoxMenuItem.text")); // NOI18N
        cursorPositionShowOffsetCheckBoxMenuItem.setName("cursorPositionShowOffsetCheckBoxMenuItem"); // NOI18N
        cursorPositionShowOffsetCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cursorPositionShowOffsetCheckBoxMenuItemActionPerformed(evt);
            }
        });
        positionPopupMenu.add(cursorPositionShowOffsetCheckBoxMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        positionPopupMenu.add(jSeparator2);

        positionCopyMenuItem.setText(resourceBundle.getString("positionCopyMenuItem.text")); // NOI18N
        positionCopyMenuItem.setName("positionCopyMenuItem"); // NOI18N
        positionCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionCopyMenuItemActionPerformed(evt);
            }
        });
        positionPopupMenu.add(positionCopyMenuItem);

        positionGoToMenuItem.setText(resourceBundle.getString("positionGoToMenuItem.text")); // NOI18N
        positionGoToMenuItem.setName("positionGoToMenuItem"); // NOI18N
        positionGoToMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionGoToMenuItemActionPerformed(evt);
            }
        });
        positionPopupMenu.add(positionGoToMenuItem);

        documentSizePopupMenu.setName("documentSizePopupMenu"); // NOI18N

        documentSizeCodeTypeMenu.setText(resourceBundle.getString("documentSizecodeTypeMenu.text")); // NOI18N
        documentSizeCodeTypeMenu.setName("documentSizeCodeTypeMenu"); // NOI18N

        documentSizeModeButtonGroup.add(octalDocumentSizeModeRadioButtonMenuItem);
        octalDocumentSizeModeRadioButtonMenuItem.setText(resourceBundle.getString("octDocumentSizeModeRadioButtonMenuItem.text")); // NOI18N
        octalDocumentSizeModeRadioButtonMenuItem.setName("octalDocumentSizeModeRadioButtonMenuItem"); // NOI18N
        octalDocumentSizeModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                octalDocumentSizeModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        documentSizeCodeTypeMenu.add(octalDocumentSizeModeRadioButtonMenuItem);

        documentSizeModeButtonGroup.add(decimalDocumentSizeModeRadioButtonMenuItem);
        decimalDocumentSizeModeRadioButtonMenuItem.setText(resourceBundle.getString("decDocumentSizeModeRadioButtonMenuItem1.text")); // NOI18N
        decimalDocumentSizeModeRadioButtonMenuItem.setName("decimalDocumentSizeModeRadioButtonMenuItem"); // NOI18N
        decimalDocumentSizeModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decimalDocumentSizeModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        documentSizeCodeTypeMenu.add(decimalDocumentSizeModeRadioButtonMenuItem);

        documentSizeModeButtonGroup.add(hexadecimalDocumentSizeModeRadioButtonMenuItem);
        hexadecimalDocumentSizeModeRadioButtonMenuItem.setText(resourceBundle.getString("hexDocumentSizeModeRadioButtonMenuItem1.text")); // NOI18N
        hexadecimalDocumentSizeModeRadioButtonMenuItem.setName("hexadecimalDocumentSizeModeRadioButtonMenuItem"); // NOI18N
        hexadecimalDocumentSizeModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexadecimalDocumentSizeModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        documentSizeCodeTypeMenu.add(hexadecimalDocumentSizeModeRadioButtonMenuItem);

        documentSizePopupMenu.add(documentSizeCodeTypeMenu);

        documentSizeShowRelativeCheckBoxMenuItem.setSelected(true);
        documentSizeShowRelativeCheckBoxMenuItem.setText(resourceBundle.getString("showRelativeCheckBoxMenuItem.text")); // NOI18N
        documentSizeShowRelativeCheckBoxMenuItem.setName("documentSizeShowRelativeCheckBoxMenuItem"); // NOI18N
        documentSizeShowRelativeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentSizeShowRelativeCheckBoxMenuItemActionPerformed(evt);
            }
        });
        documentSizePopupMenu.add(documentSizeShowRelativeCheckBoxMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        documentSizePopupMenu.add(jSeparator1);

        documentSizeCopyMenuItem.setText(resourceBundle.getString("documentSizeCopyMenuItem.text")); // NOI18N
        documentSizeCopyMenuItem.setName("documentSizeCopyMenuItem"); // NOI18N
        documentSizeCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentSizeCopyMenuItemActionPerformed(evt);
            }
        });
        documentSizePopupMenu.add(documentSizeCopyMenuItem);

        memoryModePopupMenu.setName("memoryModePopupMenu"); // NOI18N

        memoryModeButtonGroup.add(deltaMemoryModeRadioButtonMenuItem);
        deltaMemoryModeRadioButtonMenuItem.setSelected(true);
        deltaMemoryModeRadioButtonMenuItem.setText(resourceBundle.getString("deltaMemoryModeRadioButtonMenuItem.text")); // NOI18N
        deltaMemoryModeRadioButtonMenuItem.setName("deltaMemoryModeRadioButtonMenuItem"); // NOI18N
        deltaMemoryModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deltaMemoryModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        memoryModePopupMenu.add(deltaMemoryModeRadioButtonMenuItem);

        memoryModeButtonGroup.add(ramMemoryModeRadioButtonMenuItem);
        ramMemoryModeRadioButtonMenuItem.setText(resourceBundle.getString("ramMemoryModeRadioButtonMenuItem.text")); // NOI18N
        ramMemoryModeRadioButtonMenuItem.setName("ramMemoryModeRadioButtonMenuItem"); // NOI18N
        ramMemoryModeRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ramMemoryModeRadioButtonMenuItemActionPerformed(evt);
            }
        });
        memoryModePopupMenu.add(ramMemoryModeRadioButtonMenuItem);

        setName("Form"); // NOI18N

        memoryModeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        memoryModeLabel.setText(resourceBundle.getString("memoryModeLabel.text")); // NOI18N
        memoryModeLabel.setToolTipText(resourceBundle.getString("memoryModeLabel.toolTipText")); // NOI18N
        memoryModeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        memoryModeLabel.setComponentPopupMenu(memoryModePopupMenu);
        memoryModeLabel.setName("memoryModeLabel"); // NOI18N

        documentSizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        documentSizeLabel.setText("0 (0)");
        documentSizeLabel.setToolTipText(resourceBundle.getString("documentSizeLabel.toolTipText")); // NOI18N
        documentSizeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        documentSizeLabel.setComponentPopupMenu(documentSizePopupMenu);
        documentSizeLabel.setName("documentSizeLabel"); // NOI18N

        cursorPositionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cursorPositionLabel.setText("0:0");
        cursorPositionLabel.setToolTipText(resourceBundle.getString("cursorPositionLabel.toolTipText")); // NOI18N
        cursorPositionLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cursorPositionLabel.setComponentPopupMenu(positionPopupMenu);
        cursorPositionLabel.setName("cursorPositionLabel"); // NOI18N
        cursorPositionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cursorPositionLabelMouseClicked(evt);
            }
        });

        editationModeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        editationModeLabel.setText("OVR");
        editationModeLabel.setToolTipText(resourceBundle.getString("editationModeLabel.toolTipText")); // NOI18N
        editationModeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        editationModeLabel.setName("editationModeLabel"); // NOI18N
        editationModeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editationModeLabelMouseClicked(evt);
            }
        });

        encodingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        encodingLabel.setText(resourceBundle.getString("encodingLabel.text")); // NOI18N
        encodingLabel.setToolTipText(resourceBundle.getString("encodingLabel.toolTipText")); // NOI18N
        encodingLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        encodingLabel.setName("encodingLabel"); // NOI18N
        encodingLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                encodingLabelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                encodingLabelMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encodingLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(195, Short.MAX_VALUE)
                .addComponent(encodingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(documentSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(cursorPositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(memoryModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(editationModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editationModeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(documentSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(memoryModeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cursorPositionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(encodingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void editationModeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editationModeLabelMouseClicked
        if (statusControlHandler != null && evt.getButton() == MouseEvent.BUTTON1) {
            if (editationOperation == EditationOperation.INSERT) {
                statusControlHandler.changeEditationOperation(EditationOperation.OVERWRITE);
            } else if (editationOperation == EditationOperation.OVERWRITE) {
                statusControlHandler.changeEditationOperation(EditationOperation.INSERT);
            }
        }
    }//GEN-LAST:event_editationModeLabelMouseClicked

    private void cursorPositionLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cursorPositionLabelMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() > 1) {
            statusControlHandler.changeCursorPosition();
        }
    }//GEN-LAST:event_cursorPositionLabelMouseClicked

    private void positionGoToMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionGoToMenuItemActionPerformed
        statusControlHandler.changeCursorPosition();
    }//GEN-LAST:event_positionGoToMenuItemActionPerformed

    private void positionCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionCopyMenuItemActionPerformed
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(cursorPositionLabel.getText()), null);
        } catch (IllegalStateException ex) {
            // ignore issues with clipboard
        }
    }//GEN-LAST:event_positionCopyMenuItemActionPerformed

    private void documentSizeCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_documentSizeCopyMenuItemActionPerformed
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(documentSizeLabel.getText()), null);
        } catch (IllegalStateException ex) {
            // ignore issues with clipboard
        }
    }//GEN-LAST:event_documentSizeCopyMenuItemActionPerformed

    private void encodingLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encodingLabelMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1) {
            statusControlHandler.cycleEncodings();
        } else {
            handleEncodingPopup(evt);
        }
    }//GEN-LAST:event_encodingLabelMouseClicked

    private void encodingLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encodingLabelMousePressed
        handleEncodingPopup(evt);
    }//GEN-LAST:event_encodingLabelMousePressed

    private void encodingLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encodingLabelMouseReleased
        handleEncodingPopup(evt);
    }//GEN-LAST:event_encodingLabelMouseReleased

    private void deltaMemoryModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deltaMemoryModeRadioButtonMenuItemActionPerformed
        statusControlHandler.changeMemoryMode(MemoryMode.DELTA_MODE);
    }//GEN-LAST:event_deltaMemoryModeRadioButtonMenuItemActionPerformed

    private void ramMemoryModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ramMemoryModeRadioButtonMenuItemActionPerformed
        statusControlHandler.changeMemoryMode(MemoryMode.RAM_MEMORY);
    }//GEN-LAST:event_ramMemoryModeRadioButtonMenuItemActionPerformed

    private void cursorPositionShowOffsetCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cursorPositionShowOffsetCheckBoxMenuItemActionPerformed
        cursorPositionFormat.setShowOffset(cursorPositionShowOffsetCheckBoxMenuItem.isSelected());
        updateCaretPosition();
        statusParameters.setCursorShowOffset(cursorPositionFormat.isShowOffset());
    }//GEN-LAST:event_cursorPositionShowOffsetCheckBoxMenuItemActionPerformed

    private void documentSizeShowRelativeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_documentSizeShowRelativeCheckBoxMenuItemActionPerformed
        documentSizeFormat.setShowRelative(documentSizeShowRelativeCheckBoxMenuItem.isSelected());
        updateDocumentSize();
        updateDocumentSizeToolTip();
        statusParameters.setDocumentSizeShowRelative(documentSizeFormat.isShowRelative());
    }//GEN-LAST:event_documentSizeShowRelativeCheckBoxMenuItemActionPerformed

    private void octalCursorPositionModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_octalCursorPositionModeRadioButtonMenuItemActionPerformed
        cursorPositionFormat.setCodeType(PositionCodeType.OCTAL);
        updateCaretPosition();
        statusParameters.setCursorPositionCodeType(cursorPositionFormat.getCodeType());
    }//GEN-LAST:event_octalCursorPositionModeRadioButtonMenuItemActionPerformed

    private void decimalCursorPositionModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decimalCursorPositionModeRadioButtonMenuItemActionPerformed
        cursorPositionFormat.setCodeType(PositionCodeType.DECIMAL);
        updateCaretPosition();
        statusParameters.setCursorPositionCodeType(cursorPositionFormat.getCodeType());
    }//GEN-LAST:event_decimalCursorPositionModeRadioButtonMenuItemActionPerformed

    private void hexadecimalCursorPositionModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexadecimalCursorPositionModeRadioButtonMenuItemActionPerformed
        cursorPositionFormat.setCodeType(PositionCodeType.HEXADECIMAL);
        updateCaretPosition();
        statusParameters.setCursorPositionCodeType(cursorPositionFormat.getCodeType());
    }//GEN-LAST:event_hexadecimalCursorPositionModeRadioButtonMenuItemActionPerformed

    private void octalDocumentSizeModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_octalDocumentSizeModeRadioButtonMenuItemActionPerformed
        documentSizeFormat.setCodeType(PositionCodeType.OCTAL);
        updateDocumentSize();
        statusParameters.setDocumentSizeCodeType(documentSizeFormat.getCodeType());
    }//GEN-LAST:event_octalDocumentSizeModeRadioButtonMenuItemActionPerformed

    private void decimalDocumentSizeModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decimalDocumentSizeModeRadioButtonMenuItemActionPerformed
        documentSizeFormat.setCodeType(PositionCodeType.DECIMAL);
        updateDocumentSize();
        statusParameters.setDocumentSizeCodeType(documentSizeFormat.getCodeType());
    }//GEN-LAST:event_decimalDocumentSizeModeRadioButtonMenuItemActionPerformed

    private void hexadecimalDocumentSizeModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexadecimalDocumentSizeModeRadioButtonMenuItemActionPerformed
        documentSizeFormat.setCodeType(PositionCodeType.HEXADECIMAL);
        updateDocumentSize();
        statusParameters.setDocumentSizeCodeType(documentSizeFormat.getCodeType());
    }//GEN-LAST:event_hexadecimalDocumentSizeModeRadioButtonMenuItemActionPerformed

    private void handleEncodingPopup(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            statusControlHandler.encodingsPopupEncodingsMenu(evt);
        }
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WindowUtils.invokeDialog(new BinaryStatusPanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu cursorPositionCodeTypeMenu;
    private javax.swing.JLabel cursorPositionLabel;
    private javax.swing.ButtonGroup cursorPositionModeButtonGroup;
    private javax.swing.JCheckBoxMenuItem cursorPositionShowOffsetCheckBoxMenuItem;
    private javax.swing.JRadioButtonMenuItem decimalCursorPositionModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem decimalDocumentSizeModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem deltaMemoryModeRadioButtonMenuItem;
    private javax.swing.JMenu documentSizeCodeTypeMenu;
    private javax.swing.JMenuItem documentSizeCopyMenuItem;
    private javax.swing.JLabel documentSizeLabel;
    private javax.swing.ButtonGroup documentSizeModeButtonGroup;
    private javax.swing.JPopupMenu documentSizePopupMenu;
    private javax.swing.JCheckBoxMenuItem documentSizeShowRelativeCheckBoxMenuItem;
    private javax.swing.JLabel editationModeLabel;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JRadioButtonMenuItem hexadecimalCursorPositionModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem hexadecimalDocumentSizeModeRadioButtonMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.ButtonGroup memoryModeButtonGroup;
    private javax.swing.JLabel memoryModeLabel;
    private javax.swing.JPopupMenu memoryModePopupMenu;
    private javax.swing.JRadioButtonMenuItem octalCursorPositionModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem octalDocumentSizeModeRadioButtonMenuItem;
    private javax.swing.JMenuItem positionCopyMenuItem;
    private javax.swing.JMenuItem positionGoToMenuItem;
    private javax.swing.JPopupMenu positionPopupMenu;
    private javax.swing.JRadioButtonMenuItem ramMemoryModeRadioButtonMenuItem;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setCursorPosition(CodeAreaCaretPosition caretPosition) {
        this.caretPosition = caretPosition;
        updateCaretPosition();
        updateCursorPositionToolTip();
    }

    @Override
    public void setCurrentDocumentSize(long documentSize, long initialDocumentSize) {
        this.documentSize = documentSize;
        this.initialDocumentSize = initialDocumentSize;
        updateDocumentSize();
        updateDocumentSizeToolTip();
    }

    @Nonnull
    @Override
    public String getEncoding() {
        return encodingLabel.getText();
    }

    @Override
    public void setEncoding(String encodingName) {
        encodingLabel.setText(encodingName + " ^");
    }

    @Override
    public void setEditationMode(EditationMode editationMode, EditationOperation editationOperation) {
        this.editationOperation = editationOperation;
        switch (editationMode) {
            case READ_ONLY: {
                editationModeLabel.setText(READONLY_EDITATION_MODE_LABEL);
                break;
            }
            case EXPANDING:
            case CAPPED: {
                switch (editationOperation) {
                    case INSERT: {
                        editationModeLabel.setText(INSERT_EDITATION_MODE_LABEL);
                        break;
                    }
                    case OVERWRITE: {
                        editationModeLabel.setText(OVERWRITE_EDITATION_MODE_LABEL);
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected editation operation " + editationOperation.name());
                }
                break;
            }
            case INPLACE: {
                editationModeLabel.setText(INPLACE_EDITATION_MODE_LABEL);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected editation mode " + editationMode.name());
        }
    }

    @Override
    public void setControlHandler(StatusControlHandler statusControlHandler) {
        this.statusControlHandler = statusControlHandler;
    }

    @Override
    public void setMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
        memoryModeLabel.setText(memoryMode.getDisplayChar());
        boolean enabled = memoryMode != MemoryMode.READ_ONLY;
        deltaMemoryModeRadioButtonMenuItem.setEnabled(enabled);
        ramMemoryModeRadioButtonMenuItem.setEnabled(enabled);
        if (memoryMode == MemoryMode.RAM_MEMORY) {
            ramMemoryModeRadioButtonMenuItem.setSelected(true);
        } else {
            deltaMemoryModeRadioButtonMenuItem.setSelected(true);
        }
    }

    private void updateCaretPosition() {
        if (caretPosition == null) {
            cursorPositionLabel.setText("-");
        } else {
            StringBuilder labelBuilder = new StringBuilder();
            labelBuilder.append(numberToPosition(caretPosition.getDataPosition(), cursorPositionFormat.getCodeType()));
            if (cursorPositionFormat.isShowOffset()) {
                labelBuilder.append(":");
                labelBuilder.append(caretPosition.getCodeOffset());
            }
            cursorPositionLabel.setText(labelBuilder.toString());
        }
    }

    private void updateCursorPositionToolTip() {
        String tooltipText;
        if (caretPosition == null) {
            tooltipText = resourceBundle.getString("cursorPositionLabel.toolTipText");
        } else {
            long dataPosition = caretPosition.getDataPosition();
            tooltipText = "<html>" + resourceBundle.getString("cursorPositionLabel.toolTipText")
                    + "<br>" + OCTAL_CODE_TYPE_LABEL + ": " + numberToPosition(dataPosition, PositionCodeType.OCTAL)
                    + "<br>" + DECIMAL_CODE_TYPE_LABEL + ": " + numberToPosition(dataPosition, PositionCodeType.DECIMAL)
                    + "<br>" + HEXADECIMAL_CODE_TYPE_LABEL + ": " + numberToPosition(dataPosition, PositionCodeType.HEXADECIMAL)
                    + "</html>";
        }
        cursorPositionLabel.setToolTipText(tooltipText);
    }

    private void updateDocumentSize() {
        if (documentSize == -1) {
            documentSizeLabel.setText(documentSizeFormat.isShowRelative() ? "0 (0)" : "0");
        } else {
            StringBuilder labelBuilder = new StringBuilder();
            labelBuilder.append(numberToPosition(documentSize, documentSizeFormat.getCodeType()));
            if (documentSizeFormat.isShowRelative()) {
                long difference = documentSize - initialDocumentSize;
                labelBuilder.append(difference > 0 ? " (+" : " (");
                labelBuilder.append(numberToPosition(difference, documentSizeFormat.getCodeType()));
                labelBuilder.append(")");

            }

            documentSizeLabel.setText(labelBuilder.toString());
        }
    }

    private void updateDocumentSizeToolTip() {
        String tooltipText = "<html>" + resourceBundle.getString("documentSizeLabel.toolTipText")
                + "<br>" + OCTAL_CODE_TYPE_LABEL + ": " + numberToPosition(documentSize, PositionCodeType.OCTAL)
                + "<br>" + DECIMAL_CODE_TYPE_LABEL + ": " + numberToPosition(documentSize, PositionCodeType.DECIMAL)
                + "<br>" + HEXADECIMAL_CODE_TYPE_LABEL + ": " + numberToPosition(documentSize, PositionCodeType.HEXADECIMAL)
                + "</html>";
        documentSizeLabel.setToolTipText(tooltipText);
    }

    @Nonnull
    private String numberToPosition(long value, PositionCodeType codeType) {
        if (value == 0) {
            return "0";
        }

        int spaceGroupSize = 0;
        switch (codeType) {
            case OCTAL: {
                spaceGroupSize = octalSpaceGroupSize;
                break;
            }
            case DECIMAL: {
                spaceGroupSize = decimalSpaceGroupSize;
                break;
            }
            case HEXADECIMAL: {
                spaceGroupSize = hexadecimalSpaceGroupSize;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type " + codeType.name());
        }

        long remainder = value > 0 ? value : -value;
        StringBuilder builder = new StringBuilder();
        int base = codeType.getBase();
        int groupSize = spaceGroupSize == 0 ? -1 : spaceGroupSize;
        while (remainder > 0) {
            if (groupSize >= 0) {
                if (groupSize == 0) {
                    builder.insert(0, ' ');
                    groupSize = spaceGroupSize - 1;
                } else {
                    groupSize--;
                }
            }

            int digit = (int) (remainder % base);
            remainder = remainder / base;
            builder.insert(0, CodeAreaUtils.UPPER_HEX_CODES[digit]);
        }

        if (value < 0) {
            builder.insert(0, "-");
        }
        return builder.toString();
    }
}

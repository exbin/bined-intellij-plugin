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
package org.exbin.bined.operation.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.operation.swing.command.CodeAreaCommandType;
import org.exbin.bined.operation.swing.command.EditCharDataCommand;
import org.exbin.bined.operation.swing.command.EditCodeDataCommand;
import org.exbin.bined.operation.swing.command.EditDataCommand;
import org.exbin.bined.operation.swing.command.HexCompoundCommand;
import org.exbin.bined.operation.swing.command.RemoveDataCommand;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.basic.DefaultCodeAreaCaret;
import org.exbin.bined.swing.basic.DefaultCodeAreaCommandHandler;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Command handler for undo/redo aware hexadecimal editor editing.
 *
 * @version 0.2.0 2018/08/11
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaOperationCommandHandler implements CodeAreaCommandHandler {

    public static final String BINED_CLIPBOARD_MIME = "application/x-bined";
    public static final String MIME_CLIPBOARD_BINARY = "application/octet-stream";
    public static final String MIME_CHARSET = "charset";
    private static final int CODE_BUFFER_LENGTH = 16;
    private static final char BACKSPACE_CHAR = '\b';
    private static final char DELETE_CHAR = (char) 0x7f;

    private final int metaMask;

    private final CodeAreaCore codeArea;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private Clipboard clipboard;
    private boolean canPaste = false;
    private DataFlavor binedDataFlavor;
    private CodeAreaUtils.ClipboardData currentClipboardData = null;
    private DataFlavor binaryDataFlavor;

    private final BinaryDataUndoHandler undoHandler;
    private EditDataCommand editCommand = null;

    public CodeAreaOperationCommandHandler(CodeAreaCore codeArea, BinaryDataUndoHandler undoHandler) {
        this.codeArea = codeArea;
        this.undoHandler = undoHandler;

        codeTypeSupported = codeArea instanceof CodeTypeCapable;
        viewModeSupported = codeArea instanceof ViewModeCapable;

        int metaMaskInit;
        try {
            metaMaskInit = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMaskInit = java.awt.Event.CTRL_MASK;
        }
        this.metaMask = metaMaskInit;

        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.addFlavorListener((FlavorEvent e) -> {
                updateCanPaste();
            });
            try {
                binaryDataFlavor = new DataFlavor(CodeAreaUtils.MIME_CLIPBOARD_BINARY);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                binedDataFlavor = new DataFlavor(BINED_CLIPBOARD_MIME);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            updateCanPaste();
            canPaste = clipboard.isDataFlavorAvailable(binedDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (IllegalStateException ex) {
            canPaste = false;
        } catch (java.awt.HeadlessException ex) {
            Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Nonnull
    public static CodeAreaCommandHandler.CodeAreaCommandHandlerFactory createDefaultCodeAreaCommandHandlerFactory() {
        return new CodeAreaCommandHandlerFactory() {
            @Nonnull
            @Override
            public CodeAreaCommandHandler createCommandHandler(@Nonnull CodeAreaCore codeArea) {
                return new CodeAreaOperationCommandHandler(codeArea, new CodeAreaUndoHandler(codeArea));
            }
        };
    }

    private void updateCanPaste() {
        canPaste = CodeAreaUtils.canPaste(clipboard, binaryDataFlavor);
    }

    @Override
    public void undoSequenceBreak() {
        editCommand = null;
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                move(keyEvent.getModifiersEx(), MovementDirection.LEFT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                move(keyEvent.getModifiersEx(), MovementDirection.RIGHT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_UP: {
                move(keyEvent.getModifiersEx(), MovementDirection.UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DOWN: {
                move(keyEvent.getModifiersEx(), MovementDirection.DOWN);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    move(keyEvent.getModifiersEx(), MovementDirection.DOC_START);
                } else {
                    move(keyEvent.getModifiersEx(), MovementDirection.ROW_START);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
                if ((keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    move(keyEvent.getModifiersEx(), MovementDirection.DOC_END);
                } else {
                    move(keyEvent.getModifiersEx(), MovementDirection.ROW_END);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(keyEvent.getModifiersEx(), MovementDirection.PAGE_UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(keyEvent.getModifiersEx(), MovementDirection.PAGE_DOWN);
                undoSequenceBreak();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_INSERT: {
                EditationMode editationMode = ((EditationModeCapable) codeArea).getEditationMode();
                switch (editationMode) {
                    case INSERT: {
                        ((EditationModeCapable) codeArea).setEditationMode(EditationMode.OVERWRITE);
                        keyEvent.consume();
                        break;
                    }
                    case OVERWRITE: {
                        ((EditationModeCapable) codeArea).setEditationMode(EditationMode.INSERT);
                        keyEvent.consume();
                        break;
                    }
                }
                break;
            }
            case KeyEvent.VK_TAB: {
                if (viewModeSupported && ((ViewModeCapable) codeArea).getViewMode() == CodeAreaViewMode.DUAL) {
                    move(keyEvent.getModifiersEx(), MovementDirection.SWITCH_SECTION);
                    undoSequenceBreak();
                    revealCursor();
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_DELETE: {
                deletePressed();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                backSpacePressed();
                keyEvent.consume();
                break;
            }
            default: {
                if (((ClipboardCapable) codeArea).isHandleClipboard()) {
                    if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_C) {
                        copy();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_X) {
                        cut();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_V) {
                        paste();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.VK_A) {
                        codeArea.selectAll();
                        keyEvent.consume();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        char keyValue = keyEvent.getKeyChar();
        if (keyValue == KeyEvent.CHAR_UNDEFINED) {
            return;
        }
        if (!((EditationModeCapable) codeArea).isEditable()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
            long dataPosition = caretPosition.getDataPosition();
            int startCodeOffset = caretPosition.getCodeOffset();
            CodeType codeType = getCodeType();
            boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyValue, startCodeOffset, codeType);
            if (validKey) {
                DeleteSelectionCommand deleteSelectionCommand = null;
                if (codeArea.hasSelection()) {
                    long selectionStart = ((SelectionCapable) codeArea).getSelection().getFirst();
                    deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
                    ((CaretCapable) codeArea).getCaret().setCaretPosition(selectionStart);
                }

                int value;
                if (keyValue >= '0' && keyValue <= '9') {
                    value = keyValue - '0';
                } else {
                    value = Character.toLowerCase(keyValue) - 'a' + 10;
                }

//                if (codeArea.getEditationAllowed() == EditationAllowed.OVERWRITE_ONLY && codeArea.getEditationMode() == EditationMode.OVERWRITE && dataPosition == dataSize) {
//                    return;
//                }
                if (editCommand != null && editCommand.wasReverted()) {
                    editCommand = null;
                }

                int codeOffset = ((CaretCapable) codeArea).getCaret().getCaretPosition().getCodeOffset();
                if (((EditationModeCapable) codeArea).getEditationMode() == EditationMode.OVERWRITE) {
                    if (editCommand == null || !(editCommand instanceof EditCodeDataCommand) || editCommand.getCommandType() != EditDataCommand.EditCommandType.OVERWRITE) {
                        editCommand = new EditCodeDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition, codeOffset);
                        if (deleteSelectionCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteSelectionCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (BinaryDataOperationException ex) {
                                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCodeDataCommand) editCommand).appendEdit((byte) value);
                } else {
                    if (editCommand == null || !(editCommand instanceof EditCodeDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.INSERT) {
                        editCommand = new EditCodeDataCommand(codeArea, EditCharDataCommand.EditCommandType.INSERT, dataPosition, codeOffset);
                        if (deleteSelectionCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteSelectionCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (BinaryDataOperationException ex) {
                                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCodeDataCommand) editCommand).appendEdit((byte) value);
                }
                codeArea.notifyDataChanged();
                move(DefaultCodeAreaCommandHandler.NO_MODIFIER, MovementDirection.RIGHT);
                revealCursor();
            }
        } else {
            char keyChar = keyValue;
            boolean validKey = keyChar > 31 && keyChar != DELETE_CHAR && isValidChar(keyValue);
            if (validKey) {
                if (editCommand != null && editCommand.wasReverted()) {
                    editCommand = null;
                }
                long dataPosition = ((CaretCapable) codeArea).getCaret().getCaretPosition().getDataPosition();
                DeleteSelectionCommand deleteCommand = null;
                if (codeArea.hasSelection()) {
                    deleteCommand = new DeleteSelectionCommand(codeArea);
                }

                if (((EditationModeCapable) codeArea).getEditationMode() == EditationMode.OVERWRITE) {
                    if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.OVERWRITE) {
                        editCommand = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition);
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (BinaryDataOperationException ex) {
                                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCharDataCommand) editCommand).appendEdit(keyChar);
                } else {
                    if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.INSERT) {
                        editCommand = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.INSERT, dataPosition);
                        if (deleteCommand != null) {
                            HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                            compoundCommand.appendCommand(deleteCommand);
                            try {
                                undoHandler.execute(compoundCommand);
                            } catch (BinaryDataOperationException ex) {
                                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            compoundCommand.appendCommand(editCommand);
                        } else {
                            undoHandler.addCommand(editCommand);
                        }
                    }

                    ((EditCharDataCommand) editCommand).appendEdit(keyChar);
                }

                codeArea.notifyDataChanged();
                revealCursor();
                codeArea.repaint();
            }
        }
    }

    @Override
    public void backSpacePressed() {
        if (!((EditationModeCapable) codeArea).isEditable()) {
            return;
        }

//        deleteAction(BACKSPACE_CHAR);
    }

    @Override
    public void deletePressed() {
        if (!((EditationModeCapable) codeArea).isEditable()) {
            return;
        }

//        deleteAction(DELETE_CHAR);
    }

//    private void deleteAction(char keyChar) {
//        if (codeArea.hasSelection()) {
//            DeleteSelectionCommand deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
//            try {
//                undoHandler.execute(deleteSelectionCommand);
//                sequenceBreak();
//                codeArea.notifyDataChanged();
//            } catch (BinaryDataOperationException ex) {
//                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } else {
//            if (editCommand != null && editCommand.wasReverted()) {
//                editCommand = null;
//            }
//
//            long dataPosition = codeArea.getDataPosition();
//            if (codeArea.getActiveSection() == Section.CODE_MATRIX) {
//                if (editCommand == null || !(editCommand instanceof EditCodeDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.DELETE) {
//                    editCommand = new EditCodeDataCommand(codeArea, EditCodeDataCommand.EditCommandType.DELETE, dataPosition, 0);
//                    undoHandler.addCommand(editCommand);
//                }
//
//                ((EditCodeDataCommand) editCommand).appendEdit((byte) keyChar);
//            } else {
//                if (editCommand == null || !(editCommand instanceof EditCharDataCommand) || editCommand.getCommandType() != EditCodeDataCommand.EditCommandType.DELETE) {
//                    editCommand = new EditCharDataCommand(codeArea, EditCharDataCommand.EditCommandType.DELETE, dataPosition);
//                    undoHandler.addCommand(editCommand);
//                }
//
//                ((EditCharDataCommand) editCommand).appendEdit(keyChar);
//            }
//            codeArea.notifyDataChanged();
//        }
//    }
    @Override
    public void delete() {
        if (!((EditationModeCapable) codeArea).isEditable()) {
            return;
        }

        try {
            undoHandler.execute(new DeleteSelectionCommand(codeArea));
            undoSequenceBreak();
            codeArea.notifyDataChanged();
        } catch (BinaryDataOperationException ex) {
            Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void copy() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = codeArea.getContentData().copy(first, last - first + 1);

            CodeAreaUtils.BinaryDataClipboardData binaryData = new CodeAreaUtils.BinaryDataClipboardData(copy, binaryDataFlavor);
            setClipboardContent(binaryData);
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = codeArea.getContentData().copy(first, last - first + 1);

            CodeType codeType = ((CodeTypeCapable) codeArea).getCodeType();
            CodeCharactersCase charactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
            CodeAreaUtils.CodeDataClipboardData binaryData = new CodeAreaUtils.CodeDataClipboardData(copy, binaryDataFlavor, codeType, charactersCase);
            setClipboardContent(binaryData);
        }
    }

    private void setClipboardContent(@Nonnull CodeAreaUtils.ClipboardData content) {
        clearClipboardData();
        try {
            currentClipboardData = content;
            clipboard.setContents(content, content);
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore and clear
            clearClipboardData();
        }
    }

    private void clearClipboardData() {
        if (currentClipboardData != null) {
            currentClipboardData.dispose();
            currentClipboardData = null;
        }
    }

    @Override
    public void cut() {
        if (!((EditationModeCapable) codeArea).isEditable()) {
            return;
        }

//        SelectionRange selection = codeArea.getSelection();
//        if (selection != null) {
//            copy();
//            try {
//                undoHandler.execute(new DeleteSelectionCommand(codeArea));
//                sequenceBreak();
//                codeArea.notifyDataChanged();
//            } catch (BinaryDataOperationException ex) {
//                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    @Override
    public void paste() {
        if (!((EditationModeCapable) codeArea).isEditable()) {
            return;
        }

//        try {
//            if (!clipboard.isDataFlavorAvailable(binedDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.getTextPlainUnicodeFlavor())) {
//                return;
//            }
//        } catch (IllegalStateException ex) {
//            return;
//        }
//
//        DeleteSelectionCommand deleteSelectionCommand = null;
//        if (codeArea.hasSelection()) {
//            try {
//                deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
//                deleteSelectionCommand.execute();
//                sequenceBreak();
//            } catch (BinaryDataOperationException ex) {
//                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        long dataSize = codeArea.getDataSize();
//        try {
//            if (clipboard.isDataFlavorAvailable(binedDataFlavor)) {
//                try {
//                    Object clipboardObject = clipboard.getContentData(binedDataFlavor);
//                    if (clipboardObject instanceof BinaryData) {
//                        BinaryData clipboardData = (BinaryData) clipboardObject;
//                        CodeAreaCaret caret = codeArea.getCaret();
//                        long dataPosition = caret.getDataPosition();
//
//                        CodeAreaCommand modifyCommand = null;
//                        BinaryData insertedData = null;
//                        long clipDataSize = clipboardData.getDataSize();
//                        long insertionPosition = dataPosition;
//                        if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
//                            BinaryData modifiedData;
//                            long replacedPartSize = clipDataSize;
//                            if (insertionPosition + replacedPartSize > dataSize) {
//                                replacedPartSize = dataSize - insertionPosition;
//                                modifiedData = clipboardData.copy(0, replacedPartSize);
//                            } else {
//                                modifiedData = clipboardData.copy();
//                            }
//                            if (replacedPartSize > 0) {
//                                modifyCommand = new ModifyDataCommand(codeArea, dataPosition, modifiedData);
//                                if (clipDataSize > replacedPartSize) {
//                                    insertedData = clipboardData.copy(replacedPartSize, clipDataSize - replacedPartSize);
//                                    insertionPosition += replacedPartSize;
//                                } else {
//                                    insertedData = new ByteArrayData();
//                                }
//                            }
//                        }
//                        if (insertedData == null) {
//                            insertedData = clipboardData.copy();
//                        }
//
//                        CodeAreaCommand insertCommand = null;
//                        if (!insertedData.isEmpty()) {
//                            insertCommand = new InsertDataCommand(codeArea, insertionPosition, (EditableBinaryData) insertedData);
//                        }
//
//                        CodeAreaCommand pasteCommand = HexCompoundCommand.buildCompoundCommand(codeArea, deleteSelectionCommand, modifyCommand, insertCommand);
//                        if (pasteCommand != null) {
//                            try {
//                                if (modifyCommand != null) {
//                                    modifyCommand.execute();
//                                }
//                                if (insertCommand != null) {
//                                    insertCommand.execute();
//                                }
//                                undoHandler.addCommand(pasteCommand);
//                            } catch (BinaryDataOperationException ex) {
//                                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//
//                            sequenceBreak();
//                            codeArea.notifyDataChanged();
//                            codeArea.updateScrollBars();
//                            codeArea.revealCursor();
//                            codeArea.repaint();
//                        }
//                    }
//                } catch (UnsupportedFlavorException | IllegalStateException | IOException ex) {
//                    Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } else if (clipboard.isDataFlavorAvailable(DataFlavor.getTextPlainUnicodeFlavor())) {
//                InputStream clipboardData;
//                try {
//                    clipboardData = (InputStream) clipboard.getContentData(DataFlavor.getTextPlainUnicodeFlavor());
//                    CodeAreaCaret caret = codeArea.getCaret();
//                    long dataPosition = caret.getDataPosition();
//
//                    CodeAreaCommand modifyCommand = null;
//                    DataFlavor textPlainUnicodeFlavor = DataFlavor.getTextPlainUnicodeFlavor();
//                    String charsetName = textPlainUnicodeFlavor.getParameter(MIME_CHARSET);
//                    CharsetStreamTranslator translator = new CharsetStreamTranslator(Charset.forName(charsetName), codeArea.getCharset(), clipboardData);
//
//                    // TODO use stream directly without buffer
//                    PagedData insertedData = new PagedData();
//                    insertedData.insert(0, translator, -1);
//                    long clipDataSize = insertedData.getDataSize();
//                    long insertionPosition = dataPosition;
//                    if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
//                        BinaryData modifiedData;
//                        long replacedPartSize = clipDataSize;
//                        if (insertionPosition + replacedPartSize > dataSize) {
//                            replacedPartSize = dataSize - insertionPosition;
//                            modifiedData = insertedData.copy(0, replacedPartSize);
//                        } else {
//                            modifiedData = insertedData.copy();
//                        }
//                        if (replacedPartSize > 0) {
//                            modifyCommand = new ModifyDataCommand(codeArea, dataPosition, modifiedData);
//                            if (clipDataSize > replacedPartSize) {
//                                insertedData = insertedData.copy(replacedPartSize, clipDataSize - replacedPartSize);
//                                insertionPosition += replacedPartSize;
//                            } else {
//                                insertedData.clear();
//                            }
//                        }
//                    }
//
//                    CodeAreaCommand insertCommand = null;
//                    if (!insertedData.isEmpty()) {
//                        insertCommand = new InsertDataCommand(codeArea, insertionPosition, insertedData);
//                    }
//
//                    CodeAreaCommand pasteCommand = HexCompoundCommand.buildCompoundCommand(codeArea, deleteSelectionCommand, modifyCommand, insertCommand);
//                    try {
//                        if (modifyCommand != null) {
//                            modifyCommand.execute();
//                        }
//                        if (insertCommand != null) {
//                            insertCommand.execute();
//                        }
//                        undoHandler.addCommand(pasteCommand);
//                    } catch (BinaryDataOperationException ex) {
//                        Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                    sequenceBreak();
//                    codeArea.notifyDataChanged();
//                    codeArea.updateScrollBars();
//                    codeArea.revealCursor();
//                    codeArea.repaint();
//                } catch (UnsupportedFlavorException | IllegalStateException | IOException ex) {
//                    Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        } catch (IllegalStateException ex) {
//            // Clipboard not available - ignore
//        }
    }

    @Override
    public void pasteFromCode() {
        if (!((EditationModeCapable) codeArea).isEditable()) {
            return;
        }

        try {
            if (!clipboard.isDataFlavorAvailable(binedDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.getTextPlainUnicodeFlavor())) {
                return;
            }
        } catch (IllegalStateException ex) {
            return;
        }

//        try {
//            if (clipboard.isDataFlavorAvailable(binedDataFlavor)) {
//                paste();
//            } else if (clipboard.isDataFlavorAvailable(DataFlavor.getTextPlainUnicodeFlavor())) {
//                DeleteSelectionCommand deleteSelectionCommand = null;
//                if (codeArea.hasSelection()) {
//                    try {
//                        deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
//                        deleteSelectionCommand.execute();
//                    } catch (BinaryDataOperationException ex) {
//                        Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                long dataSize = codeArea.getDataSize();
//                InputStream insertedData;
//                try {
//                    insertedData = (InputStream) clipboard.getContentData(DataFlavor.getTextPlainUnicodeFlavor());
//                    CodeAreaCaret caret = codeArea.getCaret();
//                    long dataPosition = caret.getDataPosition();
//
//                    CodeAreaCommand modifyCommand = null;
//                    CodeType codeType = codeArea.getCodeType();
//                    int maxDigits = codeType.getMaxDigits();
//
//                    DataFlavor textPlainUnicodeFlavor = DataFlavor.getTextPlainUnicodeFlavor();
//                    String charsetName = textPlainUnicodeFlavor.getParameter(MIME_CHARSET);
//                    CharsetStreamTranslator translator = new CharsetStreamTranslator(Charset.forName(charsetName), codeArea.getCharset(), insertedData);
//                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                    byte[] dataBuffer = new byte[1024];
//                    int length;
//                    while ((length = translator.read(dataBuffer)) != -1) {
//                        outputStream.write(dataBuffer, 0, length);
//                    }
//                    String insertedString = outputStream.toString(codeArea.getCharset().name());
//                    ByteArrayEditableData clipData = new ByteArrayEditableData();
//                    byte[] buffer = new byte[CODE_BUFFER_LENGTH];
//                    int bufferUsage = 0;
//                    int offset = 0;
//                    for (int i = 0; i < insertedString.length(); i++) {
//                        char charAt = insertedString.charAt(i);
//                        if ((charAt == ' ' || charAt == '\t') && offset == i) {
//                            offset++;
//                        } else if (charAt == ' ' || charAt == '\t' || charAt == ',' || charAt == ';' || charAt == ':') {
//                            byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset, i), codeType);
//                            if (bufferUsage < CODE_BUFFER_LENGTH) {
//                                buffer[bufferUsage] = value;
//                                bufferUsage++;
//                            } else {
//                                clipData.insert(clipData.getDataSize(), buffer, 0, bufferUsage);
//                                bufferUsage = 0;
//                            }
//                            offset = i + 1;
//                        } else if (i == offset + maxDigits) {
//                            byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset, i), codeType);
//                            if (bufferUsage < CODE_BUFFER_LENGTH) {
//                                buffer[bufferUsage] = value;
//                                bufferUsage++;
//                            } else {
//                                clipData.insert(clipData.getDataSize(), buffer, 0, bufferUsage);
//                                bufferUsage = 0;
//                            }
//                            offset = i;
//                        }
//                    }
//
//                    long clipDataSize = clipData.getDataSize();
//                    if (offset < insertedString.length()) {
//                        byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset), codeType);
//                        if (bufferUsage < CODE_BUFFER_LENGTH) {
//                            buffer[bufferUsage] = value;
//                            bufferUsage++;
//                        } else {
//                            clipData.insert(clipDataSize, buffer, 0, bufferUsage);
//                            bufferUsage = 0;
//                        }
//                    }
//
//                    if (bufferUsage > 0) {
//                        clipData.insert(clipDataSize, buffer, 0, bufferUsage);
//                    }
//
//                    PagedData pastedData = new PagedData();
//                    pastedData.insert(0, clipData);
//                    long pastedDataSize = pastedData.getDataSize();
//                    long insertionPosition = dataPosition;
//                    if (codeArea.getEditationMode() == EditationMode.OVERWRITE) {
//                        BinaryData modifiedData = pastedData;
//                        long replacedPartSize = clipDataSize;
//                        if (insertionPosition + replacedPartSize > dataSize) {
//                            replacedPartSize = dataSize - insertionPosition;
//                            modifiedData = pastedData.copy(0, replacedPartSize);
//                        }
//                        if (replacedPartSize > 0) {
//                            modifyCommand = new ModifyDataCommand(codeArea, dataPosition, modifiedData);
//                            if (pastedDataSize > replacedPartSize) {
//                                pastedData = pastedData.copy(replacedPartSize, pastedDataSize - replacedPartSize);
//                                insertionPosition += replacedPartSize;
//                            } else {
//                                pastedData.clear();
//                            }
//                        }
//                    }
//
//                    CodeAreaCommand insertCommand = null;
//                    if (pastedData.getDataSize() > 0) {
//                        insertCommand = new InsertDataCommand(codeArea, insertionPosition, pastedData);
//                    }
//
//                    CodeAreaCommand pasteCommand = HexCompoundCommand.buildCompoundCommand(codeArea, deleteSelectionCommand, modifyCommand, insertCommand);
//                    try {
//                        if (modifyCommand != null) {
//                            modifyCommand.execute();
//                        }
//                        if (insertCommand != null) {
//                            insertCommand.execute();
//                        }
//                        undoHandler.addCommand(pasteCommand);
//                    } catch (BinaryDataOperationException ex) {
//                        Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                    sequenceBreak();
//                    codeArea.notifyDataChanged();
//                    codeArea.updateScrollBars();
//                    codeArea.revealCursor();
//                    codeArea.repaint();
//                } catch (UnsupportedFlavorException | IOException ex) {
//                    Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        } catch (IllegalStateException ex) {
//            // Clipboard not available - ignore
//        }
    }

    @Override
    public boolean canPaste() {
        return canPaste;
    }

    @Override
    public void selectAll() {
        long dataSize = codeArea.getDataSize();
        if (dataSize > 0) {
            ((SelectionCapable) codeArea).setSelection(0, dataSize - 1);
        }
    }

    @Override
    public void clearSelection() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        ((SelectionCapable) codeArea).setSelection(selection.getStart(), selection.getStart());
    }

    public void updateSelection(@Nonnull SelectingMode selecting, @Nonnull CaretPosition caretPosition) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selecting == SelectingMode.SELECTING) {
            ((SelectionCapable) codeArea).setSelection(selection.getStart(), caret.getDataPosition());
        } else {
            ((SelectionCapable) codeArea).setSelection(caret.getDataPosition(), caret.getDataPosition());
        }
    }

    @Override
    public void moveCaret(int positionX, int positionY, @Nonnull SelectingMode selecting) {
        CaretPosition caretPosition = ((CaretCapable) codeArea).mousePositionToClosestCaretPosition(positionX, positionY, PositionOverflowMode.OVERFLOW);
        if (caretPosition != null) {
            ((CaretCapable) codeArea).getCaret().setCaretPosition(caretPosition);
            updateSelection(selecting, caretPosition);

            notifyCaretMoved();
            undoSequenceBreak();
            codeArea.repaint();
        }
    }

    public void move(int modifiers, @Nonnull MovementDirection direction) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        CaretPosition movePosition = ((CaretCapable) codeArea).computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            caret.setCaretPosition(movePosition);
            updateSelection((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE, movePosition);
            notifyCaretMoved();
        }
    }

    public void scroll(@Nonnull ScrollingDirection direction) {
        CodeAreaScrollPosition sourcePosition = ((ScrollingCapable) codeArea).getScrollPosition();
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) codeArea).computeScrolling(sourcePosition, direction);
        if (!sourcePosition.equals(scrollPosition)) {
            ((ScrollingCapable) codeArea).setScrollPosition(scrollPosition);
            codeArea.resetPainter();
            notifyScrolled();
            updateScrollBars();
        }
    }

    @Override
    public void wheelScroll(int scrollSize, ScrollbarOrientation orientation) {
        if (scrollSize < 0) {
            for (int i = 0; i < -scrollSize; i++) {
                scroll(ScrollingDirection.UP);
            }
        } else if (scrollSize > 0) {
            for (int i = 0; i < scrollSize; i++) {
                scroll(ScrollingDirection.DOWN);
            }
        }
    }

    private static class DeleteSelectionCommand extends CodeAreaCommand {

        private final RemoveDataCommand removeCommand;
        private final long position;
        private final long size;

        public DeleteSelectionCommand(CodeAreaCore coreArea) {
            super(coreArea);
            SelectionRange selection = ((SelectionCapable) coreArea).getSelection();
            position = selection.getFirst();
            size = selection.getLast() - position + 1;
            removeCommand = new RemoveDataCommand(coreArea, position, 0, size);
        }

        @Override
        public void execute() throws BinaryDataOperationException {
            super.execute();
        }

        @Override
        public void redo() throws BinaryDataOperationException {
            removeCommand.redo();
            codeArea.clearSelection();
            CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
            caret.setCaretPosition(position);
            ((ScrollingCapable) codeArea).revealCursor();
            codeArea.notifyDataChanged();
            ((ScrollingCapable) codeArea).updateScrollBars();
        }

        @Override
        public void undo() throws BinaryDataOperationException {
            removeCommand.undo();
            codeArea.clearSelection();
            CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
            caret.setCaretPosition(size);
            ((ScrollingCapable) codeArea).revealCursor();
            codeArea.notifyDataChanged();
            ((ScrollingCapable) codeArea).updateScrollBars();
        }

        @Override
        public CodeAreaCommandType getType() {
            return CodeAreaCommandType.DATA_REMOVED;
        }

        @Override
        public boolean canUndo() {
            return true;
        }
    }

    public boolean isValidChar(char value) {
        return ((CharsetCapable) codeArea).getCharset().canEncode();
    }

    @Nonnull
    private CodeType getCodeType() {
        if (codeTypeSupported) {
            return ((CodeTypeCapable) codeArea).getCodeType();
        }

        return CodeType.HEXADECIMAL;
    }

    private void revealCursor() {
        ((ScrollingCapable) codeArea).revealCursor();
        codeArea.repaint();
    }

    private void notifyCaretMoved() {
        ((CaretCapable) codeArea).notifyCaretMoved();
    }

    private void notifyScrolled() {
        ((ScrollingCapable) codeArea).notifyScrolled();
    }

    private void updateScrollBars() {
        ((ScrollingCapable) codeArea).updateScrollBars();
    }
}

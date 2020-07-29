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
package org.exbin.bined.intellij;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

/**
 * Editor listener for binary editor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.0 2016/12/08
 */
public class EditorListener implements FileEditorManagerListener {

    private JPanel editor;

    public EditorListener(JPanel editor) {
        this.editor = editor;
    }

    public void fileOpened(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
    }

    public void fileClosed(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
    }

    public void selectionChanged(FileEditorManagerEvent event) {
        if (event.getNewFile() == null) {
            return;
        }
//        if (SupportedExtensions.isSupported(event.getNewFile().getExtension())) {
//            this.editor.getCanvas().setFile(event.getNewFile());
//            this.editor.getCanvas().showSelectedFile();
//        }
    }
}
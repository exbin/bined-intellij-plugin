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
package org.exbin.bined.intellij;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * File editor provider for binary files.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdFileEditorProvider implements FileEditorProvider, DumbAware {

    public static final String BINED_EDITOR_TYPE_ID = "org.exbin.bined";

    @Override
    public boolean accept(Project project, VirtualFile file) {
        return file instanceof BinEdVirtualFile || file.getPath().startsWith(BinEdVirtualFile.PATH_PREFIX);
    }

    @Nonnull
    @Override
    public FileEditor createEditor(Project project, VirtualFile virtualFile) {
        BinEdVirtualFile binEdVirtualFile;
        if (virtualFile instanceof BinEdVirtualFile) {
            binEdVirtualFile = (BinEdVirtualFile) virtualFile;
        } else {
            binEdVirtualFile = new BinEdVirtualFile(virtualFile);
        }

        BinEdFileEditor fileEditor = new BinEdFileEditor(project, binEdVirtualFile);
        fileEditor.setDisplayName(binEdVirtualFile.getDisplayName());

        return fileEditor;
    }

    @Nonnull
    @Override
    public String getEditorTypeId() {
        return BINED_EDITOR_TYPE_ID;
    }

    @Nonnull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @Nonnull
    @Override
    public FileEditorState readState(Element sourceElement, Project project, VirtualFile file) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void writeState(FileEditorState state, Project project, Element targetElement) {
    }
}

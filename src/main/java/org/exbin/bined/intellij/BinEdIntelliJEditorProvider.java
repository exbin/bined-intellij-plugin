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

import org.exbin.framework.editor.api.MultiEditorProvider;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.file.api.FileType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Editor provider wrapper for IntelliJ BinEd editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdIntelliJEditorProvider implements MultiEditorProvider {

    private FileHandler activeFileOverride = null;

    public BinEdIntelliJEditorProvider() {
    }

    public void setActiveFile(@Nullable FileHandler fileHandler) {
        activeFileOverride = fileHandler;
        // activeFileChanged();
    }

    @NotNull @Override public List<FileHandler> getFileHandlers() {
        return null;
    }

    @NotNull @Override public String getName(FileHandler fileHandler) {
        return null;
    }

    @Override public void saveFile(FileHandler fileHandler) {

    }

    @Override public void saveAsFile(FileHandler fileHandler) {

    }

    @Override public void closeFile() {

    }

    @Override public void closeFile(FileHandler fileHandler) {

    }

    @Override public void closeOtherFiles(FileHandler fileHandler) {

    }

    @Override public void closeAllFiles() {

    }

    @Override public void saveAllFiles() {

    }

    @NotNull @Override public JComponent getEditorComponent() {
        return null;
    }

    @NotNull @Override public Optional<FileHandler> getActiveFile() {
        return Optional.empty();
    }

    @NotNull @Override public String getWindowTitle(String s) {
        return null;
    }

    @Override public void openFile(URI uri, FileType fileType) {

    }

    @Override public void setModificationListener(EditorModificationListener editorModificationListener) {

    }

    @Override public void newFile() {

    }

    @Override public void openFile() {

    }

    @Override public void saveFile() {

    }

    @Override public void saveAsFile() {

    }

    @Override public boolean canSave() {
        return false;
    }

    @Override public boolean releaseFile(FileHandler fileHandler) {
        return false;
    }

    @Override public boolean releaseAllFiles() {
        return false;
    }

    @Override public void loadFromFile(String s) throws URISyntaxException {

    }

    @Override public void loadFromFile(URI uri, @org.jetbrains.annotations.Nullable FileType fileType) {

    }

    @NotNull @Override public Optional<File> getLastUsedDirectory() {
        return Optional.empty();
    }

    @Override public void setLastUsedDirectory(@org.jetbrains.annotations.Nullable File file) {

    }

    @Override public void updateRecentFilesList(URI uri, FileType fileType) {

    }

    //    @Override
//    public void updateActiveFile() {
//        activeFile = activeFileOverride;
//    }
}

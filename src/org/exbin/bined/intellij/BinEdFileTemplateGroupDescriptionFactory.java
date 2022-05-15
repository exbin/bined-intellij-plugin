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

import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.project.DumbAware;

import javax.annotation.Nonnull;

/**
 * File template group descriptor factory.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.6 2021/12/30
 */
public class BinEdFileTemplateGroupDescriptionFactory implements FileTemplateGroupDescriptorFactory, DumbAware {

    @Nonnull
    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        final FileTemplateGroupDescriptor descriptor = new FileTemplateGroupDescriptor("Binary File", AllIcons.FileTypes.Any_type);
        descriptor.addTemplate(new FileTemplateDescriptor("Empty binary file.bin", AllIcons.FileTypes.Any_type));
        return descriptor;
    }
}
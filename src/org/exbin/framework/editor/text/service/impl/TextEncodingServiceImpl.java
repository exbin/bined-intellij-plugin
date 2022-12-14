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
package org.exbin.framework.editor.text.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.preferences.TextEncodingPreferences;
import org.exbin.framework.editor.text.service.*;
import org.exbin.xbup.core.util.StringUtils;

/**
 * Implementation of the text encoding service.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class TextEncodingServiceImpl implements TextEncodingService {

    private List<String> encodings = new ArrayList<>();
    private String selectedEncoding = StringUtils.ENCODING_UTF8;
    private TextEncodingStatusApi textEncodingStatus = null;
    private EncodingChangeListener encodingChangeListener = null;

    @Nonnull
    @Override
    public List<String> getEncodings() {
        return encodings;
    }

    @Override
    public void setEncodings(List<String> encodings) {
        this.encodings = encodings;
        if (encodingChangeListener != null) {
            encodingChangeListener.encodingListChanged();
        }
    }

    @Nonnull
    @Override
    public String getSelectedEncoding() {
        return selectedEncoding;
    }

    @Override
    public void setSelectedEncoding(String encoding) {
        selectedEncoding = encoding;

        if (textEncodingStatus != null) {
            textEncodingStatus.setEncoding(encoding);
        }

        if (encodingChangeListener != null) {
            encodingChangeListener.selectedEncodingChanged();
        }
    }

    @Override
    public void setTextEncodingStatus(TextEncodingStatusApi textEncodingStatus) {
        this.textEncodingStatus = textEncodingStatus;
        textEncodingStatus.setEncoding(selectedEncoding);
    }

    @Override
    public void loadFromPreferences(TextEncodingPreferences preferences) {
        selectedEncoding = preferences.getSelectedEncoding();
        encodings.clear();
        encodings.addAll(preferences.getEncodings());

        if (encodingChangeListener != null) {
            encodingChangeListener.selectedEncodingChanged();
            encodingChangeListener.encodingListChanged();
        }
    }

    @Override
    public void setEncodingChangeListener(EncodingChangeListener listener) {
        this.encodingChangeListener = listener;
    }
}

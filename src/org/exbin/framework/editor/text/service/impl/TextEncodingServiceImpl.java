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
package org.exbin.framework.editor.text.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.preferences.TextEncodingPreferences;
import org.exbin.framework.editor.text.service.*;

/**
 * Implementation of the text encoding service.
 *
 * @version 0.2.1 2019/07/19
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class TextEncodingServiceImpl implements TextEncodingService {

    private List<String> encodings = new ArrayList<>();
    private String selectedEncoding = TextEncodingPreferences.ENCODING_UTF8;
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

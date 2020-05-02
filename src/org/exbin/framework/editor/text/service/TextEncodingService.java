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
package org.exbin.framework.editor.text.service;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.preferences.TextEncodingPreferences;

/**
 * Text encoding panel API.
 *
 * @version 0.2.1 2019/07/19
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface TextEncodingService {

    /**
     * Returns current encodings used in application frame.
     *
     * @return font
     */
    @Nonnull
    List<String> getEncodings();

    /**
     * Gets selected encoding.
     *
     * @return selected encoding
     */
    @Nonnull
    String getSelectedEncoding();

    /**
     * Sets current encodings used in application frame.
     *
     * @param encodings list of encodings
     */
    void setEncodings(List<String> encodings);

    /**
     * Sets selected encoding.
     *
     * @param encoding encoding
     */
    void setSelectedEncoding(String encoding);

    void setTextEncodingStatus(TextEncodingStatusApi textEncodingStatus);

    void loadFromPreferences(TextEncodingPreferences preferences);

    void setEncodingChangeListener(EncodingChangeListener listener);

    public interface EncodingChangeListener {

        void encodingListChanged();

        void selectedEncodingChanged();
    }
}

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
package org.exbin.framework.bined.options;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.bined.panel.StatusCursorPositionFormat;
import org.exbin.framework.bined.panel.StatusDocumentSizeFormat;
import org.exbin.framework.bined.preferences.StatusParameters;

/**
 * Status panel options.
 *
 * @version 0.2.0 2019/03/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatusOptions {

    public static int DEFAULT_OCTAL_SPACE_GROUP_SIZE = 4;
    public static int DEFAULT_DECIMAL_SPACE_GROUP_SIZE = 3;
    public static int DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE = 4;

    private StatusCursorPositionFormat cursorPositionFormat = new StatusCursorPositionFormat();
    private StatusDocumentSizeFormat documentSizeFormat = new StatusDocumentSizeFormat();
    private int octalSpaceGroupSize = DEFAULT_OCTAL_SPACE_GROUP_SIZE;
    private int decimalSpaceGroupSize = DEFAULT_DECIMAL_SPACE_GROUP_SIZE;
    private int hexadecimalSpaceGroupSize = DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE;

    @Nonnull
    public StatusCursorPositionFormat getCursorPositionFormat() {
        return cursorPositionFormat;
    }

    public void setCursorPositionFormat(StatusCursorPositionFormat cursorPositionFormat) {
        this.cursorPositionFormat = cursorPositionFormat;
    }

    @Nonnull
    public StatusDocumentSizeFormat getDocumentSizeFormat() {
        return documentSizeFormat;
    }

    public void setDocumentSizeFormat(StatusDocumentSizeFormat documentSizeFormat) {
        this.documentSizeFormat = documentSizeFormat;
    }

    public int getOctalSpaceGroupSize() {
        return octalSpaceGroupSize;
    }

    public void setOctalSpaceGroupSize(int octalSpaceGroupSize) {
        this.octalSpaceGroupSize = octalSpaceGroupSize;
    }

    public int getDecimalSpaceGroupSize() {
        return decimalSpaceGroupSize;
    }

    public void setDecimalSpaceGroupSize(int decimalSpaceGroupSize) {
        this.decimalSpaceGroupSize = decimalSpaceGroupSize;
    }

    public int getHexadecimalSpaceGroupSize() {
        return hexadecimalSpaceGroupSize;
    }

    public void setHexadecimalSpaceGroupSize(int hexadecimalSpaceGroupSize) {
        this.hexadecimalSpaceGroupSize = hexadecimalSpaceGroupSize;
    }

    public void loadFromParameters(StatusParameters parameters) {
        cursorPositionFormat.setCodeType(parameters.getCursorPositionCodeType());
        cursorPositionFormat.setShowOffset(parameters.isCursorShowOffset());
        documentSizeFormat.setCodeType(parameters.getDocumentSizeCodeType());
        documentSizeFormat.setShowRelative(parameters.isDocumentSizeShowRelative());
        octalSpaceGroupSize = parameters.getOctalSpaceGroupSize();
        decimalSpaceGroupSize = parameters.getDecimalSpaceGroupSize();
        hexadecimalSpaceGroupSize = parameters.getHexadecimalSpaceGroupSize();
    }

    public void saveToParameters(StatusParameters parameters) {
        parameters.setCursorPositionCodeType(cursorPositionFormat.getCodeType());
        parameters.setCursorShowOffset(cursorPositionFormat.isShowOffset());
        parameters.setDocumentSizeCodeType(documentSizeFormat.getCodeType());
        parameters.setDocumentSizeShowRelative(documentSizeFormat.isShowRelative());
        parameters.setOctalSpaceSize(octalSpaceGroupSize);
        parameters.setDecimalSpaceSize(decimalSpaceGroupSize);
        parameters.setHexadecimalSpaceSize(hexadecimalSpaceGroupSize);
    }

    public void setOptions(StatusOptions statusOptions) {
        cursorPositionFormat = statusOptions.cursorPositionFormat;
        documentSizeFormat = statusOptions.documentSizeFormat;
        octalSpaceGroupSize = statusOptions.octalSpaceGroupSize;
        decimalSpaceGroupSize = statusOptions.decimalSpaceGroupSize;
        hexadecimalSpaceGroupSize = statusOptions.hexadecimalSpaceGroupSize;
    }
}

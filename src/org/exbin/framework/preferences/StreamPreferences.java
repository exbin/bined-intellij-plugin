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
package org.exbin.framework.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Preferences over input stream.
 *
 * @version 0.2.1 2021/09/23
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StreamPreferences extends AbstractPreferences {

    public static final String PRECERENCES_DTD_URI = "http://java.sun.com/dtd/preferences.dtd";
    public static final String PREFERENCES_DTD
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!-- DTD for preferences -->"
            + "<!ELEMENT map (entry*) >"
            + "<!ATTLIST map"
            + "  MAP_XML_VERSION CDATA \"0.0\"  >"
            + "<!ELEMENT entry EMPTY >"
            + "<!ATTLIST entry"
            + "          key CDATA #REQUIRED"
            + "          value CDATA #REQUIRED >";

    private final InputStream stream;
    private final Map<String, String> spiValues;
    private Map<String, StreamPreferences> children;

    public StreamPreferences(InputStream stream) {
        super(null, "");
        this.spiValues = new TreeMap<>();
        this.children = new TreeMap<>();
        this.stream = stream;
        init();
    }

    private void init() {
        try {
            sync();
        } catch (BackingStoreException ex) {
            Logger.getLogger(StreamPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void putSpi(String key, String value) {
        spiValues.put(key, value);
        try {
            flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(StreamPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Nullable
    @Override
    protected String getSpi(String key) {
        return spiValues.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        spiValues.remove(key);
        try {
            flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(StreamPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        removeNode();
        flush();
    }

    @Nonnull
    @Override
    protected String[] keysSpi() throws BackingStoreException {
        Set<String> keySet = spiValues.keySet();
        return (String[]) keySet.toArray(new String[keySet.size()]);
    }

    @Nonnull
    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        Set<String> keySet = children.keySet();
        return (String[]) keySet.toArray(new String[keySet.size()]);
    }

    @Nonnull
    @Override
    protected AbstractPreferences childSpi(String name) {
        throw throwCannotEdit();
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        if (isRemoved()) {
            clear();
            return;
        }

        synchronized (stream) {
            importFromStream(stream, this);
        }
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        // ignore - editing not allowed
    }

    private void importFromStream(InputStream fileStream, StreamPreferences p) {
        // Lock for complete import
        synchronized (p.lock) {
            try {
                Document doc = loadPrefsDoc(fileStream);
                importPrefs(p, doc.getDocumentElement());
            } catch (SAXException | IOException ex) {
                Logger.getLogger(StreamPreferences.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Imports the preferences described by the specified XML element (a map
     * from a preferences document) into the specified preferences node.
     */
    private static void importPrefs(Preferences prefsNode, Element map) {
        NodeList entries = map.getChildNodes();
        for (int i = 0, numEntries = entries.getLength(); i < numEntries; i++) {
            Element entry = (Element) entries.item(i);
            prefsNode.put(entry.getAttribute("key"),
                    entry.getAttribute("value"));
        }
    }

    /**
     * Loads an XML document from specified input stream, which must have the
     * requisite DTD URI.
     */
    @Nonnull
    private static Document loadPrefsDoc(InputStream in)
            throws SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setValidating(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new RethrowErrorHandler());
            return db.parse(new InputSource(in));
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    private static IllegalStateException throwCannotEdit() {
        return new IllegalStateException("Cannot edit stream preferences");
    }

    @ParametersAreNonnullByDefault
    private static class Resolver implements EntityResolver {

        @Nonnull
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException {
            if (systemId.equals(StreamPreferences.PRECERENCES_DTD_URI)) {
                InputSource is = new InputSource(new StringReader(StreamPreferences.PREFERENCES_DTD));
                is.setSystemId(StreamPreferences.PRECERENCES_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + systemId);
        }
    }

    @ParametersAreNonnullByDefault
    private static class RethrowErrorHandler implements ErrorHandler {

        @Override
        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

        @Override
        public void warning(SAXParseException ex) throws SAXException {
            throw ex;
        }
    }
}

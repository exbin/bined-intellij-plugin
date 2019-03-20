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
package org.exbin.framework.gui.utils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Static utility methods for central language support.
 *
 * @version 0.2.0 2016/08/18
 * @author ExBin Project (http://exbin.org)
 */
public class LanguageUtils {

    private static ClassLoader languageClassLoader = null;

    public static ClassLoader getLanguageClassLoader() {
        return languageClassLoader;
    }

    public static void setLanguageClassLoader(ClassLoader languageClassLoader) {
        LanguageUtils.languageClassLoader = languageClassLoader;
    }

    /**
     * Returns class name path.
     *
     * Result is canonical name with dots replaced with slashes.
     *
     * @param targetClass target class
     * @return name path
     */
    public static String getClassNamePath(Class<?> targetClass) {
        return targetClass.getCanonicalName().replace(".", "/");
    }

    /**
     * Returns resource bundle for properties file with path derived from class
     * name.
     *
     * @param targetClass target class
     * @return resource bundle
     */
    public static ResourceBundle getResourceBundleByClass(Class<?> targetClass) {
        if (languageClassLoader == null) {
            return ResourceBundle.getBundle(getResourceBaseNameBundleByClass(targetClass));
        } else {
            return new LanguageResourceBundle(getResourceBaseNameBundleByClass(targetClass));
        }
    }

    /**
     * Returns resource bundle base name for properties file with path derived
     * from class name.
     *
     * @param targetClass target class
     * @return base name string
     */
    public static String getResourceBaseNameBundleByClass(Class<?> targetClass) {
        String classNamePath = getClassNamePath(targetClass);
        int classNamePos = classNamePath.lastIndexOf("/");
        return classNamePath.substring(0, classNamePos + 1) + "resources" + classNamePath.substring(classNamePos);
    }

    /**
     * Resource bundle which looks for language resources first and main
     * resources as fallback.
     */
    private static class LanguageResourceBundle extends ResourceBundle {

        private final ResourceBundle mainResourceBundle;
        private final ResourceBundle languageResourceBundle;

        public LanguageResourceBundle(String baseName) {
            mainResourceBundle = ResourceBundle.getBundle(baseName);
            languageResourceBundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), languageClassLoader);
        }

        @Override
        protected Object handleGetObject(String key) {
            Object object = languageResourceBundle.getObject(key);
            if (object == null) {
                object = mainResourceBundle.getObject(key);
            }

            return object;
        }

        @Override
        public Enumeration<String> getKeys() {
            Set<String> keys = new HashSet<>();
            keys.addAll(Collections.list(languageResourceBundle.getKeys()));
            keys.addAll(Collections.list(mainResourceBundle.getKeys()));
            return Collections.enumeration(keys);
        }
    }
}

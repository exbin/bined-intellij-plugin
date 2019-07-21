/////////////////////////////////////////////////////////
//  Bare Bones Browser Launch                          //
//  Version 1.5 (December 10, 2005)                    //
//  By Dem Pilafian                                    //
//  Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
//  Example Usage:                                     //
//     String url = "http://www.centerkey.com/";       //
//     BareBonesBrowserLaunch.openURL(url);            //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////
package org.exbin.framework.gui.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JOptionPane;

@ParametersAreNonnullByDefault
public class BareBonesBrowserLaunch {

    private BareBonesBrowserLaunch() {
    }

    private static final String ERROR_MESSAGE = "Error attempting to launch web browser";

    @SuppressWarnings("unchecked")
    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, new Object[]{url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { //assume Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, ERROR_MESSAGE + ":\n" + e.getLocalizedMessage());
        }
    }

    public static void openDesktopURL(String url) {
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            try {
                java.net.URI uri = new java.net.URI(url);
                desktop.browse(uri);
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(BareBonesBrowserLaunch.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            BareBonesBrowserLaunch.openURL(url);
        }
    }

    public static void openDesktopURL(URI uri) {
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (IOException ex) {
                Logger.getLogger(BareBonesBrowserLaunch.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            BareBonesBrowserLaunch.openURL(uri.toString());
        }
    }

    public static void openDesktopURL(URL url) {
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            try {
                java.net.URI uri = url.toURI();
                desktop.browse(uri);
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(BareBonesBrowserLaunch.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            BareBonesBrowserLaunch.openURL(url.toString());
        }
    }
}

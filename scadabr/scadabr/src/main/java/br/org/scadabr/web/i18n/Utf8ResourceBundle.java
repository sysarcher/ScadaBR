/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.i18n;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author aploese
 */
public class Utf8ResourceBundle {

    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    static final Charset UTF_8 = Charset.forName("UTF-8");

    public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
        return ResourceBundle.getBundle(baseName, locale, loader);
    }

    public static ResourceBundle getBundle(String baseName, Locale locale) {
        return ResourceBundle.getBundle(baseName, locale);
    }

    public static ResourceBundle getBundle(String baseName) {
        return ResourceBundle.getBundle(baseName);
    }

}

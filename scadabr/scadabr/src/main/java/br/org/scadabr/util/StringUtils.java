/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util;

import br.org.scadabr.ImplementMeException;
import java.util.Properties;

/**
 *
 * @author aploese
 */
public class StringUtils {

    @Deprecated //Use Arrays.???
    public static boolean isEmpty(int[] values) {
        throw new ImplementMeException();
    }

    public static String pad(String s, char c, int length) {
        if (s.length() >= length) {
            return s;
        }
        StringBuilder sb = new StringBuilder(length);
        final int amount = length - s.length();
        for (int i = 0; i < amount; i++) {
            sb.append(c);
        }
        sb.append(s);
        return sb.toString();
    }

    @Deprecated
    public static boolean isLengthGreaterThan(String username, int i) {
        throw new ImplementMeException();
    }

    public static String generateRandomString(int i, String string) {
        throw new ImplementMeException();
    }

    public static boolean globWhiteListMatchIgnoreCase(String[] deviceIdWhiteList, String deviceId) {
        throw new ImplementMeException();
    }

    public static String capitalize(String toString) {
        throw new ImplementMeException();
    }

    public static boolean isBetweenInc(int slaveId, int i, int i0) {
        throw new ImplementMeException();
    }

    public static String escapeLT(String value) {
        throw new ImplementMeException();
    }

    public static int parseInt(String substring, int width) {
        throw new ImplementMeException();
    }

    public static String truncate(String localizeI18nKey, int i) {
        throw new ImplementMeException();
    }

    public static String replaceMacros(String dir, Properties properties) {
        throw new ImplementMeException();
    }

    public static String replaceMacro(String timestampSql, String field, String ts) {
        throw new ImplementMeException();
    }

    public static String trimWhitespace(String currentContent) {
        throw new ImplementMeException();
    }

    public static Object[] truncate(String url, int i, String s) {
        throw new ImplementMeException();
    }

    public static String toHex(short s) {
        throw new ImplementMeException();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util;

import br.org.scadabr.ImplementMeException;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author aploese
 */
public class StringUtils {

    public static final Random RANDOM;

    static {
        RANDOM = new Random();
        for (int i = 0; i < 100; i++) {
            RANDOM.nextInt();
        }
    }

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

    public static String generateRandomString(int length, String charSet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(charSet.charAt(RANDOM.nextInt(charSet.length())));
        }
        return sb.toString();
    }

    public static boolean globWhiteListMatchIgnoreCase(String[] deviceIdWhiteList, String deviceId) {
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

    public static String truncate(final String string, final int length) {
        return string.length() > length ? string.substring(0, length) : string;
    }

    public static String replaceMacros(String dir, Properties properties) {
        throw new ImplementMeException();
    }

    public static String replaceMacro(String timestampSql, String field, String ts) {
        throw new ImplementMeException();
    }

    public static Object[] truncate(String url, int i, String s) {
        throw new ImplementMeException();
    }

    public static String toHex(short s) {
        throw new ImplementMeException();
    }

}

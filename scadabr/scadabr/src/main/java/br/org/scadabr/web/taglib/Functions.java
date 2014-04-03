/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib;

import br.org.scadabr.ImplementMeException;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 *
 * @author aploese
 */
public class Functions {

    public static String truncate(String comment, int i) {
        throw new ImplementMeException();
    }

    public static String escapeLessThan(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                default:
                    sb.append(sb);
            }
        }
        return sb.toString();
    }

    public static String escapeAllQuotes(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\"':
                    sb.append("\\\\\"");
                    break;
                case '\'':
                    sb.append("\\\\'");
                    break;
                default:
                    sb.append(sb);
            }
        }
        return sb.toString();
    }

    public static String escapeDoubleQuote(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\"':
                    sb.append("\\\\\"");
                    break;
                default:
                    sb.append(sb);
            }
        }
        return sb.toString();
    }

    public static String escapeSingleQuote(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\'':
                    sb.append("\\\\'");
                    break;
                default:
                    sb.append(sb);
            }
        }
        return sb.toString();
    }

    /**
     * Print the attribute, only if value != null.
     *
     * @param out the JspWriter to write to.
     * @param attributeName the name of the attribure
     * @param attributeValue the value of the attribute, if null nothing is
     * written
     * @throws IOException
     */
    public static void printAttribute(JspWriter out, String attributeName, String attributeValue) throws IOException {
        if (attributeValue != null) {
            out.print(" ");
            out.print(attributeName);
            out.print("=\"");
            out.print(attributeValue);
            out.print("\"");
        }
    }

    public static int size(Object o) throws JspException {
        if ((o instanceof Collection)) {
            return ((Collection) o).size();
        }
        if (o.getClass().isArray()) {
            return ((Object[]) o).length;
        }
        throw new JspException("Object of type " + o.getClass().getName() + " not implemented");
    }

}

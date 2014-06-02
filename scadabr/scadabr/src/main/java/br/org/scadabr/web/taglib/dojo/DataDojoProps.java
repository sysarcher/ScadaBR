/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib.dojo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.JspWriter;

/**
 *
 * @author aploese
 */
public class DataDojoProps {

    private Map<String, Object> props;

    public boolean containsKey(String key) {
        return props == null ? false : props.containsKey(key);
    }

    public void put(String key, Object value) {
        if (props == null) {
            props = new HashMap<>();
        }
        props.put(key, value);
    }

    public void print(JspWriter out) throws IOException {
        if (props != null) {
            boolean firstProp = true;
            out.append(" data-dojo-props=\"");
            for (String prop : props.keySet()) {
                if (firstProp) {
                    firstProp = false;
                } else {
                    out.append(", ");
                }
                out.append(prop);
                out.append(": ");
                final Object propValue = props.get(prop);
                if (propValue instanceof Boolean) {
                    out.append(propValue.toString());
                } else if (propValue instanceof Number) {
                    out.append(propValue.toString());
                } else {
                    out.append('\'');
                    out.append(propValue.toString());
                    out.append('\'');
                }
            }
            out.append('\"');

        }
    }

}

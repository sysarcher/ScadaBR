/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.json;

import br.org.scadabr.ImplementMeException;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author aploese
 */
public class JsonObject extends JsonValue {

    public JsonArray getJsonArray(String name) {
        throw new ImplementMeException();
    }

    public String getString(String name) {
        throw new ImplementMeException();
    }

    public JsonObject getJsonObject(String pointLocator) {
        throw new ImplementMeException();
    }

    public Map<String, JsonValue> getProperties() {
        throw new ImplementMeException();
    }

    public long getLong(String timestamp) {
        throw new ImplementMeException();
    }

    public Integer getInt(String name) {
        throw new ImplementMeException();
    }

    public Double getDouble(String name) {
        throw new ImplementMeException();
    }

    public Boolean getBoolean(String name) {
        throw new ImplementMeException();
    }

    public JsonValue getValue(String name) {
        throw new ImplementMeException();
    }

    public boolean isNull(String tcpConnection) {
        throw new ImplementMeException();
    }

}

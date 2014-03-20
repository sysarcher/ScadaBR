/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.json;

import br.org.scadabr.ImplementMeException;

/**
 *
 * @author aploese
 */
public class JsonReader {

    public JsonReader(String data) {
        throw new ImplementMeException();
    }

    public void populateObject(Object pointLocator, JsonObject locatorJson) throws JsonException {
        throw new ImplementMeException();
    }

    public <T> T readPropertyValue(JsonValue pointHierarchyJson, Class<T> aClass, Class<?> aClass0) throws JsonException {
        throw new ImplementMeException();
    }

    public <T> T readObject(JsonObject jsonObject, Class<T> aClass) throws JsonException {
        throw new ImplementMeException();
    }

    public JsonValue inflate() throws JsonException {
        throw new ImplementMeException();
    }

}

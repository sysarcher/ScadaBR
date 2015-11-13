/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.json.dao;

import br.org.scadabr.json.ColorDeserializer;
import br.org.scadabr.json.ColorSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.inject.Named;

/**
 *
 * @author aploese
 */
@Named
public class JsonMapperFactory {

    private ObjectMapper objectMapper;

    public JsonMapperFactory() {

        objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping();

        SimpleModule testModule = new SimpleModule(); //"ScadaBR-Json", new Version(1, 0, 0, null, "br.org.scadabr", "json"));
        testModule.addSerializer(new ColorSerializer());
        testModule.addDeserializer(Color.class, new ColorDeserializer());
        objectMapper.registerModule(testModule);
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    @JsonIgnore
    public ObjectWriter getWriter() {
        return objectMapper.writerWithView(JsonPersistence.class);
    }

    @JsonIgnore
    public ObjectReader getReader() {
        return objectMapper.readerWithView(JsonPersistence.class);
    }

    public String writeValueAsString(Object o) {
        try {
            return getWriter().writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public StringReader write(Object o) {
        return new StringReader(writeValueAsString(o));
    }

    public Object read(String className, InputStream is) {
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(className);
            return objectMapper.readValue(is, clazz);
        } catch (ClassNotFoundException | IOException classNotFoundException) {
            throw new RuntimeException(classNotFoundException);
        }
    }

    public Object read(String className, String s) {
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(className);
            return objectMapper.readValue(s, clazz);
        } catch (ClassNotFoundException | IOException classNotFoundException) {
            throw new RuntimeException(classNotFoundException);
        }
    }
}

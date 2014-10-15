/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.rest;

import java.util.List;

/**
 *
 * @author aploese
 */
public class JsonPointValueSeries {
    
    public JsonPointValueSeries(int id, List<JsonPointValue> values) {
        this.id = id;
        this.values = values;
    }
    
    final private int id;
    final private List<JsonPointValue> values;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the values
     */
    public List<JsonPointValue> getValues() {
        return values;
    }
    
}

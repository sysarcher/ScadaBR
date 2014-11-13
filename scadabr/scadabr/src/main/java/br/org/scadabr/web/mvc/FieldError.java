/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc;

/**
 *
 * @author aploese
 */
public class FieldError {
    private final String field;
    private final String msg;
    private final Object rejectedValue;

    public FieldError(String field, Object rejectedValue, String msg) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.msg = msg;
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @return the rejectedValue
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }
    
}

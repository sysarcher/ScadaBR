/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc;

import java.util.LinkedList;
import java.util.List;
import org.springframework.validation.BindingResult;

/**
 *
 * @author aploese
 */
public class AjaxFormPostResponse {
    private final Object target;
    private final List<FieldError> fieldErrors = new LinkedList<>();
    private final List<String> objectErrors = new LinkedList<>();
    
    
    public AjaxFormPostResponse(BindingResult bindingResult) {
        this.target = bindingResult.getTarget();
        if (bindingResult.hasGlobalErrors()) {
            for (org.springframework.validation.ObjectError oe : bindingResult.getGlobalErrors()) {
                objectErrors.add(oe.getDefaultMessage());
            }
        }
        if (bindingResult.hasFieldErrors()) {
            for (org.springframework.validation.FieldError fe : bindingResult.getFieldErrors()) {
                fieldErrors.add(new FieldError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
            }
        }
    }

    public Object getTarget() {
        return target;
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}

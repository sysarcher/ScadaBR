/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc;

import br.org.scadabr.web.l10n.RequestContextAwareLocalizer;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.validation.BeanPropertyBindingResult;

/**
 *
 * @author aploese
 */
@Configurable
public class AjaxFormPostResponse {
    private final Object target;
    private final List<FieldError> fieldErrors = new LinkedList<>();
    @Autowired
    private transient RequestContextAwareLocalizer localizer;
    
    public AjaxFormPostResponse(BeanPropertyBindingResult bindingResult) {
        this.target = bindingResult.getTarget();
        if (bindingResult.hasFieldErrors()) {
            for (org.springframework.validation.FieldError fe : bindingResult.getFieldErrors()) {
                fieldErrors.add(new FieldError(fe.getField(), fe.getRejectedValue(), localizer.getMessage(fe.getCode(), fe.getArguments())));
            }
        }
    }

    public AjaxFormPostResponse(BeanPropertyBindingResult bindingResult, RequestContextAwareLocalizer localizer) {
        this.target = bindingResult.getTarget();
        if (bindingResult.hasFieldErrors()) {
            for (org.springframework.validation.FieldError fe : bindingResult.getFieldErrors()) {
                fieldErrors.add(new FieldError(fe.getField(), fe.getRejectedValue(), localizer.getMessage(fe.getCode(), fe.getArguments())));
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

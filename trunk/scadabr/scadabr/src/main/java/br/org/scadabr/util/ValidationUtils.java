/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util;

import br.org.scadabr.ImplementMeException;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import org.springframework.validation.BindException;

/**
 *
 * @author aploese
 */
public class ValidationUtils {

    public static void rejectValue(BindException errors, String loggingType, String validaterequired) {
        throw new ImplementMeException();
    }

    public static void reject(BindException errors, String loginvalidationaccountDisabled) {
        throw new ImplementMeException();
    }

    public static void reject(BindException errors, String validatepedxidUsed, String xid) {
        throw new ImplementMeException();
    }

    public static void reject(BindException errors, String view, DwrResponseI18n response) {
        throw new ImplementMeException();
    }

}

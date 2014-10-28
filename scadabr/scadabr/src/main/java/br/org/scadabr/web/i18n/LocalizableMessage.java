/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.i18n;

/**
 *
 * @author aploese
 */
public interface LocalizableMessage {
    public final static Object[] EMPTY_ARGS = new Object[0];
    
    String getI18nKey();

    Object[] getArgs();

}

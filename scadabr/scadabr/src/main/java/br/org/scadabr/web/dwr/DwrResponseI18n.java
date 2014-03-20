/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.dwr;

import br.org.scadabr.ImplementMeException;
import br.org.scadabr.web.i18n.LocalizableMessage;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author aploese
 */
public class DwrResponseI18n {

    public void addMessage(String i18nKey, Object... args) {
        throw new ImplementMeException();
    }

    public void addMessage(LocalizableMessage msg) {
        throw new ImplementMeException();
    }

    public void addContextualMessage(String i18nKey, Object... args) {
        throw new ImplementMeException();
    }

    public void addGenericMessage(String i18nKey, Object... args) {
        throw new ImplementMeException();
    }

    public void setMessages(List<DwrMessageI18n> l) {
        throw new ImplementMeException();
    }

    //Todo change to hasMessages???
    public boolean getHasMessages() {
        throw new ImplementMeException();
    }

    public Iterable<DwrMessageI18n> getMessages() {
        throw new ImplementMeException();
    }

    @Deprecated //USe Localizer
    public String toString(ResourceBundle b) {
        throw new ImplementMeException();
    }

    public void addData(String name, Object b) {
        throw new ImplementMeException();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util;

import br.org.scadabr.web.i18n.LocalizableMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import org.junit.Ignore;

/**
 *
 * @author aploese
 */
class ScadaBrObjectInputStream extends ObjectInputStream {

    public ScadaBrObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {
        String name = desc.getName();
        switch (name) {
            case "com.serotonin.Message":
                desc = ObjectStreamClass.lookup(LocalizableMessage.class);
                break;
            default:
        }
        return super.resolveClass(desc);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aploese
 */
public class DummyB implements Serializable {

    private static final long serialVersionUID = -1;
    private final static int version = 2;
    private String field1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.write(version);
        out.writeObject(field1);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        switch (in.readInt()) {
            case 1:
                field1 = Integer.toString(in.readInt());
                break;
            case 2:
                try {
                    field1 = (String) in.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

}

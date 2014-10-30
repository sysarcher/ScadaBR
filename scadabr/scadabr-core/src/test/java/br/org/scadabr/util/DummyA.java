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

/**
 *
 * @author aploese
 */
public class DummyA implements Serializable {

    private static final long serialVersionUID = -1;
    private final static int version = 1;
    private int field1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(field1);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        switch (in.readInt()) {
            case 1:
                field1 = in.readInt();
                break;
            default:
                throw new RuntimeException();
        }
    }

}

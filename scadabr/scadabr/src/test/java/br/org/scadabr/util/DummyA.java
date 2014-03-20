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

    private void writeObject(ObjectOutputStream out) throws IOException {
    }

    private void readObject(ObjectInputStream in) throws IOException {
    }

}

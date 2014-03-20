/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util.collections;

import br.org.scadabr.ImplementMeException;
import java.util.Enumeration;
import java.util.Iterator;

/**
 *
 * @author aploese
 */
public class EnumerationIterator<T extends Object> implements Iterable<T> {

    public EnumerationIterator(Enumeration e) {
        throw new ImplementMeException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new ImplementMeException();
    }

}

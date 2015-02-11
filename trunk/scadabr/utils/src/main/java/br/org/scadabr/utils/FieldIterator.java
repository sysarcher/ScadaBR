/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Iterator;

/**
 *
 * @author aploese
 */
public class FieldIterator implements Iterator<Field> {

    private Class clazz;
    private Field[] declaredFields;
    private int index;
    private Field nextField;
    private final Class<? extends Annotation> annotation;

    public FieldIterator(Class clazz, Class<? extends Annotation> annotation) {
        if (clazz == null) {
            throw new IllegalArgumentException();
        }
        this.annotation = annotation;
        this.clazz = clazz;
        declaredFields = clazz.getDeclaredFields();
        index = 0;
        fetchNextField();
    }

    public FieldIterator(Class clazz) {
        this(clazz, null);
    }

    private void fetchNextField() {
        do {
            if (index < declaredFields.length) {
                if (annotation == null) {
                    nextField = declaredFields[index++];
                    return;
                } else {
                    for (int i = index; i < declaredFields.length; i++) {
                        nextField = declaredFields[i];
                        if (nextField.isAnnotationPresent(annotation)) {
                            index = i + 1;
                            return;
                        }
                    }

                }
            }
            clazz = clazz.getSuperclass();
            declaredFields = clazz.getDeclaredFields();
            index = 0;
        } while (clazz != Object.class);
        nextField = null;
    }

    @Override
    public boolean hasNext() {
        return nextField != null;
    }

    @Override
    public Field next() {
        final Field result = nextField;
        if (nextField == null) {
            throw new IndexOutOfBoundsException();
        }
        fetchNextField();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

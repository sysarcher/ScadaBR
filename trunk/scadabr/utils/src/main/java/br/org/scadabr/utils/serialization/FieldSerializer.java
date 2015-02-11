/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.utils.serialization;

import br.org.scadabr.utils.FieldIterator;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 *
 * @author aploese
 */
public class FieldSerializer extends InputStream {

    private Serializable s;
    private FieldIterator fi;
    private byte[] byteCache = new byte[]{'{', 0x0A};
    private int pos;
    private boolean endSignWritten;

    public FieldSerializer(Serializable s) {
        this.s = s;
        this.fi = new FieldIterator(s.getClass(), SerializabeField.class);

    }

    @Override
    public int read() throws IOException {
        if (pos == byteCache.length) {
            if (!fi.hasNext()) {
                if (!endSignWritten) {
                    byteCache = new byte[]{'}'};
                    endSignWritten = true;
                    pos = 0;
                } else {
                    return -1;
                }
            } else {
                Field f = fi.next();
                final boolean acessible = f.isAccessible();
                try {
                    if (!acessible) {
                        f.setAccessible(true);
                    }
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    DataOutputStream ds = new DataOutputStream(bo);
                    ds.writeBytes(f.getName());
                    if (f.getType().isPrimitive()) {
                        switch (f.getType().getName()) {
                            case "char":
                                ds.writeBytes("(C)=");
                                ds.writeChar(f.getChar(s));
                                break;
                            case "boolean":
                                ds.writeBytes("(Z)=");
                                ds.writeBoolean(f.getBoolean(s));
                                break;
                            case "byte":
                                ds.writeBytes("(B)=");
                                ds.writeByte(f.getByte(s));
                                break;
                            case "short":
                                ds.writeBytes("(S)=");
                                ds.writeShort(f.getShort(s));
                                break;
                            case "int":
                                ds.writeBytes("(I)=");
                                ds.writeInt(f.getInt(s));
                                break;
                            case "long":
                                ds.writeBytes("(J)=");
                                ds.writeLong(f.getLong(s));
                                break;
                            case "float":
                                ds.writeBytes("(F)=");
                                ds.writeFloat(f.getFloat(s));
                                break;
                            case "double":
                                ds.writeBytes("(D)=");
                                ds.writeDouble(f.getDouble(s));
                                break;
                            default:
                                throw new RuntimeException();
                        }
                    } else {
                        final Object value = f.get(s);
                        ds.writeBytes("(L" + (value == null ? f.getType().getName() : value.getClass().getName()) + ")");
                        if (value != null) {
                            ds.writeBytes("=");
                            switch (f.getType().getName()) {
                                case "java.lang.String":
                                    ds.writeUTF((String) value);
                                    break;
                                case "java.lang.Boolean":
                                    ds.writeBoolean((Boolean) value);
                                    break;
                                case "java.lang.Character":
                                    ds.writeChar((Character) value);
                                    break;
                                case "java.lang.Byte":
                                    ds.writeByte((Byte) value);
                                    break;
                                case "java.lang.Short":
                                    ds.writeShort((Short) value);
                                    break;
                                case "java.lang.Integer":
                                    ds.writeInt((Integer) value);
                                    break;
                                case "java.lang.Long":
                                    ds.writeLong((Long) value);
                                    break;
                                case "java.lang.Float":
                                    ds.writeFloat((Float) value);
                                    break;
                                case "java.lang.Double":
                                    ds.writeDouble((Double) value);
                                    break;
                                default:
                                    if (value instanceof Enum) {
                                        ds.writeBytes(((Enum) value).name());
                                    } else {
                                        throw new RuntimeException("Not supported: " + f.getType().getName());
                                    }

                            }
                        }
                    }
                    ds.writeByte(0x0A);
                    ds.close();
                    byteCache = bo.toByteArray();
                    pos = 0;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (!acessible) {
                        f.setAccessible(acessible);
                    }
                }

            }
        }
        return byteCache[pos++] & 0xFF;
    }

}

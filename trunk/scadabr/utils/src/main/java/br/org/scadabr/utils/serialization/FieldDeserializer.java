/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.utils.serialization;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.lang.reflect.Field;

/**
 *
 * @author aploese
 */
public class FieldDeserializer {

    private final Serializable s;
    private Field field;
    private String fieldType;
    private final StringBuilder stringBuilder = new StringBuilder();
    private int pos;
    private byte[] buffer;
    private int bufferPos;
    private int bytesLeft;
    private short rawShort;
    private int rawInt;
    private long rawLong;

    private FieldDeserializer.State state = State.START;

    public FieldDeserializer(String binaryClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        s = (Serializable) getClass().getClassLoader().loadClass(binaryClassName).newInstance();
    }

    private void setNullValue() {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(s, null);
            state = State.PARSE_NAME;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    public Serializable deserializeObject(InputStream is) throws IOException {
        int i = is.read();
        while (i != -1) {
            write(i);
            i = is.read();
        }
        if (state != State.END) {
            throw new RuntimeException("Parsing went wrong");
        }
        return s;
    }

    private String getStringValue() throws IOException {
        int utflen = buffer.length;
        char[] chararr = new char[utflen];

        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        while (count < utflen) {
            c = (int) buffer[count] & 0xff;
            if (c > 127) {
                break;
            }
            count++;
            chararr[chararr_count++] = (char) c;
        }

        while (count < utflen) {
            c = (int) buffer[count] & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen) {
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    }
                    char2 = (int) buffer[count - 1];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
                    }
                    chararr[chararr_count++] = (char) (((c & 0x1F) << 6)
                            | (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen) {
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    }
                    char2 = (int) buffer[count - 2];
                    char3 = (int) buffer[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException(
                                "malformed input around byte " + (count - 1));
                    }
                    chararr[chararr_count++] = (char) (((c & 0x0F) << 12)
                            | ((char2 & 0x3F) << 6)
                            | ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte " + count);
            }
        }
        return new String(chararr, 0, chararr_count);
    }

    private void setStringValue(String value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(s, value);
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setBooleanValue(boolean value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setBoolean(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setCharValue(char value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setChar(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setByteValue(byte value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setByte(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setShortValue(short value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setShort(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setIntValue(int value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setInt(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setLongValue(long value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setLong(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setFloatValue(float value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setFloat(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setDoubleValue(double value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            if (field.getType().isPrimitive()) {
                field.setDouble(s, value);
            } else {
                field.set(s, value);
            }
            state = State.COLLECT_0X0A;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void setEnumValue(Enum value) {
        final boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(s, value);
            state = State.PARSE_NAME;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void getField(String fieldName) {
        Class clazz = s.getClass();
        field = null;
        do {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException | SecurityException ex) {
                clazz = clazz.getSuperclass();
            }
        } while (field == null && clazz != Object.class);
    }

    enum State {

        START,
        PARSE_NAME,
        PARSE_TYPE,
        PARSE_VALUE_START,
        COLLECT_BOOLEAN_VALUE,
        COLLECT_CHARACTER_VALUE,
        COLLECT_BYTE_VALUE,
        COLLECT_SHORT_VALUE,
        COLLECT_INTEGER_VALUE,
        COLLECT_LONG_VALUE,
        COLLECT_FLOAT_VALUE,
        COLLECT_DOUBLE_VALUE,
        COLLECT_STRING_VALUE_LENGTH,
        COLLECT_STRING_VALUE,
        COLLECT_ENUM_VALUE,
        COLLECT_0X0A,
        END;
    }

    public void write(int b) throws IOException {
        b &= 0xFF;
        pos++;
        switch (state) {
            case START:
                if (pos == 1 & b == '{') {
                } else if (pos == 2 & b == 0x0A) {
                    field = null;
                    fieldType = null;
                    buffer = null;
                    bufferPos = 0;
                    bytesLeft = 0;
                    stringBuilder.setLength(0);
                    state = State.PARSE_NAME;
                } else {
                    throw new RuntimeException();
                }
                break;
            case PARSE_NAME:
                switch (b) {
                    case '}':
                        state = State.END;
                        break;
                    case '(':
                        state = State.PARSE_TYPE;
                        getField(stringBuilder.toString());
                        stringBuilder.setLength(0);
                        break;
                    default:
                        stringBuilder.append((char) b);
                }
                break;
            case PARSE_TYPE:
                if (b == ')') {
                    state = State.PARSE_VALUE_START;
                    fieldType = stringBuilder.toString();
                    stringBuilder.setLength(0);
                } else {
                    stringBuilder.append((char) b);
                }
                break;
            case PARSE_VALUE_START:
                if (b == '=') {
                    switch (fieldType.toString()) {
                        case "Z":
                        case "Ljava.lang.Boolean":
                            state = State.COLLECT_BOOLEAN_VALUE;
                            break;
                        case "C":
                        case "Ljava.lang.Character":
                            rawShort = 0;
                            bytesLeft = 2;
                            state = State.COLLECT_CHARACTER_VALUE;
                            break;
                        case "B":
                        case "Ljava.lang.Byte":
                            state = State.COLLECT_BYTE_VALUE;
                            break;
                        case "S":
                        case "Ljava.lang.Short":
                            rawShort = 0;
                            bytesLeft = 2;
                            state = State.COLLECT_SHORT_VALUE;
                            break;
                        case "I":
                        case "Ljava.lang.Integer":
                            rawInt = 0;
                            bytesLeft = 4;
                            state = State.COLLECT_INTEGER_VALUE;
                            break;
                        case "J":
                        case "Ljava.lang.Long":
                            rawLong = 0;
                            bytesLeft = 8;
                            state = State.COLLECT_LONG_VALUE;
                            break;
                        case "F":
                        case "Ljava.lang.Float":
                            rawInt = 0;
                            bytesLeft = 4;
                            state = State.COLLECT_FLOAT_VALUE;
                            break;
                        case "D":
                        case "Ljava.lang.Double":
                            rawLong = 0;
                            bytesLeft = 8;
                            state = State.COLLECT_DOUBLE_VALUE;
                            break;
                        case "Ljava.lang.String":
                            rawInt = 0;
                            bytesLeft = 2;
                            state = State.COLLECT_STRING_VALUE_LENGTH;
                            break;
                        default:
                            Class c;
                            try {
                                c = getClass().getClassLoader().loadClass(fieldType.substring(1));
                            } catch (ClassNotFoundException ex) {
                                throw new RuntimeException(ex);
                            }
                            if (Enum.class.isAssignableFrom(c)) {
                                stringBuilder.setLength(0);
                                state = State.COLLECT_ENUM_VALUE;
                            } else {
                                throw new RuntimeException();
                            }
                    }
                } else if (b == 0x0A) {
                    setNullValue();
                } else {
                    throw new RuntimeException();
                }
                break;
            case COLLECT_BOOLEAN_VALUE:
                setBooleanValue(b != 0);
                break;
            case COLLECT_CHARACTER_VALUE:
                rawShort |= (short) b;
                if (0 == --bytesLeft) {
                    setCharValue((char) rawShort);
                } else {
                    rawShort <<= 8;
                }
                break;
            case COLLECT_BYTE_VALUE:
                setByteValue((byte) b);
                break;
            case COLLECT_SHORT_VALUE:
                rawShort |= (short) b;
                if (0 == --bytesLeft) {
                    setShortValue(rawShort);
                } else {
                    rawShort <<= 8;
                }
                break;
            case COLLECT_INTEGER_VALUE:
                rawInt |= (int) b;
                if (0 == --bytesLeft) {
                    setIntValue(rawInt);
                } else {
                    rawInt <<= 8;
                }
                break;
            case COLLECT_LONG_VALUE:
                rawLong |= (long) b;
                if (0 == --bytesLeft) {
                    setLongValue(rawLong);
                } else {
                    rawLong <<= 8;
                }
                break;
            case COLLECT_FLOAT_VALUE:
                rawInt |= (int) b;
                if (0 == --bytesLeft) {
                    setFloatValue(Float.intBitsToFloat(rawInt));
                } else {
                    rawInt <<= 8;
                }
                break;
            case COLLECT_DOUBLE_VALUE:
                rawLong |= (long) b;
                if (0 == --bytesLeft) {
                    setDoubleValue(Double.longBitsToDouble(rawLong));
                } else {
                    rawLong <<= 8;
                }
                break;
            case COLLECT_STRING_VALUE_LENGTH:
                rawInt |= (int) b;
                if (0 == --bytesLeft) {
                    buffer = new byte[rawInt];
                    bufferPos = 0;
                    state = State.COLLECT_STRING_VALUE;
                } else {
                    rawInt <<= 8;
                }
                break;
            case COLLECT_STRING_VALUE:
                buffer[bufferPos++] = (byte) b;
                if (buffer.length == bufferPos) {
                    setStringValue(getStringValue());
                }
                break;
            case COLLECT_ENUM_VALUE:
                if (b == 0x0a) {
                    try {
                        Class<Enum> ce = (Class<Enum>) getClass().getClassLoader().loadClass(fieldType.substring(1));
                        setEnumValue(Enum.valueOf(ce, stringBuilder.toString()));
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    stringBuilder.setLength(0);
                } else {
                    stringBuilder.append((char) b);
                }
                break;
            case COLLECT_0X0A:
                if (b == 0x0A) {
                    state = State.PARSE_NAME;
                } else {
                    throw new RuntimeException();
                }
                break;
            case END:
                throw new RuntimeException();
            default:
                throw new RuntimeException();

        }
    }

}

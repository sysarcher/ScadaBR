/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.io;

import br.org.scadabr.ImplementMeException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author aploese
 */
public class StreamUtils {

    public static void transfer(InputStream in, OutputStream out) throws IOException {
        throw new ImplementMeException();
    }

    public static void transfer(InputStream in, OutputStream out, int i) throws IOException {
        throw new ImplementMeException();
    }

    public static void transfer(Reader r, Writer w) throws IOException {
        throw new ImplementMeException();
    }

    public static int read4ByteSigned(InputStream in) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static byte readByte(InputStream in) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void write4ByteSigned(OutputStream out, int length) throws IOException {
        throw new ImplementMeException();
    }

    public static void writeByte(OutputStream out, byte id) throws IOException {
        throw new ImplementMeException();
    }

}

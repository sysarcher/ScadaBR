/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.email;

import br.org.scadabr.ImplementMeException;

/**
 *
 * @author aploese
 */
public class EmailInline {

    public static class ByteArrayInline extends EmailInline {

        public ByteArrayInline(String string, byte[] imageData, String contentType) {
            throw new ImplementMeException();
        }
    }

    public static class FileInline extends EmailInline {

        public FileInline(String s, String realPath) {
            throw new ImplementMeException();
        }
    }

}

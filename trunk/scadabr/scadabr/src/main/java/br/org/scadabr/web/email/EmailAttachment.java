/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.email;

import br.org.scadabr.ImplementMeException;
import java.io.File;

/**
 *
 * @author aploese
 */
public class EmailAttachment {

    public static class FileAttachment extends EmailAttachment {

        public FileAttachment(String string, File zipFile) {
            throw new ImplementMeException();
        }
    }

}

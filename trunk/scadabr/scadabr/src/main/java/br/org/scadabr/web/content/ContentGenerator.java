/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.content;

import br.org.scadabr.ImplementMeException;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author aploese
 */
public class ContentGenerator {

    public static String generateContent(HttpServletRequest request, String string, Map<String, Object> model) throws ServletException, IOException {
        throw new ImplementMeException();
    }

}

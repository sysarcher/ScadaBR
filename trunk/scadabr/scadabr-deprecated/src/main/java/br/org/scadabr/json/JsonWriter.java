/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.json;

import br.org.scadabr.utils.ImplementMeException;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author aploese
 */
public class JsonWriter {

    public void setPrettyIndent(int prettyIndent) {
        throw new ImplementMeException();
    }

    public void setPrettyOutput(boolean b) {
        throw new ImplementMeException();
    }

    public String write(Map<String, Object> data) throws JsonException, IOException {
        throw new ImplementMeException();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib.dijit;

import br.org.scadabr.web.taglib.DojoInputTag;

/**
 *
 * @author aploese
 */
public class TextBoxTag extends DojoInputTag {

    private Object value;
    
    public TextBoxTag() {
        super("dijit/form/TextBox");
    }
    
    @Override
    protected String getValue0()  {
        return value == null ? null : value.toString();
    }
    
    public void setValue(Object value) {
        this.value = value;
    }

}

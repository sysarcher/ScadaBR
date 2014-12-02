/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.taglib.dojox;

import br.org.scadabr.web.taglib.DojoTag;

/**
 *
 * @author aploese
 */
public class ContentPaneTag extends DojoTag {

    public ContentPaneTag() {
        super("div", "dojox/layout/ContentPane");
        putDataDojoProp("parseOnLoad", false);
    }

    public void setSplitter(boolean splitter) {
        putDataDojoProp("splitter", splitter);
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.i18n;

import com.serotonin.web.i18n.LocalizableMessage;
import java.util.ResourceBundle;

/**
 *
 * @author aploese
 */
public interface LocalizableEnum<T extends Enum<T>> {
        LocalizableMessage getMessageI18n();

    String getName();    
        
    String getLocalizedMessage(ResourceBundle bundle);

    String getI18nMessageKey();
    
    Class<T> getEnum();

}

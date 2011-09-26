/*
Mango - Open Source M2M - http://mango.serotoninsoftware.com
Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
@author Matthew Lohbihler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.web.i18n.LocalizableMessage;
import com.sun.mail.handlers.message_rfc822;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.ResourceBundle;

public enum MangoDataType implements Serializable {

    UNKNOWN(0,"common.unknown"),
    BINARY(1, "common.dataTypes.binary"),
    MULTISTATE(2, "common.dataTypes.multistate"),
    NUMERIC(3, "common.dataTypes.numeric"),
    ALPHANUMERIC(4, "common.dataTypes.alphanumeric"),
    IMAGE(5, "common.dataTypes.image");

    public final static String ALIAS_DATA_TYPE = "dataType";

    public static MangoDataType fromMangoId(int mangoId) {
        for (MangoDataType dt : MangoDataType.values()) {
            if (dt.mangoId == mangoId) {
                return dt;
            }
        }
        throw new ShouldNeverHappenException("Cant find dataType of mangoId: " + mangoId);
    }
    
    private final LocalizableMessage dataTypeMessage;
    public final int mangoId;
    
    private MangoDataType(int mangoId, String msg) {
        this.mangoId = mangoId;
        this.dataTypeMessage = new LocalizableMessage(msg);
    }

    public LocalizableMessage getLocalizableMessage() {
        return dataTypeMessage;
    }
    
    public static EnumSet<MangoDataType> getCodeList(EnumSet<MangoDataType> exclude) {
        return EnumSet.complementOf(exclude);
    }
    
    public static EnumSet<MangoDataType> getCodeList(MangoDataType exclude) {
        EnumSet<MangoDataType> result =  EnumSet.allOf(MangoDataType.class);
        result.remove(exclude);
        return result;
    }

    public String getLocalizedMessage(ResourceBundle bundle) {
        return dataTypeMessage.getLocalizedMessage(bundle);
    }
    
    public String getI18nMessageKey() {
        return dataTypeMessage.getKey();
    }

}

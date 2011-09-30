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
import com.serotonin.mango.i18n.LocalizableEnum;
import com.serotonin.web.i18n.LocalizableMessage;
import java.util.EnumSet;
import java.util.ResourceBundle;

public enum MangoDataType implements LocalizableEnum<MangoDataType> {

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
    
    private final LocalizableMessage messageI18n;
    public final int mangoId;
    
    private MangoDataType(int mangoId, String keyI18n) {
        this.mangoId = mangoId;
        this.messageI18n = new LocalizableMessage(keyI18n);
    }

    public static EnumSet<MangoDataType> getCodeList(EnumSet<MangoDataType> exclude) {
        return EnumSet.complementOf(exclude);
    }
    
    public static EnumSet<MangoDataType> getCodeList(MangoDataType exclude) {
        EnumSet<MangoDataType> result =  EnumSet.allOf(MangoDataType.class);
        result.remove(exclude);
        return result;
    }

    @Override
    public LocalizableMessage getMessageI18n() {
        return messageI18n;
    }
    
    @Override
    public String getLocalizedMessage(ResourceBundle bundle) {
        return messageI18n.getLocalizedMessage(bundle);
    }
    
    @Override
    public String getI18nMessageKey() {
        return messageI18n.getKey();
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Class<MangoDataType> getEnum() {
        return MangoDataType.class;
    }

}

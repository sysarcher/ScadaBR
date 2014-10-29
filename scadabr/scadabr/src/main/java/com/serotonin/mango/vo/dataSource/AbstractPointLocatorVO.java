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
package com.serotonin.mango.vo.dataSource;

import br.org.scadabr.DataType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.util.LocalizableJsonException;
import br.org.scadabr.i18n.LocalizableMessage;
import java.util.Set;

abstract public class AbstractPointLocatorVO implements PointLocatorVO {

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        in.readInt(); // Read the version. Value is currently not used.
    }

    protected void serializeDataType(Map<String, Object> map) {
        map.put("dataType", getDataType().getName());
    }

    protected DataType deserializeDataType(JsonObject json, Set<DataType> excludeTypes) throws JsonException {
        String text = json.getString("dataType");
        if (text == null) {
            return null;
        }

        try {

            return DataType.valueOf(text);
        } catch (Exception e) {
            throw new LocalizableJsonException("emport.error.invalid", "dataType", text,
                    DataType.nameValues());
        }
    }

    /**
     * Defaults to returning null. Override to return something else.
     */
    @Override
    public DataPointSaveHandler getDataPointSaveHandler() {
        return null;
    }

    /**
     * Defaults to returning false. Override to return something else.
     */
    @Override
    public boolean isRelinquishable() {
        return false;
    }
}

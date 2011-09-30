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
package com.serotonin.mango.web.dwr.beans;

import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.web.i18n.LocalizableMessage;

public class DataPointBean {
    private int id;
    private String name;
    private boolean settable;
    private MangoDataType mangoDataType = MangoDataType.UNKNOWN;
    private final LocalizableMessage mangoDataTypeI18n;
    private final String chartColour;

    public DataPointBean(DataPointVO vo) {
        id = vo.getId();
        name = vo.getExtendedName();
        settable = vo.getPointLocator().isSettable();
        mangoDataType = vo.getPointLocator().getMangoDataType();
        mangoDataTypeI18n = mangoDataType.getMessageI18n();
        chartColour = vo.getChartColour();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    public MangoDataType getMangoDataType() {
        return mangoDataType;
    }

    public void setMangoDataType(MangoDataType mangoDataType) {
        this.mangoDataType = mangoDataType;
    }

    public LocalizableMessage getMangoDataTypeI18n() {
        return mangoDataTypeI18n;
    }

    public String getChartColour() {
        return chartColour;
    }
}

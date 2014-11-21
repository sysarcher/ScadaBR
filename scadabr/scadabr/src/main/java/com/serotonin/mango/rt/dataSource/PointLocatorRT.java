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
package com.serotonin.mango.rt.dataSource;

import br.org.scadabr.DataType;
import br.org.scadabr.vo.dataSource.PointLocatorVO;

/**
 * This type provides the data source with the information that it needs to
 * locate the point data.
 *
 * @author mlohbihler
 * @param <T>
 * @param <U>
 */
abstract public class PointLocatorRT<T extends PointLocatorVO> {

    protected final T vo;

    public PointLocatorRT(T vo) {
        this.vo = vo;
    }

    public boolean isSettable() {
        return vo.isSettable();
    }

    public boolean isRelinquishable() {
        return false;
    }

    public final T getVo() {
        return vo;
    }

    public final DataType getDataType() {
        return vo.getDataType();
    }

}

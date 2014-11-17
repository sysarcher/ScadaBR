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
package br.org.scadabr.vo.dataSource;

import br.org.scadabr.DataType;
import java.io.Serializable;

import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.util.ChangeComparableObject;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import com.serotonin.mango.vo.dataSource.DataPointSaveHandler;

public interface PointLocatorVO extends Serializable, ChangeComparableObject {

    /**
     * One of the com.serotonin.mango.DataType
     *
     * @return
     */
    DataType getDataType();

    /**
     * An arbitrary description of the point location configuration for human
     * consumption.
     *
     * @return
     */
    LocalizableMessage getConfigurationDescription();

    /**
     * Can the value be set in the data source?
     *
     * @return
     */
    boolean isSettable();

    /**
     * Supplemental to being settable, can the set value be relinquished?
     *
     * @return
     */
    boolean isRelinquishable();

    /**
     * Create a runtime version of the locator
     *
     * @return
     */
    PointLocatorRT createRuntime();

    DataPointSaveHandler getDataPointSaveHandler();

    String getName();
    
    int getId();
    
    void setId(int id);
    
}

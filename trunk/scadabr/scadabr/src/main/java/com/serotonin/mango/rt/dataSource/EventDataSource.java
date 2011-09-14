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

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import java.util.List;
import java.util.ArrayList;

/**
 * This class holds a list with the active dataPoints.
 * 
 * access must be synchronized  by the field #dataPoints
 * 
 * @author aploese
 */
abstract public class EventDataSource extends DataSourceRT {

    public EventDataSource(DataSourceVO<?> vo, boolean doCache) {
        super(vo, doCache);
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // Typically, event based data sources cannot set point values, so don't make subclasses implement this.
    }

}

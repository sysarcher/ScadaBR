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
package com.serotonin.mango.rt.dataImage;

import br.org.scadabr.DataType;
import java.io.Serializable;

import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.view.stats.IValueTime;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;

/**
 * The simple value of a point at a given time.
 *
 * @see AnnotatedPointValueTime
 * @author Matthew Lohbihler
 */
public class PointValueTime<T extends MangoValue>  implements Serializable, IValueTime<T> {

    private static final long serialVersionUID = -1;

    public static <T extends MangoValue> boolean equalValues(PointValueTime<T> pvt1, PointValueTime<T> pvt2) {
        if (pvt1 == null && pvt2 == null) {
            return true;
        }
        if (pvt1 == null || pvt2 == null) {
            return false;
        }
        return Objects.equals(pvt1.getValue(), pvt2.getValue());
    }

    public static <T extends MangoValue>  T getValue(PointValueTime<T> pvt) {
        if (pvt == null) {
            return null;
        }
        return pvt.getMangoValue();
    }

    private final T value;
    private final long timestamp;
    private final int dataPointId;

    public PointValueTime(T value, int dataPointId, long timestamp) {
        this.value = value;
        this.dataPointId = dataPointId;
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public T getMangoValue() {
        return value;
    }

    @Override
    public Object getValue() {
        return value.getValue();
    }

    public boolean isAnnotated() {
        return false;
    }

/*
    public float getFloatValue() {
        return value.getFloatValue();
    }

    public double getDoubleValue() {
        return value.getDoubleValue();
    }

    public String getStringValue() {
        return value.getStringValue();
    }

    public int getIntegerValue() {
        return value.getIntegerValue();
    }

    public boolean getBooleanValue() {
        return value.getBooleanValue();
    }
*/
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PointValueTime)) {
            return false;
        }
        PointValueTime that = (PointValueTime) o;
        if (timestamp != that.timestamp) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.value);
        hash = 29 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return MessageFormat.format("PointValueTime( {0} @{1})", value, new Date(timestamp));
    }

    @Override
    public DataType getDataType() {
        return value.getDataType();
    }
    
    @Override
    public int getDataPointId() {
        return dataPointId;
    }
    
}

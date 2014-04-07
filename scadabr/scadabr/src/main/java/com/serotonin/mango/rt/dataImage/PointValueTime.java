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

import java.io.Serializable;
import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonSerializable;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
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
public class PointValueTime implements Serializable, IValueTime,
        JsonSerializable {

    private static final long serialVersionUID = -1;

    public static boolean equalValues(PointValueTime pvt1, PointValueTime pvt2) {
        if (pvt1 == null && pvt2 == null) {
            return true;
        }
        if (pvt1 == null || pvt2 == null) {
            return false;
        }
        return Objects.equals(pvt1.getValue(), pvt2.getValue());
    }

    public static MangoValue getValue(PointValueTime pvt) {
        if (pvt == null) {
            return null;
        }
        return pvt.getValue();
    }

    private final MangoValue value;
    private final long time;

    public PointValueTime(MangoValue value, long time) {
        this.value = value;
        this.time = time;
    }

    public PointValueTime(boolean value, long time) {
        this(new BinaryValue(value), time);
    }

    public PointValueTime(int value, long time) {
        this(new MultistateValue(value), time);
    }

    public PointValueTime(double value, long time) {
        this(new NumericValue(value), time);
    }

    public PointValueTime(String value, long time) {
        this(new AlphanumericValue(value), time);
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public MangoValue getValue() {
        return value;
    }

    public boolean isAnnotated() {
        return false;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PointValueTime)) {
            return false;
        }
        PointValueTime that = (PointValueTime) o;
        if (time != that.time) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.value);
        hash = 29 * hash + (int) (this.time ^ (this.time >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return MessageFormat.format("PointValueTime( {0} @{1})", value, new Date(time));
    }

    @Override
    public void jsonDeserialize(JsonReader arg0, JsonObject arg1)
            throws JsonException {
        System.out.println("POINT VALUES DESERIALIZE");

    }

    @Override
    public void jsonSerialize(Map<String, Object> arg0) {
        System.out.println("POINT VALUES SERIALIZE");

    }
}

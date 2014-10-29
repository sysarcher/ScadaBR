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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.view.text.MultistateRenderer;
import com.serotonin.mango.view.text.MultistateValue;
import com.serotonin.mango.view.text.TextRenderer;
import br.org.scadabr.i18n.LocalizableException;

/**
 * @author Matthew Lohbihler
 */
public class DataSourceUtils {

    public static MangoValue getValue(Pattern valuePattern, String data, DataType dataType, String binary0Value,
            TextRenderer textRenderer, DecimalFormat valueFormat, String pointName) throws LocalizableException {
        if (data == null) {
            throw new LocalizableException("event.valueParse.noData", pointName);
        }

        Matcher matcher = valuePattern.matcher(data);
        if (matcher.find()) {
            String valueStr = matcher.group(1);
            if (valueStr == null) {
                valueStr = "";
            }

            return getValue(valueStr, dataType, binary0Value, textRenderer, valueFormat, pointName);
        }

        throw new NoMatchException("event.valueParse.noValue", pointName);
    }

    public static long getValueTime(long time, Pattern timePattern, String data, DateFormat timeFormat, String pointName)
            throws LocalizableException {
        if (data == null) {
            throw new LocalizableException("event.valueParse.noData", pointName);
        }

        // Get the time.
        long valueTime = time;
        if (timePattern != null) {
            Matcher matcher = timePattern.matcher(data);
            if (matcher.find()) {
                String timeStr = matcher.group(1);
                try {
                    valueTime = timeFormat.parse(timeStr).getTime();
                } catch (ParseException e) {
                    if (pointName == null) {
                        throw new LocalizableException("event.valueParse.timeParse", timeStr);
                    }
                    throw new LocalizableException("event.valueParse.timeParsePoint", timeStr, pointName);
                }
            } else {
                throw new LocalizableException("event.valueParse.noTime", pointName);
            }
        }

        return valueTime;
    }

    public static MangoValue getValue(String valueStr, DataType dataType, String binary0Value, TextRenderer textRenderer,
            DecimalFormat valueFormat, String pointName) throws LocalizableException {

        switch (dataType) {
            case ALPHANUMERIC:
                return new AlphanumericValue(valueStr);
            case BINARY:
                return new BinaryValue(!valueStr.equals(binary0Value));
            case MULTISTATE:
                if (textRenderer instanceof MultistateRenderer) {
                    List<MultistateValue> multistateValues = ((MultistateRenderer) textRenderer).getMultistateValues();
                    for (MultistateValue multistateValue : multistateValues) {
                        if (multistateValue.getText().equalsIgnoreCase(valueStr)) {
                            return new com.serotonin.mango.rt.dataImage.types.MultistateValue(multistateValue.getKey());
                        }
                    }
                }

                try {
                    return com.serotonin.mango.rt.dataImage.types.MultistateValue.parseMultistate(valueStr);
                } catch (NumberFormatException e) {
                    if (pointName == null) {
                        throw new LocalizableException("event.valueParse.textParse", valueStr);
                    }
                    throw new LocalizableException("event.valueParse.textParsePoint", valueStr, pointName);
                }
            case NUMERIC:
                try {
                    if (valueFormat != null) {
                        return new NumericValue(valueFormat.parse(valueStr).doubleValue());
                    }
                    return NumericValue.parseNumeric(valueStr);
                } catch (NumberFormatException e) {
                    if (pointName == null) {
                        throw new LocalizableException("event.valueParse.numericParse", valueStr);
                    }
                    throw new LocalizableException("event.valueParse.numericParsePoint", valueStr, pointName);
                } catch (ParseException e) {
                    if (pointName == null) {
                        throw new LocalizableException("event.valueParse.generalParse", e.getMessage(), valueStr);
                    }
                    throw new LocalizableException("event.valueParse.generalParsePoint", e.getMessage(), valueStr, pointName);
                }
            default:
                return null;
        }
    }
}

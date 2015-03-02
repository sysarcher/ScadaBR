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
package com.serotonin.mango.vo;

import br.org.scadabr.DataType;
import java.io.Serializable;
import java.util.List;

import br.org.scadabr.ScadaBrConstants;
import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.utils.ImplementMeException;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.event.DoublePointEventDetectorVO;
import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.vo.datasource.PointLocatorVO;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.serialization.SerializabeField;
import br.org.scadabr.vo.IntervalLoggingTypes;
import br.org.scadabr.vo.LoggingTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class DataPointVO<T extends PointValueTime> implements Serializable, Cloneable, ChangeComparable<DataPointVO<T>> {

    public static DataPointVO create(DataType dataType) {
        switch (dataType) {
            case DOUBLE:
                return new DoubleDataPointVO();
            default:
                throw new ImplementMeException();
        }
    }

    public DataPointVO() {
    }
    
    public abstract DataPointRT<T> createRT(PointLocatorVO<T> pointLocatorVO);

    /**
     * @return the valuePattern
     */
    public String getValuePattern() {
        return valuePattern;
    }

    /**
     * @return the valuePattern for the specific PointValueTime
     */
    public String getValuePattern(T pvt) {
        return valuePattern;
    }

    /**
     * @param valuePattern the valuePattern to set
     */
    public void setValuePattern(String valuePattern) {
        this.valuePattern = valuePattern;
    }

    /**
     * @return the valueAndUnitPattern
     */
    public String getValueAndUnitPattern() {
        return valueAndUnitPattern;
    }

    /**
     * @param pvt
     * @return the valueAndUnitPattern for the specific PointValueTime. The
     * first param ('{0}') is the value, the second ('{1}') is the unit
     */
    public String getValueAndUnitPattern(T pvt) {
        return valueAndUnitPattern;
    }

    /**
     * @param valueAndUnitPattern the valueAndUnitPattern to set
     */
    public void setValueAndUnitPattern(String valueAndUnitPattern) {
        this.valueAndUnitPattern = valueAndUnitPattern;
    }

    /**
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * @return the pointLocatorId
     */
    public Integer getPointLocatorId() {
        return pointLocatorId;
    }

    /**
     * @param pointLocatorId the pointLocatorId to set
     */
    public void setPointLocatorId(Integer pointLocatorId) {
        this.pointLocatorId = pointLocatorId;
    }

    public boolean isEvtDetectorsEmpty() {
        return eventDetectors == null ? true : eventDetectors.isEmpty();
    }

    @Configurable
    public static class DataPointVoValidator implements Validator {

        @Autowired
        private DataPointDao dataPointDao;

        @Override
        public boolean supports(Class<?> clazz) {
            return DataPointVO.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            final DataPointVO vo = (DataPointVO) target;
            if (vo.xid.isEmpty()) {
                errors.rejectValue("xid", "validate.required");
            } else if (vo.xid.length() > 50) {
                errors.rejectValue("xid", "validate.notLongerThan", new Object[]{50}, "validate.notLongerThan");
            } else if (!dataPointDao.isXidUnique(vo.xid, vo.id)) {
                errors.rejectValue("xid", "validate.xidUsed");
            }

            if (vo.name.isEmpty()) {
                errors.rejectValue("name", "validate.required");
            }

            if (vo.intervalLoggingPeriod <= 0) {
                errors.rejectValue("intervalLoggingPeriod", "validate.greaterThanZero");
            }

            if (vo.purgePeriod <= 0) {
                errors.rejectValue("purgePeriod", "validate.greaterThanZero");
            }

        }

    }

    public static final String XID_PREFIX = "DP_";

    public abstract DataType getDataType();

    public static final Set<TimePeriods> PURGE_TYPES = EnumSet.of(TimePeriods.DAYS, TimePeriods.WEEKS, TimePeriods.MONTHS, TimePeriods.YEARS);

    @JsonIgnore
    public boolean isNew() {
        return id == ScadaBrConstants.NEW_ID;
    }

    //
    //
    // Properties
    //
    private int id = ScadaBrConstants.NEW_ID;
    private String xid;

    private String name = getClass().getSimpleName();

    private int pointFolderId;
    @SerializabeField
    private LoggingTypes loggingType = LoggingTypes.ALL;
    @SerializabeField
    private TimePeriods intervalLoggingPeriodType = TimePeriods.MINUTES;

    @SerializabeField
    private int intervalLoggingPeriod = 15;
    @SerializabeField
    private IntervalLoggingTypes intervalLoggingType = IntervalLoggingTypes.INSTANT;

    private TimePeriods _purgeType = TimePeriods.YEARS;

    private int purgePeriod = 1;
    private List<DoublePointEventDetectorVO> eventDetectors;
    private List<UserComment> comments;

    private Integer pointLocatorId;

    private String valuePattern;
    private String valueAndUnitPattern;
    private String unit;

    //
    //
    // Runtime data
    //
    /*
     * This is used by the watch list and graphic views to cache the last known value for a point to determine if the
     * browser side needs to be refreshed. Initially set to this value so that point views will update (since null
     * values in this case do in fact equal each other).
     */
    //TODO use null ...
    private T lastValue;

    public DataPointVO(String valuePattern, String valueAndUnitPattern) {
        this.valuePattern = valuePattern;
        this.valueAndUnitPattern = valueAndUnitPattern;
    }

    public void resetLastValue() {
        lastValue = null;
    }

    public PointValueTime lastValue() {
        return lastValue;
    }

    public void updateLastValue(T pvt) {
        lastValue = pvt;
    }

    /*
     * This value is used by the watchlists. It is set when the watchlist is loaded to determine if the user is allowed
     * to set the point or not based upon various conditions.
     */
    private boolean settable;

    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    @Override
    public String getTypeKey() {
        return "event.audit.dataPoint";
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "dsEdit.points.name", name);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.type", loggingType);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.period", intervalLoggingPeriodType.getPeriodDescription(intervalLoggingPeriod));
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.valueType", intervalLoggingType);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.purge", _purgeType.getPeriodDescription(purgePeriod));
        AuditEventType.addPropertyMessage(list, "pointEdit.unit", unit);
        AuditEventType.addPropertyMessage(list, "pointEdit.valuePattern", valuePattern);
        AuditEventType.addPropertyMessage(list, "pointEdit.valueAndUnitPattern", valueAndUnitPattern);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, DataPointVO<T> from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.points.name", from.name, name);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.type", from.loggingType, loggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.period",
                from.intervalLoggingPeriodType.getPeriod(from.intervalLoggingPeriod),
                intervalLoggingPeriodType.getPeriod(intervalLoggingPeriod));
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.valueType", from.intervalLoggingType, intervalLoggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.purge", from._purgeType.getPeriodDescription(from.purgePeriod), _purgeType.getPeriodDescription(purgePeriod));

        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.unit", from.unit, unit);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.valuePattern", from.valuePattern, valuePattern);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.valueAndUnitPattern", from.valuePattern, valuePattern);
    }

    public int getPointFolderId() {
        return pointFolderId;
    }

    public void setPointFolderId(int pointFolderId) {
        this.pointFolderId = pointFolderId;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LoggingTypes getLoggingType() {
        return loggingType;
    }

    public void setLoggingType(LoggingTypes loggingType) {
        this.loggingType = loggingType;
    }

    public int getPurgePeriod() {
        return purgePeriod;
    }

    public void setPurgePeriod(int purgePeriod) {
        this.purgePeriod = purgePeriod;
    }

    public TimePeriods getPurgeType() {
        return _purgeType;
    }

    public void setPurgeType(TimePeriods purgeType) {
        this._purgeType = purgeType;
    }

    public List<DoublePointEventDetectorVO> getEventDetectors() {
        return eventDetectors;
    }

    public void setEventDetectors(List<DoublePointEventDetectorVO> eventDetectors) {
        this.eventDetectors = eventDetectors;
    }

    public List<UserComment> getComments() {
        return comments;
    }

    public void setComments(List<UserComment> comments) {
        this.comments = comments;
    }

    public TimePeriods getIntervalLoggingPeriodType() {
        return intervalLoggingPeriodType;
    }

    public void setIntervalLoggingPeriodType(TimePeriods intervalLoggingPeriodType) {
        this.intervalLoggingPeriodType = intervalLoggingPeriodType;
    }

    public int getIntervalLoggingPeriod() {
        return intervalLoggingPeriod;
    }

    public void setIntervalLoggingPeriod(int intervalLoggingPeriod) {
        this.intervalLoggingPeriod = intervalLoggingPeriod;
    }

    public IntervalLoggingTypes getIntervalLoggingType() {
        return intervalLoggingType;
    }

    public void setIntervalLoggingType(IntervalLoggingTypes intervalLoggingType) {
        this.intervalLoggingType = intervalLoggingType;
    }

    public DataPointVO copy() {
        try {
            return (DataPointVO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    @Override
    public String toString() {
        return "DataPointVO{" + "id=" + id + ", xid=" + xid + ", name=" + name + ", pointFolderId=" + pointFolderId + ", loggingType=" + loggingType + ", intervalLoggingPeriodType=" + intervalLoggingPeriodType + ", intervalLoggingPeriod=" + intervalLoggingPeriod + ", intervalLoggingType=" + intervalLoggingType + ", _purgeType=" + _purgeType + ", purgePeriod=" + purgePeriod + ", eventDetectors=" + eventDetectors + ", comments=" + comments + ", pointLocatorId=" + pointLocatorId + ", valuePattern=" + valuePattern + ", valueAndUnitPattern=" + valueAndUnitPattern + ", unit=" + unit + ", lastValue=" + lastValue + ", settable=" + settable + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.name);
        hash = 13 * hash + this.pointFolderId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataPointVO<?> other = (DataPointVO<?>) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.pointFolderId != other.pointFolderId) {
            return false;
        }
        return true;
    }

}

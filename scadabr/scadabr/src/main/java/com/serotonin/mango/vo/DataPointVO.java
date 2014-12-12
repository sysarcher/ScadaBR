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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import br.org.scadabr.ScadaBrConstants;
import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.dao.DataPointDao;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.event.DoublePointEventDetectorVO;
import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.vo.dataSource.PointLocatorVO;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.vo.IntervalLoggingTypes;
import br.org.scadabr.vo.LoggingTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class DataPointVO<T extends PointValueTime> implements Serializable, Cloneable, ChangeComparable<DataPointVO<T>> {

    public abstract <T extends PointValueTime> DataPointRT<T> createRT();

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
     * @return the valueAndUnitPattern for the specific PointValueTime.
     * The first param ('{0}') is the value, the second ('{1}') is the unit
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

    private static final long serialVersionUID = -1;
    public static final String XID_PREFIX = "DP_";
    

    public DataType getDataType() {
        return pointLocator.getDataType();
    }

    public static final Set<TimePeriods> PURGE_TYPES = EnumSet.of(TimePeriods.DAYS, TimePeriods.WEEKS, TimePeriods.MONTHS, TimePeriods.YEARS);

    public LocalizableMessage getConfigurationDescription() {
        return pointLocator.getConfigurationDescription();
    }

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

    private String name;
    private int dataSourceId;

    private String deviceName;

    private boolean enabled;
    private int pointFolderId;
    private LoggingTypes loggingType = LoggingTypes.ALL;
    private TimePeriods intervalLoggingPeriodType = TimePeriods.MINUTES;

    private int intervalLoggingPeriod = 15;
    private IntervalLoggingTypes intervalLoggingType = IntervalLoggingTypes.INSTANT;

    private TimePeriods _purgeType = TimePeriods.YEARS;

    private int purgePeriod = 1;
    private List<DoublePointEventDetectorVO> eventDetectors;
    private List<UserComment> comments;

    private PointLocatorVO<T> pointLocator;
    
    private String valuePattern;
    private String valueAndUnitPattern;
    private String unit;
    

    //
    //
    // Convenience data from data source
    //
    private String dataSourceName;

    //
    //
    // Required for importing
    //
    private String dataSourceXid;

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

    @Deprecated //TODO Make name with hirearchy path
    public String getExtendedName() {
        return deviceName + " - " + name;
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
        AuditEventType.addPropertyMessage(list, "common.enabled", enabled);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.type", loggingType);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.period", intervalLoggingPeriodType.getPeriodDescription(intervalLoggingPeriod));
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.valueType", intervalLoggingType);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.purge", _purgeType.getPeriodDescription(purgePeriod));
        AuditEventType.addPropertyMessage(list, "pointEdit.unit", unit);
        AuditEventType.addPropertyMessage(list, "pointEdit.valuePattern", valuePattern);
        AuditEventType.addPropertyMessage(list, "pointEdit.valueAndUnitPattern", valueAndUnitPattern);
        
        pointLocator.addProperties(list);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, DataPointVO<T> from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.points.name", from.name, name);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.enabled", from.enabled, enabled);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.type", from.loggingType, loggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.period",
                from.intervalLoggingPeriodType.getPeriod(from.intervalLoggingPeriod),
                intervalLoggingPeriodType.getPeriod(intervalLoggingPeriod));
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.valueType", from.intervalLoggingType, intervalLoggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.purge", from._purgeType.getPeriodDescription(from.purgePeriod), _purgeType.getPeriodDescription(purgePeriod));

        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.unit", from.unit, unit);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.valuePattern", from.valuePattern, valuePattern);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.valueAndUnitPattern", from.valuePattern, valuePattern);

        pointLocator.addPropertyChanges(list, from.pointLocator);
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.pointLocator.setEnabled(value);
        this.enabled = value;
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

    public <PL extends PointLocatorVO<T>> PL getPointLocator() {
        return (PL) pointLocator;
    }

    public void setPointLocator(PointLocatorVO<T> pointLocator) {
        this.pointLocator = pointLocator;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        if (deviceName == null) {
            deviceName = dataSourceName;
        }
    }

    public String getDataSourceXid() {
        return dataSourceXid;
    }

    public void setDataSourceXid(String dataSourceXid) {
        this.dataSourceXid = dataSourceXid;
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
        return "DataPointVO [id=" + id + ", xid=" + xid + ", name=" + name + ", dataSourceId=" + dataSourceId
                + ", deviceName=" + deviceName + ", enabled=" + enabled + ", pointFolderId=" + pointFolderId
                + ", loggingType=" + loggingType + ", intervalLoggingPeriodType=" + intervalLoggingPeriodType
                + ", intervalLoggingPeriod=" + intervalLoggingPeriod + ", intervalLoggingType=" + intervalLoggingType
                + ", purgeType=" + _purgeType + ", purgePeriod=" + purgePeriod
                + ", eventDetectors=" + eventDetectors + ", comments=" + comments
                + ", pointLocator=" + pointLocator
                + ", dataSourceName=" + dataSourceName + ", dataSourceXid=" + dataSourceXid
                + ", lastValue=" + lastValue + ", settable=" + settable + "]";
    }

    //
    //
    // Serialization
    //
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeObject(name);
        out.writeObject(deviceName);
        out.writeBoolean(enabled);
        out.writeInt(pointFolderId);
        out.writeObject(loggingType);
        out.writeObject(intervalLoggingPeriodType);
        out.writeInt(intervalLoggingPeriod);
        out.writeObject(intervalLoggingType);
        out.writeObject(_purgeType);
        out.writeInt(purgePeriod);
        out.writeObject(unit);
        out.writeObject(valuePattern);
        out.writeObject(valueAndUnitPattern);

        out.writeObject(pointLocator);
        
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        switch (ver) {
            case 1:
                name = (String) in.readObject();
                deviceName = (String) in.readObject();
                enabled = in.readBoolean();
                pointFolderId = in.readInt();
                loggingType = (LoggingTypes) (in.readObject());
                intervalLoggingPeriodType = (TimePeriods) in.readObject();
                intervalLoggingPeriod = in.readInt();
                intervalLoggingType = (IntervalLoggingTypes) in.readObject();
                _purgeType = (TimePeriods) in.readObject();
                purgePeriod = in.readInt();
                unit = (String) in.readObject();
                valuePattern = (String) in.readObject();
                valueAndUnitPattern = (String) in.readObject();
                
                pointLocator = (PointLocatorVO) in.readObject();
        }
    }

}

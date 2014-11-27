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
import br.org.scadabr.InvalidArgumentException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import br.org.scadabr.ScadaBrConstants;
import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.util.ColorUtils;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.view.chart.ChartRenderer;
import com.serotonin.mango.view.text.NoneRenderer;
import com.serotonin.mango.view.text.PlainRenderer;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import br.org.scadabr.util.SerializationHelper;
import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.vo.dataSource.PointLocatorVO;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.vo.IntervalLoggingTypes;
import br.org.scadabr.vo.LoggingTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DataPointVO implements Serializable, Cloneable, ChangeComparable<DataPointVO> {

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

            if (vo.loggingType == LoggingTypes.ON_CHANGE && vo.getDataType() == DataType.NUMERIC) {
                if (vo.tolerance < 0) {
                    errors.rejectValue("tolerance", "validate.cannotBeNegative");
                }
            }

            if (vo.intervalLoggingPeriod <= 0) {
                errors.rejectValue("intervalLoggingPeriod", "validate.greaterThanZero");
            }

            if (vo.purgePeriod <= 0) {
                errors.rejectValue("purgePeriod", "validate.greaterThanZero");
            }

            if (vo.textRenderer == null) {
                errors.rejectValue("textRenderer", "validate.required");
            }

            if (!vo.chartColour.isEmpty()) {
                try {
                    ColorUtils.toColor(vo.chartColour);
                } catch (InvalidArgumentException e) {
                    errors.rejectValue("chartColour", "validate.invalidValue");
                }
            }

            // Check text renderer type
            if (vo.textRenderer != null && !vo.textRenderer.getDef().supports(vo.pointLocator.getDataType())) {
                errors.rejectValue("textRenderer", "validate.text.incompatible");
            }

            // Check chart renderer type
            if (vo.chartRenderer != null && !vo.chartRenderer.getType().supports(vo.pointLocator.getDataType())) {
                errors.rejectValue("chartRenderer", "validate.chart.incompatible");
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
    private LoggingTypes loggingType = LoggingTypes.ON_CHANGE;
    private TimePeriods intervalLoggingPeriodType = TimePeriods.MINUTES;

    private int intervalLoggingPeriod = 15;
    private IntervalLoggingTypes intervalLoggingType = IntervalLoggingTypes.INSTANT;

    private double tolerance = 0;
    private TimePeriods _purgeType = TimePeriods.YEARS;

    private int purgePeriod = 1;
    //TODO SingleValueRendererSettings
    @Deprecated
    private TextRenderer textRenderer;
//TODO    (typeFactory = BaseChartRenderer.Factory.class)
    //TODO Rename multipleValueRenderSettings
    @Deprecated
    private ChartRenderer chartRenderer;
    private List<PointEventDetectorVO> eventDetectors;
    private List<UserComment> comments;
    
    @Deprecated // TODO move to ChartSetting
    private String chartColour;

    private PointLocatorVO pointLocator;

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
    private PointValueTime lastValue = new PointValueTime((MangoValue) null, -1);

    public void resetLastValue() {
        lastValue = new PointValueTime((MangoValue) null, -1);
    }

    public PointValueTime lastValue() {
        return lastValue;
    }

    public void updateLastValue(PointValueTime pvt) {
        lastValue = pvt;
    }

    @Deprecated //TODO Make name with hirearchy path
    public String getExtendedName() {
        return deviceName + " - " + name;
    }

    @Deprecated
    public void defaultTextRenderer() {
        if (pointLocator == null) {
            textRenderer = new PlainRenderer("");
        } else {
            switch (pointLocator.getDataType()) {
                case IMAGE:
                    textRenderer = new NoneRenderer();
                    break;
                default:
                    textRenderer = new PlainRenderer("");
            }
        }
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
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.tolerance", tolerance);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.purge", _purgeType.getPeriodDescription(purgePeriod));
        AuditEventType.addPropertyMessage(list, "pointEdit.props.chartColour", chartColour);

        pointLocator.addProperties(list);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, DataPointVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.points.name", from.name, name);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.enabled", from.enabled, enabled);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.type", from.loggingType, loggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.period",
                from.intervalLoggingPeriodType.getPeriod(from.intervalLoggingPeriod),
                intervalLoggingPeriodType.getPeriod(intervalLoggingPeriod));
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.valueType", from.intervalLoggingType, intervalLoggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.tolerance", from.tolerance, tolerance);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.purge", from._purgeType.getPeriodDescription(from.purgePeriod), _purgeType.getPeriodDescription(purgePeriod));
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.props.chartColour", from.chartColour, chartColour);

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

    @SuppressWarnings("unchecked")
    public <T extends PointLocatorVO> T getPointLocator() {
        return (T) pointLocator;
    }

    public void setPointLocator(PointLocatorVO pointLocator) {
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

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    //TODO use MessageFormat pattern for this ???
    @Deprecated
    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    @Deprecated
    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    @Deprecated
    public ChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    @Deprecated
    public void setChartRenderer(ChartRenderer chartRenderer) {
        this.chartRenderer = chartRenderer;
    }

    public List<PointEventDetectorVO> getEventDetectors() {
        return eventDetectors;
    }

    public void setEventDetectors(List<PointEventDetectorVO> eventDetectors) {
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

    @Deprecated
    public String getChartColour() {
        return chartColour;
    }

    @Deprecated
    public void setChartColour(String chartColour) {
        this.chartColour = chartColour;
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
                + ", tolerance=" + tolerance + ", purgeType=" + _purgeType + ", purgePeriod=" + purgePeriod
                + ", textRenderer=" + textRenderer + ", chartRenderer=" + chartRenderer
                + ", eventDetectors=" + eventDetectors + ", comments=" + comments
                + ", chartColour=" + chartColour + ", pointLocator=" + pointLocator
                + ", dataSourceName=" + dataSourceName + ", dataSourceXid=" + dataSourceXid
                + ", lastValue=" + lastValue + ", settable=" + settable + "]";
    }

    //
    //
    // Serialization
    //
    private static final int version = 8;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, name);
        SerializationHelper.writeSafeUTF(out, deviceName);
        out.writeBoolean(enabled);
        out.writeInt(pointFolderId);
        out.writeInt(loggingType.mangoDbId);
        out.writeInt(intervalLoggingPeriodType.getId());
        out.writeInt(intervalLoggingPeriod);
        out.writeInt(intervalLoggingType.getId());
        out.writeDouble(tolerance);
        out.writeInt(_purgeType.getId());
        out.writeInt(purgePeriod);
        out.writeObject(textRenderer);
        out.writeObject(chartRenderer);
        out.writeObject(pointLocator);
        out.writeInt(0);
        out.writeBoolean(false); //discardExtremeValues);
        out.writeDouble(-Double.MAX_VALUE); //discardLowLimit);
        out.writeDouble(Double.MAX_VALUE); //discardHighLimit);
        out.writeInt(0); //engineeringUnits);
        SerializationHelper.writeSafeUTF(out, chartColour);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = 0;
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.MINUTES;
            intervalLoggingPeriod = 15;
            intervalLoggingType = IntervalLoggingTypes.INSTANT;
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
//            defaultCacheSize = 0;
//            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        } else if (ver == 2) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.MINUTES;
            intervalLoggingPeriod = 15;
            intervalLoggingType = IntervalLoggingTypes.INSTANT;
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();

            // The spinwave changes were not correctly implemented, so we need to handle potential errors here.
            try {
                pointLocator = (PointLocatorVO) in.readObject();
            } catch (IOException e) {
                // Turn this guy off.
                enabled = false;
            }
//            defaultCacheSize = 0;
//            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        } else if (ver == 3) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.MINUTES;
            intervalLoggingPeriod = 15;
            intervalLoggingType = IntervalLoggingTypes.INSTANT;
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();

            // The spinwave changes were not correctly implemented, so we need to handle potential errors here.
            try {
                pointLocator = (PointLocatorVO) in.readObject();
            } catch (IOException e) {
                // Turn this guy off.
                enabled = false;
            }
//            defaultCacheSize = in.readInt();
//            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        } else if (ver == 4) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.fromId(in.readInt());
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = IntervalLoggingTypes.fromId(in.readInt());
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();

            // The spinwave changes were not correctly implemented, so we need to handle potential errors here.
            try {
                pointLocator = (PointLocatorVO) in.readObject();
            } catch (IOException e) {
                // Turn this guy off.
                enabled = false;
            }
//            defaultCacheSize = in.readInt();
//            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        } else if (ver == 5) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.fromId(in.readInt());
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = IntervalLoggingTypes.fromId(in.readInt());
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            //defaultCacheSize = 
            in.readInt();
            //discardExtremeValues = 
            in.readBoolean();
            //discardLowLimit = 
            in.readDouble();
            //discardHighLimit = 
            in.readDouble();
            //engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        } else if (ver == 6) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.fromId(in.readInt());
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = IntervalLoggingTypes.fromId(in.readInt());
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            //defaultCacheSize = 
            in.readInt();
            //discardExtremeValues = 
            in.readBoolean();
            //discardLowLimit = 
            in.readDouble();
            //discardHighLimit = 
            in.readDouble();
            //engineeringUnits = 
            in.readInt();
            chartColour = null;
        } else if (ver == 7) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.fromId(in.readInt());
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = IntervalLoggingTypes.fromId(in.readInt());
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            //defaultCacheSize = 
            in.readInt();
            //discardExtremeValues = 
            in.readBoolean();
            //discardLowLimit = 
            in.readDouble();
            //discardHighLimit = 
            in.readDouble();
            //engineeringUnits = 
            in.readInt();
            chartColour = SerializationHelper.readSafeUTF(in);
        } else if (ver == 8) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = SerializationHelper.readSafeUTF(in);
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = LoggingTypes.fromMangoDbId(in.readInt());
            intervalLoggingPeriodType = TimePeriods.fromId(in.readInt());
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = IntervalLoggingTypes.fromId(in.readInt());
            tolerance = in.readDouble();
            _purgeType = TimePeriods.fromId(in.readInt());
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            //defaultCacheSize = 
            in.readInt();
            //discardExtremeValues = 
            in.readBoolean();
            //discardLowLimit = 
            in.readDouble();
            //discardHighLimit = 
            in.readDouble();
            //engineeringUnits = 
            in.readInt();
            chartColour = SerializationHelper.readSafeUTF(in);
        }

        // Check the purge type. Weird how this could have been set to 0.
        if (_purgeType == null) {
            _purgeType = TimePeriods.YEARS;
        }
        // Ditto for purge period
        if (purgePeriod == 0) {
            purgePeriod = 1;
        }
    }

}

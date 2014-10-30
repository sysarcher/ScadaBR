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
package com.serotonin.mango.vo.dataSource.sql;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.sql.SqlDataSourceRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import br.org.scadabr.util.SerializationHelper;
import br.org.scadabr.util.StringUtils;
import br.org.scadabr.utils.TimePeriods;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class SqlDataSourceVO extends DataSourceVO<SqlDataSourceVO> {

    public static final Type TYPE = Type.SQL;

    @Override
    protected void addEventTypes(List<EventTypeVO> ets) {
        ets.add(createEventType(SqlDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT,
                new LocalizableMessageImpl("event.ds.dataSource")));
        ets.add(createEventType(SqlDataSourceRT.STATEMENT_EXCEPTION_EVENT,
                new LocalizableMessageImpl("event.ds.statement")));
    }

    private static final ExportCodes EVENT_CODES = new ExportCodes();

    static {
        EVENT_CODES.addElement(SqlDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT,
                "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(SqlDataSourceRT.STATEMENT_EXCEPTION_EVENT,
                "STATEMENT_EXCEPTION");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public LocalizableMessage getConnectionDescription() {
        return new LocalizableMessageImpl("common.default", connectionUrl);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public DataSourceRT createDataSourceRT() {
        return new SqlDataSourceRT(this);
    }

    @Override
    public SqlPointLocatorVO createPointLocator() {
        return new SqlPointLocatorVO();
    }

    @JsonRemoteProperty
    private String driverClassname;
    @JsonRemoteProperty
    private String connectionUrl;
    @JsonRemoteProperty
    private String username;
    @JsonRemoteProperty
    private String password;
    @JsonRemoteProperty
    private String selectStatement;
    private TimePeriods updatePeriodType = TimePeriods.MINUTES;
    @JsonRemoteProperty
    private int updatePeriods = 5;
    @JsonRemoteProperty
    private boolean rowBasedQuery = false;

    public String getDriverClassname() {
        return driverClassname;
    }

    public void setDriverClassname(String driverClassname) {
        this.driverClassname = driverClassname;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public int getUpdatePeriods() {
        return updatePeriods;
    }

    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    public TimePeriods getUpdatePeriodType() {
        return updatePeriodType;
    }

    public void setUpdatePeriodType(TimePeriods updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSelectStatement() {
        return selectStatement;
    }

    public void setSelectStatement(String selectStatement) {
        this.selectStatement = selectStatement;
    }

    public boolean isRowBasedQuery() {
        return rowBasedQuery;
    }

    public void setRowBasedQuery(boolean rowBasedQuery) {
        this.rowBasedQuery = rowBasedQuery;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        super.validate(response);
        if (updatePeriods <= 0) {
            response.addContextual("updatePeriods", "validate.greaterThanZero");
        }
        if (driverClassname.isEmpty()) {
            response.addContextual("driverClassname", "validate.required");
        }
        if (connectionUrl.isEmpty()) {
            response.addContextual("connectionUrl", "validate.required");
        }
    }

    @Override
    protected void addPropertiesImpl(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.updatePeriod", updatePeriodType.getPeriodDescription(updatePeriods));
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.driverClassName", driverClassname);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.connectionString", connectionUrl);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.username", username);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.password", password);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.select", selectStatement);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.rowQuery", rowBasedQuery);
    }

    @Override
    protected void addPropertyChangesImpl(List<LocalizableMessage> list,
            SqlDataSourceVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.updatePeriod",
                from.updatePeriodType.getPeriodDescription(from.updatePeriods),
                updatePeriodType.getPeriodDescription(updatePeriods));
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.driverClassName", from.driverClassname, driverClassname);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.connectionString", from.connectionUrl, connectionUrl);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.username", from.username, username);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.password", from.password, password);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.select", from.selectStatement, selectStatement);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.rowQuery", from.rowBasedQuery, rowBasedQuery);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, driverClassname);
        SerializationHelper.writeSafeUTF(out, connectionUrl);
        SerializationHelper.writeSafeUTF(out, username);
        SerializationHelper.writeSafeUTF(out, password);
        SerializationHelper.writeSafeUTF(out, selectStatement);
        out.writeInt(updatePeriodType.mangoDbId);
        out.writeInt(updatePeriods);
        out.writeBoolean(rowBasedQuery);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be
        // elegantly handled.
        if (ver == 1) {
            driverClassname = SerializationHelper.readSafeUTF(in);
            connectionUrl = SerializationHelper.readSafeUTF(in);
            username = SerializationHelper.readSafeUTF(in);
            password = SerializationHelper.readSafeUTF(in);
            selectStatement = SerializationHelper.readSafeUTF(in);
            updatePeriodType = TimePeriods.fromMangoDbId(in.readInt());
            updatePeriods = in.readInt();
            rowBasedQuery = false;
        } else if (ver == 2) {
            driverClassname = SerializationHelper.readSafeUTF(in);
            connectionUrl = SerializationHelper.readSafeUTF(in);
            username = SerializationHelper.readSafeUTF(in);
            password = SerializationHelper.readSafeUTF(in);
            selectStatement = SerializationHelper.readSafeUTF(in);
            updatePeriodType = TimePeriods.fromMangoDbId(in.readInt());
            updatePeriods = in.readInt();
            rowBasedQuery = in.readBoolean();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json)
            throws JsonException {
        super.jsonDeserialize(reader, json);
        TimePeriods value = deserializeUpdatePeriodType(json);
        if (value != null) {
            updatePeriodType = value;
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        serializeUpdatePeriodType(map, updatePeriodType);
    }
}

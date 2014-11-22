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
package com.serotonin.mango.vo.link;

import br.org.scadabr.ScadaBrConstants;
import java.util.List;

import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.util.ExportCodes;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Matthew Lohbihler
 */

@Configurable
public class PointLinkVO implements ChangeComparable<PointLinkVO> {

    @Autowired
    private DataPointDao dataPointDao;

    public static final String XID_PREFIX = "PL_";

    public static final int EVENT_UPDATE = 1;
    public static final int EVENT_CHANGE = 2;

    public final static ExportCodes EVENT_CODES = new ExportCodes();

    static {
        EVENT_CODES.addElement(EVENT_UPDATE, "UPDATE", "pointLinks.event.update");
        EVENT_CODES.addElement(EVENT_CHANGE, "CHANGE", "pointLinks.event.change");
    }

    private int id = ScadaBrConstants.NEW_ID;
    private String xid;
    private int sourcePointId;
    private int targetPointId;
    
    private String script;
    private int event;
    
    private boolean disabled;

    @JsonIgnore
    public boolean isNew() {
        return id == ScadaBrConstants.NEW_ID;
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

    public int getSourcePointId() {
        return sourcePointId;
    }

    public void setSourcePointId(int sourcePointId) {
        this.sourcePointId = sourcePointId;
    }

    public int getTargetPointId() {
        return targetPointId;
    }

    public void setTargetPointId(int targetPointId) {
        this.targetPointId = targetPointId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String getTypeKey() {
        return "event.audit.pointLink";
    }

    public void validate(DwrResponseI18n response) {
        if (sourcePointId == 0) {
            response.addContextual("sourcePointId", "pointLinks.validate.sourceRequired");
        }
        if (targetPointId == 0) {
            response.addContextual("targetPointId", "pointLinks.validate.targetRequired");
        }
        if (sourcePointId == targetPointId) {
            response.addContextual("targetPointId", "pointLinks.validate.samePoint");
        }
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "pointLinks.source", dataPointDao.getExtendedPointName(sourcePointId));
        AuditEventType.addPropertyMessage(list, "pointLinks.target", dataPointDao.getExtendedPointName(targetPointId));
        AuditEventType.addPropertyMessage(list, "pointLinks.script", script);
        AuditEventType.addExportCodeMessage(list, "pointLinks.event", EVENT_CODES, event);
        AuditEventType.addPropertyMessage(list, "common.disabled", disabled);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, PointLinkVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType
                .maybeAddPropertyChangeMessage(list, "pointLinks.source",
                        dataPointDao.getExtendedPointName(from.sourcePointId),
                        dataPointDao.getExtendedPointName(sourcePointId));
        AuditEventType
                .maybeAddPropertyChangeMessage(list, "pointLinks.target",
                        dataPointDao.getExtendedPointName(from.targetPointId),
                        dataPointDao.getExtendedPointName(targetPointId));
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointLinks.script", from.script, script);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "pointLinks.event", EVENT_CODES, from.event, event);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.disabled", from.disabled, disabled);
    }

}

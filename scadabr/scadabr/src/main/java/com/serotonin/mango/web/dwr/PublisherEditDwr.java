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
package com.serotonin.mango.web.dwr;

import br.org.scadabr.DataType;
import java.util.Iterator;
import java.util.List;

import br.org.scadabr.db.KeyValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.publish.persistent.PersistentSenderRT;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.publish.PublishedPointVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.mango.vo.publish.httpSender.HttpPointVO;
import com.serotonin.mango.vo.publish.httpSender.HttpSenderVO;
import com.serotonin.mango.vo.publish.pachube.PachubePointVO;
import com.serotonin.mango.vo.publish.pachube.PachubeSenderVO;
import com.serotonin.mango.vo.publish.persistent.PersistentPointVO;
import com.serotonin.mango.vo.publish.persistent.PersistentSenderVO;
import com.serotonin.mango.web.dwr.beans.HttpSenderTester;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.i18n.LocalizableMessageImpl;

/**
 * @author Matthew Lohbihler
 */
public class PublisherEditDwr extends BaseDwr {

    private DwrResponseI18n trySave(PublisherVO<? extends PublishedPointVO> p) {
        DwrResponseI18n response = new DwrResponseI18n();

        p.validate(response);
        if (response.isEmpty()) {
            runtimeManager.savePublisher(p);
            response.addData("id", p.getId());
        }

        return response;
    }

    public void cancelTestingUtility() {
        Common.getUser().cancelTestingUtility();
    }

    public DwrResponseI18n initSender() {
        List<DataPointVO> allPoints = dataPointDao.getDataPoints(DataPointExtendedNameComparator.instance, false);

        // Remove image points
        Iterator<DataPointVO> iter = allPoints.iterator();
        while (iter.hasNext()) {
            DataPointVO dp = iter.next();
            if (dp.getDataType() == DataType.IMAGE) {
                iter.remove();
            }
        }

        DwrResponseI18n response = new DwrResponseI18n();
        response.addData("publisher", Common.getUser().getEditPublisher());
        response.addData("allPoints", allPoints);
        return response;
    }

    //
    //
    // HTTP sender stuff
    //
    public DwrResponseI18n saveHttpSender(String name, String xid, boolean enabled, List<HttpPointVO> points,
            String url, boolean usePost, List<KeyValuePair> staticHeaders, List<KeyValuePair> staticParameters,
            int cacheWarningSize, boolean changesOnly, boolean raiseResultWarning, int dateFormat,
            boolean sendSnapshot, int snapshotSendPeriods, int snapshotSendPeriodType) {
        HttpSenderVO p = (HttpSenderVO) Common.getUser().getEditPublisher();

        p.setName(name);
        p.setXid(xid);
        p.setEnabled(enabled);
        p.setPoints(points);
        p.setUrl(url);
        p.setUsePost(usePost);
        p.setStaticHeaders(staticHeaders);
        p.setStaticParameters(staticParameters);
        p.setCacheWarningSize(cacheWarningSize);
        p.setChangesOnly(changesOnly);
        p.setRaiseResultWarning(raiseResultWarning);
        p.setDateFormat(dateFormat);
        p.setSendSnapshot(sendSnapshot);
        p.setSnapshotSendPeriods(snapshotSendPeriods);
        p.setSnapshotSendPeriodType(snapshotSendPeriodType);

        return trySave(p);
    }

    public void httpSenderTest(String url, boolean usePost, List<KeyValuePair> staticHeaders,
            List<KeyValuePair> staticParameters) {
        Common.getUser().setTestingUtility(new HttpSenderTester(url, usePost, staticHeaders, staticParameters));
    }

    public String httpSenderTestUpdate() {
        HttpSenderTester test = Common.getUser().getTestingUtility(HttpSenderTester.class);
        if (test == null) {
            return null;
        }
        return test.getResult();
    }

    //
    //
    // Pachube sender stuff
    //
    public DwrResponseI18n savePachubeSender(String name, String xid, boolean enabled, List<PachubePointVO> points,
            String apiKey, int timeoutSeconds, int retries, int cacheWarningSize, boolean changesOnly,
            boolean sendSnapshot, int snapshotSendPeriods, int snapshotSendPeriodType) {
        PachubeSenderVO p = (PachubeSenderVO) Common.getUser().getEditPublisher();

        p.setName(name);
        p.setXid(xid);
        p.setEnabled(enabled);
        p.setPoints(points);
        p.setApiKey(apiKey);
        p.setTimeoutSeconds(timeoutSeconds);
        p.setRetries(retries);
        p.setCacheWarningSize(cacheWarningSize);
        p.setChangesOnly(changesOnly);
        p.setSendSnapshot(sendSnapshot);
        p.setSnapshotSendPeriods(snapshotSendPeriods);
        p.setSnapshotSendPeriodType(snapshotSendPeriodType);

        return trySave(p);
    }

    //
    //
    // Persistent sender stuff
    //
    public DwrResponseI18n savePersistentSender(String name, String xid, boolean enabled,
            List<PersistentPointVO> points, String host, int port, String authorizationKey, String xidPrefix,
            int syncType, int cacheWarningSize, boolean changesOnly, boolean sendSnapshot, int snapshotSendPeriods,
            int snapshotSendPeriodType) {
        PersistentSenderVO p = (PersistentSenderVO) Common.getUser().getEditPublisher();

        p.setName(name);
        p.setXid(xid);
        p.setEnabled(enabled);
        p.setPoints(points);
        p.setHost(host);
        p.setPort(port);
        p.setAuthorizationKey(authorizationKey);
        p.setXidPrefix(xidPrefix);
        p.setSyncType(syncType);
        p.setCacheWarningSize(cacheWarningSize);
        p.setChangesOnly(changesOnly);
        p.setSendSnapshot(sendSnapshot);
        p.setSnapshotSendPeriods(snapshotSendPeriods);
        p.setSnapshotSendPeriodType(snapshotSendPeriodType);

        return trySave(p);
    }

    public DwrResponseI18n getPersistentSenderStatus() {
        PersistentSenderVO p = (PersistentSenderVO) Common.getUser().getEditPublisher();
        PersistentSenderRT rt = (PersistentSenderRT) runtimeManager.getRunningPublisher(p.getId());

        DwrResponseI18n response = new DwrResponseI18n();
        if (rt == null) {
            response.addGeneric("publisherEdit.persistent.status.notEnabled");
        } else {
            response.addGeneric("publisherEdit.persistent.status.pointCount", rt.getPointCount());
            response.addGeneric("publisherEdit.persistent.status.queueSize", rt.getQueueSize());
            if (rt.getConnectingIndex() != -1) {
                response.addGeneric("publisherEdit.persistent.status.connectionState", new LocalizableMessageImpl("publisherEdit.persistent.status.connecting", rt.getConnectingIndex(), rt.getPointCount()));
            } else if (rt.isConnected()) {
                response.addGeneric("publisherEdit.persistent.status.connectionState", new LocalizableMessageImpl("publisherEdit.persistent.status.connected"));
            } else {
                response.addGeneric("publisherEdit.persistent.status.connectionState", new LocalizableMessageImpl("publisherEdit.persistent.status.notConnected"));
            }
            response.addGeneric("publisherEdit.persistent.status.packetQueueSize", rt.getPacketsToSend());

            int syncStatus = rt.getSyncStatus();
            if (syncStatus == -1) {
                response.addGeneric("publisherEdit.persistent.status.syncNotRunning");
            } else {
                response.addGeneric("publisherEdit.persistent.status.syncStatus", syncStatus, rt.getPointCount(), rt.getSyncRequestsSent());
            }
        }

        return response;
    }

    public DwrResponseI18n startPersistentSync() {
        PersistentSenderVO p = (PersistentSenderVO) Common.getUser().getEditPublisher();
        PersistentSenderRT rt = (PersistentSenderRT) runtimeManager.getRunningPublisher(p.getId());

        DwrResponseI18n response = new DwrResponseI18n();
        if (rt == null) {
            response.addGeneric("publisherEdit.persistent.status.notEnabled");
        } else if (rt.startSync()) {
            response.addGeneric("publisherEdit.persistent.syncStarted");
        } else {
            response.addGeneric("publisherEdit.persistent.syncNotStarted");
        }

        return response;
    }

}

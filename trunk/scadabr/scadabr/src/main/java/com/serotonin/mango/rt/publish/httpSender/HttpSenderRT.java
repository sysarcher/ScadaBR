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
package com.serotonin.mango.rt.publish.httpSender;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.annotation.Autowired;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.event.type.PublisherEventType;
import com.serotonin.mango.rt.publish.PublishQueue;
import com.serotonin.mango.rt.publish.PublishQueueEntry;
import com.serotonin.mango.rt.publish.PublisherRT;
import com.serotonin.mango.rt.publish.SendThread;
import com.serotonin.mango.vo.publish.httpSender.HttpPointVO;
import com.serotonin.mango.vo.publish.httpSender.HttpSenderVO;
import com.serotonin.mango.web.servlet.HttpDataSourceServlet;
import com.serotonin.util.StringUtils;
import com.serotonin.web.http.HttpUtils;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
public class HttpSenderRT extends PublisherRT<HttpPointVO> {

    @Autowired
    private Common common;
    public static final String USER_AGENT = "Mango M2M HTTP Sender publisher";
    private static final int MAX_FAILURES = 5;
    public static final int SEND_EXCEPTION_EVENT = 11;
    public static final int RESULT_WARNINGS_EVENT = 12;
    final EventType sendExceptionEventType = new PublisherEventType(getId(), SEND_EXCEPTION_EVENT);
    final EventType resultWarningsEventType = new PublisherEventType(getId(), RESULT_WARNINGS_EVENT);
    final HttpSenderVO vo;
    @Autowired
    private EventManager eventManager;

    public HttpSenderRT(HttpSenderVO vo) {
        super(vo);
        this.vo = vo;
    }

    //
    // /
    // / Lifecycle
    // /
    //
    @Override
    public void initialize() {
        super.initialize(new HttpSendThread());
    }

    PublishQueue<HttpPointVO> getPublishQueue() {
        return queue;
    }

    class HttpSendThread extends SendThread {

        private int failureCount = 0;
        private LocalizableMessage failureMessage;

        HttpSendThread() {
            super("HttpSenderRT.SendThread");
        }

        @Override
        protected void runImpl() {
            int max;
            if (vo.isUsePost()) {
                max = 100;
            } else {
                max = 10;
            }

            while (isRunning()) {
                List<PublishQueueEntry<HttpPointVO>> list = getPublishQueue().get(max);

                if (list != null) {
                    if (send(list)) {
                        for (PublishQueueEntry<HttpPointVO> e : list) {
                            getPublishQueue().remove(e);
                        }
                    } else {
                        // The send failed, so take a break so as not to over exert ourselves.
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            // no op
                        }
                    }
                } else {
                    waitImpl(10000);
                }
            }
        }

        @SuppressWarnings("synthetic-access")
        private boolean send(List<PublishQueueEntry<HttpPointVO>> list) {
            // Prepare the message
            NameValuePair[] params = createNVPs(vo.getStaticParameters(), list);

            HttpMethodBase method;
            if (vo.isUsePost()) {
                PostMethod post = new PostMethod(vo.getUrl());
                post.addParameters(params);
                method = post;
            } else {
                GetMethod get = new GetMethod(vo.getUrl());
                get.setQueryString(params);
                method = get;
            }

            // Add a recognizable header
            method.addRequestHeader("User-Agent", USER_AGENT);

            // Add the user-defined headers.
            final Map<String, String> staticHeaders = vo.getStaticHeaders();
            for (String kvp : staticHeaders.keySet()) {
                method.addRequestHeader(kvp, staticHeaders.get(kvp));
            }

            // Send the request. Set message non-null if there is a failure.
            LocalizableMessage message = null;
            try {
                int code = common.getHttpClient().executeMethod(method);
                if (code == HttpStatus.SC_OK) {
                    if (vo.isRaiseResultWarning()) {
                        String result = HttpUtils.readResponseBody(method, 1024);
                        if (!StringUtils.isEmpty(result)) {
                            eventManager.raiseEvent(resultWarningsEventType,
                                    System.currentTimeMillis(), false, AlarmLevels.INFORMATION,
                                    new LocalizableMessage("common.default", result), createEventContext());
                        }
                    }
                } else {
                    message = new LocalizableMessage("event.publish.invalidResponse", code);
                }
            } catch (Exception ex) {
                message = new LocalizableMessage("common.default", ex.getMessage());
            } finally {
                method.releaseConnection();
            }

            // Check for failure.
            if (message != null) {
                failureCount++;
                if (failureMessage == null) {
                    failureMessage = message;
                }

                if (failureCount == MAX_FAILURES + 1) {
                    eventManager.raiseEvent(sendExceptionEventType, System.currentTimeMillis(), true,
                            AlarmLevels.URGENT, failureMessage, createEventContext());
                }

                return false;
            }

            if (failureCount > 0) {
                if (failureCount > MAX_FAILURES) {
                    eventManager.returnToNormal(sendExceptionEventType, System.currentTimeMillis());
                }

                failureCount = 0;
                failureMessage = null;
            }
            return true;
        }
    }

    NameValuePair[] createNVPs(Map<String, String> staticParameters, List<PublishQueueEntry<HttpPointVO>> list) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        for (String kvp : staticParameters.keySet()) {
            nvps.add(new NameValuePair(kvp, staticParameters.get(kvp)));
        }

        for (PublishQueueEntry<HttpPointVO> e : list) {
            HttpPointVO pvo = e.getVo();
            PointValueTime pvt = e.getPvt();

            String value = pvt.getValue().getMangoDataType().name();

            if (pvo.isIncludeTimestamp()) {
                value += "@";

                switch (vo.getDateFormat()) {
                    case HttpSenderVO.DATE_FORMAT_BASIC:
                        value += HttpDataSourceServlet.BASIC_SDF_CACHE.getObject().format(new Date(pvt.getTime()));
                        break;
                    case HttpSenderVO.DATE_FORMAT_TZ:
                        value += HttpDataSourceServlet.TZ_SDF_CACHE.getObject().format(new Date(pvt.getTime()));
                        break;
                    case HttpSenderVO.DATE_FORMAT_UTC:
                        value += Long.toString(pvt.getTime());
                        break;
                    default:
                        throw new ShouldNeverHappenException("Unknown date format type: " + vo.getDateFormat());
                }
            }
            nvps.add(new NameValuePair(pvo.getParameterName(), value));
        }

        return nvps.toArray(new NameValuePair[nvps.size()]);
    }
}

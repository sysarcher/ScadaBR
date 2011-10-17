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
package com.serotonin.mango.web.dwr.beans;


import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.publish.httpSender.HttpSenderRT;
import com.serotonin.web.http.HttpUtils;

/**
 * @author Matthew Lohbihler
 */
public class HttpSenderTester extends Thread implements TestingUtility {

    private final String url;
    private final boolean usePost;
    private final Map<String, String> staticHeaders;
    private final Map<String, String> staticParameters;
    private String result;

    public HttpSenderTester(String url, boolean usePost, Map<String, String> staticHeaders,
            Map<String, String> staticParameters) {
        this.url = url;
        this.usePost = usePost;
        this.staticHeaders = staticHeaders;
        this.staticParameters = staticParameters;
        start();
    }

    @Override
    public void run() {
        HttpMethodBase method;
        if (usePost) {
            PostMethod post = new PostMethod(url);
            post.addParameters(convertToNVPs(staticParameters));
            method = post;
        } else {
            GetMethod get = new GetMethod(url);
            get.setQueryString(convertToNVPs(staticParameters));
            method = get;
        }

        // Add a recognizable header
        method.addRequestHeader("User-Agent", HttpSenderRT.USER_AGENT);

        // Add the user-defined headers.
        for (String kvp : staticHeaders.keySet()) {
            method.addRequestHeader(kvp, staticHeaders.get(kvp));
        }

        try {
            int code = Common.getHttpClient().executeMethod(method);
            if (code != HttpStatus.SC_OK) {
                result = "ERROR: Invalid response code: " + code;
            } else {
                result = HttpUtils.readResponseBody(method, 1024);
            }
        } catch (Exception e) {
            result = "ERROR: " + e.getMessage();
        } finally {
            method.releaseConnection();
        }
    }

    public String getResult() {
        return result;
    }

    private NameValuePair[] convertToNVPs(Map<String, String> staticParameters) {
        NameValuePair[] nvps = new NameValuePair[staticParameters.size()];
        int i = 0;
        for (String key : staticParameters.keySet()) {
            nvps[i++] = new NameValuePair(key, staticParameters.get(key));
        }
        return nvps;
    }

    @Override
    public void cancel() {
        // no op
    }
}

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
package com.serotonin.mango.rt.dataSource.http;

import java.util.concurrent.CopyOnWriteArraySet;

import br.org.scadabr.util.IpAddressUtils;
import br.org.scadabr.util.IpWhiteListException;
import br.org.scadabr.util.StringUtils;

/**
 * @author Matthew Lohbihler
 */
public class HttpReceiverMulticaster {

    private final CopyOnWriteArraySet<HttpMulticastListener> listeners = new CopyOnWriteArraySet<>();

    public void addListener(HttpMulticastListener l) {
        listeners.add(l);
    }

    public void removeListener(HttpMulticastListener l) {
        listeners.remove(l);
    }

    public void multicast(HttpReceiverData data) {
        for (HttpMulticastListener l : listeners) {
            // Check if the listener cares about stuff from this ip address.
            try {
                if (!IpAddressUtils.ipWhiteListCheck(l.getIpWhiteList(), data.getRemoteIp())) {
                    continue;
                }
            } catch (IpWhiteListException e) {
                l.ipWhiteListError(e.getMessage());
                continue;
            }

            // Check if the listener cares about stuff from this device id.
            if (data.getDeviceId() != null && !data.getDeviceId().isEmpty()) {
                if (!StringUtils.globWhiteListMatchIgnoreCase(l.getDeviceIdWhiteList(), data.getDeviceId())) {
                    continue;
                }
            }

            // Everything checks out, so tell the listener about this data.
            l.data(data);
        }
    }
}

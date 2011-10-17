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
package com.serotonin.mango.rt.dataSource.vmstat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataSource.EventDataSource;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatAttributes;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatDataSourceVO;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatPointLocatorVO;
import com.serotonin.web.i18n.LocalizableMessage;
import java.util.EnumMap;

/**
 * @author Matthew Lohbihler
 */
public class VMStatDataSourceRT extends EventDataSource implements Runnable {
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 1;
    public static final int PARSE_EXCEPTION_EVENT = 2;

    private final static Logger LOG = LoggerFactory.getLogger(VMStatDataSourceRT.class);
    private final VMStatDataSourceVO vo;
    private Process vmstatProcess;
    private BufferedReader in;
    private Map<VMStatAttributes, Integer> attributePositions;
    private boolean terminated;

    public VMStatDataSourceRT(VMStatDataSourceVO vo) {
        super(vo, false);
        this.vo = vo;
    }

    //
    // /
    // / Lifecycle
    // /
    //
    @Override
    public void initialize() {
        super.initialize();

        String command = "vmstat -n ";
        switch (vo.getOutputScale()) {
        case VMStatDataSourceVO.OutputScale.LOWER_K:
            command += "-S k ";
            break;
        case VMStatDataSourceVO.OutputScale.UPPER_K:
            command += "-S K ";
            break;
        case VMStatDataSourceVO.OutputScale.LOWER_M:
            command += "-S m ";
            break;
        case VMStatDataSourceVO.OutputScale.UPPER_M:
            command += "-S M ";
            break;
        }

        command += vo.getPollSeconds();

        try {
            vmstatProcess = Runtime.getRuntime().exec(command);

            // Create the input stream readers.
            in = new BufferedReader(new InputStreamReader(vmstatProcess.getInputStream()));

            // Read the first two lines of output. They are the headers.
            in.readLine();
            String headers = in.readLine();

            // Create a mapping of attribute ids to split array positions.
            attributePositions = new EnumMap<VMStatAttributes, Integer>(VMStatAttributes.class);
            String[] headerParts = headers.split("\\s+");
            for (int i = 0; i < headerParts.length; i++) {
                VMStatAttributes attribute = null;
                if ("r".equals(headerParts[i]))
                    attribute = VMStatAttributes.PROCS_R;
                else if ("b".equals(headerParts[i]))
                    attribute = VMStatAttributes.PROCS_B;
                else if ("swpd".equals(headerParts[i]))
                    attribute = VMStatAttributes.MEMORY_SWPD;
                else if ("free".equals(headerParts[i]))
                    attribute = VMStatAttributes.MEMORY_FREE;
                else if ("buff".equals(headerParts[i]))
                    attribute = VMStatAttributes.MEMORY_BUFF;
                else if ("cache".equals(headerParts[i]))
                    attribute = VMStatAttributes.MEMORY_CACHE;
                else if ("si".equals(headerParts[i]))
                    attribute = VMStatAttributes.SWAP_SI;
                else if ("so".equals(headerParts[i]))
                    attribute = VMStatAttributes.SWAP_SO;
                else if ("bi".equals(headerParts[i]))
                    attribute = VMStatAttributes.IO_BI;
                else if ("bo".equals(headerParts[i]))
                    attribute = VMStatAttributes.IO_BO;
                else if ("in".equals(headerParts[i]))
                    attribute = VMStatAttributes.SYSTEM_IN;
                else if ("cs".equals(headerParts[i]))
                    attribute = VMStatAttributes.SYSTEM_CS;
                else if ("us".equals(headerParts[i]))
                    attribute = VMStatAttributes.CPU_US;
                else if ("sy".equals(headerParts[i]))
                    attribute = VMStatAttributes.CPU_SY;
                else if ("id".equals(headerParts[i]))
                    attribute = VMStatAttributes.CPU_ID;
                else if ("wa".equals(headerParts[i]))
                    attribute = VMStatAttributes.CPU_WA;
                else if ("st".equals(headerParts[i]))
                    attribute = VMStatAttributes.CPU_ST;

                if (attribute != null)
                    attributePositions.put(attribute, i);
            }

            // Read the first line of data. This is a summary of beginning of time until now, so it is no good for
            // our purposes. Just throw it away.
            in.readLine();

            returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
        }
        catch (IOException e) {
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new LocalizableMessage(
                    "event.initializationError", e.getMessage()));
        }
    }

    @Override
    public void terminate() {
        super.terminate();

        terminated = true;

        // Stop the process.
        if (vmstatProcess != null)
            vmstatProcess.destroy();
    }

    @Override
    public void beginPolling() {
        if (vmstatProcess != null)
            new Thread(this, "VMStat data source").start();
    }

    public void run() {
        try {
            while (true) {
                String line = in.readLine();

                if (line == null) {
                    if (terminated)
                        break;
                    throw new IOException("no data");
                }

                readParts(line.split("\\s+"));
                readError();
            }
        }
        catch (IOException e) {
            // Assume that the process was ended.
            readError();

            if (!terminated) {
                raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new LocalizableMessage(
                        "event.vmstat.process", e.getMessage()));
            }
        }
    }

    private void readParts(String[] parts) {
        LocalizableMessage error = null;
        long time = System.currentTimeMillis();

        synchronized (enabledDataPoints) {
            for (DataPointRT dp : enabledDataPoints) {
                VMStatPointLocatorVO locator = ((VMStatPointLocatorRT) dp.getPointLocator()).getPointLocatorVO();

                Integer position = attributePositions.get(locator.getAttribute());
                if (position == null) {
                    if (error != null)
                        error = new LocalizableMessage("event.vmstat.attributeNotFound", locator
                                .getConfigurationDescription());
                }
                else {
                    try {
                        String data = parts[position];
                        Double value = new Double(data);
                        dp.updatePointValue(new PointValueTime(value, time));
                    }
                    catch (NumberFormatException e) {
                        LOG.error("Weird. We couldn't parse the value " + parts[position]
                                + " into a double. attribute=" + locator.getAttribute());
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        LOG.error("Weird. We need element " + position + " but the vmstat data is only " + parts.length
                                + " elements long");
                    }
                }
            }
        }

        if (error == null)
            returnToNormal(PARSE_EXCEPTION_EVENT, time);
        else
            raiseEvent(PARSE_EXCEPTION_EVENT, time, true, error);
    }

    private void readError() {
        Process p = vmstatProcess;
        if (p != null) {
            try {
                if (p.getErrorStream().available() > 0) {
                    StringBuilder errorMessage = new StringBuilder();
                    InputStreamReader err = new InputStreamReader(p.getErrorStream());
                    char[] buf = new char[1024];
                    int read;

                    while (p.getErrorStream().available() > 0) {
                        read = err.read(buf);
                        if (read == -1)
                            break;
                        errorMessage.append(buf, 0, read);
                    }

                    if (!terminated)
                        LOG.warn("Error message from vmstat process: " + errorMessage);
                }
            }
            catch (IOException e) {
                LOG.warn("Exception while reading error stream", e);
            }
        }
    }
}

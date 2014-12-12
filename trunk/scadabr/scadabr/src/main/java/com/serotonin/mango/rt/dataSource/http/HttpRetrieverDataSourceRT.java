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

import br.org.scadabr.ShouldNeverHappenException;
import br.org.scadabr.utils.ImplementMeException;
import br.org.scadabr.timer.cron.CronExpression;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.NoMatchException;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import br.org.scadabr.web.http.HttpUtils;
import br.org.scadabr.utils.i18n.LocalizableException;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.vo.datasource.http.HttpRetrieverDataSourceEventKey;
import com.serotonin.mango.rt.dataImage.AlphaNumericValueTime;
import com.serotonin.mango.rt.dataImage.BooleanValueTime;
import com.serotonin.mango.rt.dataImage.DoubleValueTime;
import com.serotonin.mango.rt.dataImage.MultistateValueTime;
import com.serotonin.mango.view.text.MultistateRenderer;
import com.serotonin.mango.view.text.MultistateValue;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Matthew Lohbihler
 */
@Configurable
public class HttpRetrieverDataSourceRT extends PollingDataSource<HttpRetrieverDataSourceVO> {

    @Autowired
    private Common common;

    private static final int READ_LIMIT = 1024 * 1024; // One MB

    public HttpRetrieverDataSourceRT(HttpRetrieverDataSourceVO vo) {
        super(vo, true);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
    }

    /*
     @Override
     public void removeDataPoint(DataPointRT dataPoint) {
     returnToNormal(PARSE_EXCEPTION_EVENT, System.currentTimeMillis());
     super.removeDataPoint(dataPoint);
     }
     */
    @Override
    public void doPoll(long time) {
        updateChangedPoints();
        String data;
        try {
            data = getData(vo.getUrl(), vo.getTimeoutSeconds(), vo.getRetries());
        } catch (Exception e) {
            LocalizableMessage lm;
            if (e instanceof LocalizableException) {
                lm = (LocalizableException) e;
            } else {
                lm = new LocalizableMessageImpl("event.httpRetriever.retrievalError", vo.getUrl(), e.getMessage());
            }
            raiseAlarm(HttpRetrieverDataSourceEventKey.DATA_RETRIEVAL_FAILURE, time, lm);
            return;
        }

        // If we made it this far, everything is good.
        clearAlarm(HttpRetrieverDataSourceEventKey.DATA_RETRIEVAL_FAILURE, time);

        // We have the data. Now run the regex.
        LocalizableMessage parseErrorMessage = null;
        for (DataPointRT dp : enabledDataPoints.values()) {
            final HttpRetrieverPointLocatorRT locator = (HttpRetrieverPointLocatorRT) dp.getPointLocator();

            try {
                // Get the time.
                final long valueTimestamp = getValueTime(dp, time, data);
                switch (locator.getDataType()) {
                    case ALPHANUMERIC:
                        dp.updatePointValue(new AlphaNumericValueTime(data, dp.getId(), valueTimestamp));
                        break;
                    case BOOLEAN:
                        dp.updatePointValue(new BooleanValueTime(!data.equals(locator.getBinary0Value()), dp.getId(), valueTimestamp));
                        break;
                    case DOUBLE:
                        try {
                            if (locator.getValueFormat() != null) {
                                dp.updatePointValue(new DoubleValueTime(locator.getValueFormat().parse(data).doubleValue(), dp.getId(), valueTimestamp));
                            } else {
                                dp.updatePointValue(new DoubleValueTime(Double.parseDouble(data), dp.getId(), valueTimestamp));
                            }
                        } catch (NumberFormatException e) {
                            if (dp.getVoName() == null) {
                                throw new LocalizableException("event.valueParse.numericParse", data);
                            }
                            throw new LocalizableException("event.valueParse.numericParsePoint", data, dp.getVoName());
                        } catch (ParseException e) {
                            if (dp.getVoName() == null) {
                                throw new LocalizableException("event.valueParse.generalParse", e.getMessage(), data);
                            }
                            throw new LocalizableException("event.valueParse.generalParsePoint", e.getMessage(), data, dp.getVoName());
                        }
                        break;
                    case IMAGE:
                        throw new ShouldNeverHappenException("Cant handle dataType " + locator.getDataType());
                    case MULTISTATE:
        throw new ImplementMeException();
        /* TODO half localized stuff
                        
                        boolean found = false;
                        if (dp.getVo().getTextRenderer() instanceof MultistateRenderer) {
                            List<MultistateValue> multistateValues = ((MultistateRenderer) dp.getVo().getTextRenderer()).getMultistateValues();
                            for (MultistateValue multistateValue : multistateValues) {
                                if (multistateValue.getText().equalsIgnoreCase(data)) {
                                    dp.updatePointValue(new MultistateValueTime(multistateValue.getKey(), dp.getId(), valueTimestamp));
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            try {
                                dp.updatePointValue(new MultistateValueTime((byte) Short.parseShort(data), dp.getId(), valueTimestamp));
                            } catch (NumberFormatException e) {
                                throw new LocalizableException("event.valueParse.textParsePoint", data, dp.getVoName());
                            }
                        }
                        break;
                */
                    default:
                        throw new ShouldNeverHappenException("Cant handle dataType " + locator.getDataType());
                }
            } catch (NoMatchException e) {
                if (!locator.isIgnoreIfMissing()) {
                    if (parseErrorMessage == null) {
                        parseErrorMessage = e;
                    }
                }
            } catch (LocalizableException e) {
                if (parseErrorMessage == null) {
                    parseErrorMessage = e;
                }
            }
        }

        if (parseErrorMessage != null) {
            fireEvent(HttpRetrieverDataSourceEventKey.PARSE_EXCEPTION, time, parseErrorMessage);
        } else {
            //TODO returnToNormal was not set so there is no need to do this ???  returnToNormal(PARSE_EXCEPTION_EVENT, time);
        }
    }

    public String getData(String url, int timeoutSeconds, int retries) throws LocalizableException {
        // Try to get the data.
        String data;
        while (true) {
            HttpClient client = common.getHttpClient(timeoutSeconds * 1000);
            GetMethod method = null;
            LocalizableException localizableException;

            try {
                method = new GetMethod(url);
                int responseCode = client.executeMethod(method);
                if (responseCode == HttpStatus.SC_OK) {
                    data = HttpUtils.readResponseBody(method, READ_LIMIT);
                    break;
                }
                localizableException = new LocalizableException("event.http.response", url, responseCode);
            } catch (Exception e) {
                localizableException = DataSourceRT.wrapException(e);
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }

            if (retries <= 0) {
                throw localizableException;
            }
            retries--;

            // Take a little break instead of trying again immediately.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // no op
            }
        }

        return data;
    }

    public long getValueTime(DataPointRT dp, long time, String data)
            throws LocalizableException {
        if (data == null) {
            throw new LocalizableException("event.valueParse.noData", dp.getVoName());
        }
        final HttpRetrieverPointLocatorRT locator = (HttpRetrieverPointLocatorRT) dp.getPointLocator();

        // Get the time.
        long valueTime = time;
        Matcher matcher = locator.getTimePattern().matcher(data);
        if (matcher.find()) {
            String timeStr = matcher.group(1);
            try {
                valueTime = locator.getTimeFormat().parse(timeStr).getTime();
            } catch (ParseException e) {
                throw new LocalizableException("event.valueParse.timeParsePoint", timeStr, dp.getVoName());
            }
        } else {
            throw new LocalizableException("event.valueParse.noTime", dp.getVoName());
        }

        return valueTime;
    }

    @Override
    protected CronExpression getCronExpression() throws ParseException {
        throw new ImplementMeException();
    }
}

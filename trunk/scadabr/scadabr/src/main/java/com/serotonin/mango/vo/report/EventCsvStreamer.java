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
package com.serotonin.mango.vo.report;

import java.io.PrintWriter;
import java.util.List;
import java.util.ResourceBundle;

import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.view.export.CsvWriter;
import br.org.scadabr.web.i18n.I18NUtils;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.l10n.Localizer;
import java.util.Locale;

/**
 * @author Matthew Lohbihler
 */
public class EventCsvStreamer {

    public EventCsvStreamer(PrintWriter out, List<EventInstance> events, ResourceBundle bundle) {
        CsvWriter csvWriter = new CsvWriter();
        String[] data = new String[7];

        // Write the headers.
        data[0] = Localizer.localizeI18nKey("reports.eventList.id", bundle);
        data[1] = Localizer.localizeI18nKey("common.alarmLevel", bundle);
        data[2] = Localizer.localizeI18nKey("common.activeTime", bundle);
        data[3] = Localizer.localizeI18nKey("reports.eventList.message", bundle);
        data[4] = Localizer.localizeI18nKey("reports.eventList.status", bundle);
        data[5] = Localizer.localizeI18nKey("reports.eventList.ackTime", bundle);
        data[6] = Localizer.localizeI18nKey("reports.eventList.ackUser", bundle);

        out.write(csvWriter.encodeRow(data));

        for (EventInstance event : events) {
            data[0] = Integer.toString(event.getId());
            data[1] = Localizer.localizeMessage(AlarmLevels.getAlarmLevelMessage(event.getAlarmLevel()), bundle);
            data[2] = Localizer.localizeDateTime(bundle.getLocale(), event.getActiveTimestamp());
            data[3] = Localizer.localizeMessage(event.getMessage(), bundle);

            if (event.isActive()) {
                data[4] = Localizer.localizeI18nKey("common.active", bundle);
            } else if (!event.isRtnApplicable()) {
                data[4] = "";
            } else {
                data[4] = String.format("%s - %s", Localizer.localizeDateTime(bundle.getLocale(), event.getRtnTimestamp()), Localizer.localizeMessage(event.getRtnMessage(), bundle));
            }

            if (event.isAcknowledged()) {
                data[5] = Localizer.localizeDateTime(bundle.getLocale(), event.getAcknowledgedTimestamp());

                LocalizableMessage ack = event.getExportAckMessage();
                if (ack == null) {
                    data[6] = "";
                } else {
                    data[6] = Localizer.localizeMessage(ack, bundle);
                }
            } else {
                data[5] = "";
                data[6] = "";
            }

            out.write(csvWriter.encodeRow(data));
        }

        out.flush();
        out.close();
    }
}

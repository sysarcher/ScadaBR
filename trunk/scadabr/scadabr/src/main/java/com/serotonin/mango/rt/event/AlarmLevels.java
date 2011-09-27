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
package com.serotonin.mango.rt.event;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.web.i18n.LocalizableMessage;
import java.util.ResourceBundle;

public enum AlarmLevels {

    NONE(0, "common.alarmLevel.none"),
    INFORMATION(1, "common.alarmLevel.info"),
    URGENT(2, "common.alarmLevel.urgent"),
    CRITICAL(3, "common.alarmLevel.critical"),
    LIFE_SAFETY(4, "common.alarmLevel.lifeSafety");
    public final int mangoId;
    private final LocalizableMessage messageI18n;

    private AlarmLevels(int mangoId, String keyI18n) {
        this.mangoId = mangoId;
        this.messageI18n = new LocalizableMessage(keyI18n);
    }

    public static AlarmLevels fromMangoId(int mangoId) {

        for (AlarmLevels dt : AlarmLevels.values()) {
            if (dt.mangoId == mangoId) {
                return dt;
            }
        }
        throw new ShouldNeverHappenException("Cant find alarmLevel of mangoId: " + mangoId);
    }

    public LocalizableMessage getMessageI18n() {
        return messageI18n;
    }

    public String getLocalizedMessage(ResourceBundle bundle) {
        return messageI18n.getLocalizedMessage(bundle);
    }
    
    public String getI18nMessageKey() {
        return messageI18n.getKey();
    }
    
}

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
package com.serotonin.mango.db.upgrade;

import com.serotonin.ShouldNeverHappenException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Lohbihler
 */
public class Upgrade1_6_2 extends DBUpgrade {

    private final static Logger LOG = LoggerFactory.getLogger(Upgrade1_6_2.class);

    @Override
    public void upgrade() throws Exception {
        runScript(script1);

        switch (getDataBaseType()) {
            case DERBY:
                runScript(derbyScript2);
                break;
            case MYSQL:
                runScript(mysqlScript2);
                break;
            default:
                throw new ShouldNeverHappenException("Unknow database " + getDataBaseType());

        }
    }

    @Override
    protected String getNewSchemaVersion() {
        return "1.6.3";
    }
    private static String[] script1 = {"alter table pointEventDetectors add column weight double;",};
    private static String[] derbyScript2 = {};
    private static String[] mysqlScript2 = {"alter table dataSources modify data longblob not null;",
        "alter table dataPoints modify data longblob not null;",
        "alter table mangoViews modify data longblob not null;",
        "alter table eventHandlers modify data longblob not null;",
        "alter table reports modify data longblob not null;",
        "alter table reportInstancePoints modify textRenderer longblob;",
        "alter table publishers modify data longblob not null;",
        "alter table systemSettings modify settingValue longtext;",
        "alter table pointValueAnnotations modify textPointValueLong longtext;",
        "alter table events modify message longtext;",
        "alter table reportInstanceDataAnnotations modify textPointValueLong longtext;",
        "alter table reportInstanceEvents modify message longtext;",};
}
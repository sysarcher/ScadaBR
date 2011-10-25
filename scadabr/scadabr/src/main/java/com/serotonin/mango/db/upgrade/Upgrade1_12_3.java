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

/**
 * @author Matthew Lohbihler
 */
public class Upgrade1_12_3 extends DBUpgrade {

    @Override
    public void upgrade() throws Exception {
        switch (getDataBaseType()) {
            case DERBY:
                runScript(derbyScript2);
                break;
            case MYSQL:
                runScript(mysqlScript2);
                break;
            case MSSQL:
                runScript(mssqlScript2);
                break;
            default:
                throw new ShouldNeverHappenException("Unknow database " + getDataBaseType());
        }
    }

    @Override
    protected String getNewSchemaVersion() {
        return "1.12.4";
    }
    private final String[] derbyScript2 = { //
        "alter table dataSources add column rtdata blob;", //
        "alter table publishers add column rtdata blob;", //
    };
    private final String[] mssqlScript2 = { //
        "alter table dataSources add column rtdata image;", //
        "alter table publishers add column rtdata image;", //
    };
    private final String[] mysqlScript2 = { //
        "alter table dataSources add column rtdata longblob;", //
        "alter table publishers add column rtdata longblob;", //
    };
}

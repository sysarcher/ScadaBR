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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.mango.db.DatabaseAccess;
import com.serotonin.mango.db.dao.ViewDao;

/**
 * @author Matthew Lohbihler
 */
public class Upgrade1_6_0 extends DBUpgrade {
    private final static Logger LOG = LoggerFactory.getLogger(Upgrade1_6_0.class);

    @Override
    public void upgrade() throws Exception {
        OutputStream out = createUpdateLogOutputStream("1_6_0");

        // Run the script.
        LOG.info("Running script 1");
        runScript(script1, out);

        xid();

        // Run the script.
        LOG.info("Running script 2");
        Map<String, String[]> scripts = new HashMap<String, String[]>();
        scripts.put(DatabaseAccess.DatabaseType.DERBY.name(), derbyScript2);
        scripts.put(DatabaseAccess.DatabaseType.MYSQL.name(), mysqlScript2);
        runScript(scripts, out);

        out.flush();
        out.close();
    }

    @Override
    protected String getNewSchemaVersion() {
        return "1.6.1";
    }

    private static String[] script1 = { "alter table mangoViews add column xid varchar(20);", };

    private static String[] derbyScript2 = { "alter table mangoViews alter xid not null;",
            "alter table mangoViews add constraint mangoViewsUn1 unique (xid);", };

    private static String[] mysqlScript2 = { "alter table mangoViews modify xid varchar(20) not null;",
            "alter table mangoViews add constraint mangoViewsUn1 unique (xid);", };

    private void xid() {
        // Default the xid values.
        ViewDao viewDao = new ViewDao();
        List<Integer> ids = getJdbcTemplate().queryForList("select id from mangoViews", Integer.class);
        for (Integer id : ids)
            getSimpleJdbcTemplate().update("update mangoViews set xid=? where id=?", viewDao.generateUniqueXid(), id);
    }
}

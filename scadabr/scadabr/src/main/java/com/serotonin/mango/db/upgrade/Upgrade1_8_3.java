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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.mango.db.dao.CompoundEventDetectorDao;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.db.dao.MailingListDao;
import com.serotonin.mango.db.dao.PublisherDao;
import com.serotonin.mango.db.dao.ScheduledEventDao;

/**
 * @author Matthew Lohbihler
 */
public class Upgrade1_8_3 extends DBUpgrade {

    private final static Logger LOG = LoggerFactory.getLogger(Upgrade1_8_3.class);

    @Override
    public void upgrade() throws Exception {
        runScript(script1);

        xid();

        switch (getDataBaseType()) {
            case DERBY:
                runScript(derbyScript2);
                break;
            case MYSQL:
                runScript(mysqlScript2);
                // Run the MySQL fix script
                try {
                    runScript(mysqlScript3);
                } catch (Exception e) {
                    // Ignore. The FKs likely already existed.
                }
                break;
            default:
                throw new ShouldNeverHappenException("Unknow database " + getDataBaseType());
        }
    }

    @Override
    protected String getNewSchemaVersion() {
        return "1.9.0";
    }
    private static String[] script1 = {"alter table scheduledEvents add column xid varchar(50);",
        "alter table compoundEventDetectors add column xid varchar(50);",
        "alter table mailingLists add column xid varchar(50);",
        "alter table publishers add column xid varchar(50);",
        "alter table eventHandlers add column xid varchar(50);",};
    private static String[] derbyScript2 = {"alter table scheduledEvents alter xid not null;",
        "alter table scheduledEvents add constraint scheduledEventsUn1 unique (xid);",
        "alter table compoundEventDetectors alter xid not null;",
        "alter table compoundEventDetectors add constraint compoundEventDetectorsUn1 unique (xid);",
        "alter table mailingLists alter xid not null;",
        "alter table mailingLists add constraint mailingListsUn1 unique (xid);",
        "alter table publishers alter xid not null;",
        "alter table publishers add constraint publishersUn1 unique (xid);",
        "alter table eventHandlers alter xid not null;",
        "alter table eventHandlers add constraint eventHandlersUn1 unique (xid);",};
    private static String[] mysqlScript2 = {"alter table scheduledEvents modify xid varchar(50) not null;",
        "alter table scheduledEvents add constraint scheduledEventsUn1 unique (xid);",
        "alter table compoundEventDetectors modify xid varchar(50) not null;",
        "alter table compoundEventDetectors add constraint compoundEventDetectorsUn1 unique (xid);",
        "alter table mailingLists modify xid varchar(50) not null;",
        "alter table mailingLists add constraint mailingListsUn1 unique (xid);",
        "alter table publishers modify xid varchar(50) not null;",
        "alter table publishers add constraint publishersUn1 unique (xid);",
        "alter table eventHandlers modify xid varchar(50) not null;",
        "alter table eventHandlers add constraint eventHandlersUn1 unique (xid);",};
    private static String[] mysqlScript3 = {
        "alter table userEvents engine=InnoDB;",
        "delete from userEvents where eventId not in (select id from events);",
        "delete from userEvents where userId not in (select id from users);",
        "alter table userEvents add constraint userEventsFk1 foreign key (eventId) references events(id) on delete cascade;",
        "alter table userEvents add constraint userEventsFk2 foreign key (userId) references users(id) on delete cascade;",};

    private void xid() {
        // Default the xid values.
        ScheduledEventDao scheduledEventDao = new ScheduledEventDao();
        List<Integer> ids = getJdbcTemplate().queryForList("select id from scheduledEvents", Integer.class);
        for (Integer id : ids) {
            getSimpleJdbcTemplate().update("update scheduledEvents set xid=? where id=?", scheduledEventDao.generateUniqueXid(), id);
        }

        CompoundEventDetectorDao compoundEventDetectorDao = new CompoundEventDetectorDao();
        ids = getJdbcTemplate().queryForList("select id from compoundEventDetectors", Integer.class);
        for (Integer id : ids) {
            getSimpleJdbcTemplate().update("update compoundEventDetectors set xid=? where id=?", compoundEventDetectorDao.generateUniqueXid(), id);
        }

        MailingListDao mailingListDao = new MailingListDao();
        ids = getJdbcTemplate().queryForList("select id from mailingLists", Integer.class);
        for (Integer id : ids) {
            getSimpleJdbcTemplate().update("update mailingLists set xid=? where id=?", mailingListDao.generateUniqueXid(), id);
        }

        PublisherDao publisherDao = new PublisherDao();
        ids = getJdbcTemplate().queryForList("select id from publishers", Integer.class);
        for (Integer id : ids) {
            getSimpleJdbcTemplate().update("update publishers set xid=? where id=?", publisherDao.generateUniqueXid(), id);
        }

        EventDao eventDao = new EventDao();
        ids = getJdbcTemplate().queryForList("select id from eventHandlers", Integer.class);
        for (Integer id : ids) {
            getSimpleJdbcTemplate().update("update eventHandlers set xid=? where id=?", eventDao.generateUniqueXid(), id);
        }
    }
}
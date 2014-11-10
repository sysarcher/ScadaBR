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
package com.serotonin.mango.db;

import br.org.scadabr.logger.LogUtils;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

@Named
public class DatabaseAccessFactory {

    private final static Logger log = Logger.getLogger(LogUtils.LOGGER_SCADABR_DAO);
    private DatabaseAccess databaseAccess;

    @Deprecated
    @Inject
    ServletContext ctx;

    public DatabaseAccessFactory() {

    }

    @PostConstruct
    public void startDB() {
        log.severe("Start DB called");
        if (databaseAccess == null) {
            databaseAccess = DatabaseAccess.createDatabaseAccess(ctx);
            databaseAccess.initialize();
        }
    }

    @PreDestroy
    public void stopDB() {
        log.severe("Stop DB called");
        if (databaseAccess != null) {
            databaseAccess.terminate();
        }
        databaseAccess = null;
    }

    public DatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }

}

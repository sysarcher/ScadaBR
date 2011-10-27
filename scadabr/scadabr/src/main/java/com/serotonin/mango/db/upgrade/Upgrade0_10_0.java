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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Lohbihler
 */
public class Upgrade0_10_0 extends DBUpgrade {

    private final static Logger LOG = LoggerFactory.getLogger(Upgrade0_10_0.class);

    @Override
    public void upgrade() throws Exception {
        runScript(script);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "1.0.0";
    }
    private static String[] script = {};
}
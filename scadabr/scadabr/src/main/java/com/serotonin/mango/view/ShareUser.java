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
package com.serotonin.mango.view;

import com.serotonin.mango.util.ExportCodes;

/**
 * @author Matthew Lohbihler
 */

public class ShareUser {

    public static final int ACCESS_NONE = 0;
    public static final int ACCESS_READ = 1;
    public static final int ACCESS_SET = 2;
    public static final int ACCESS_OWNER = 3;

    public static final ExportCodes ACCESS_CODES = new ExportCodes();

    static {
        ACCESS_CODES.addElement(ACCESS_NONE, "NONE", "common.access.none");
        ACCESS_CODES.addElement(ACCESS_READ, "READ", "common.access.read");
        ACCESS_CODES.addElement(ACCESS_SET, "SET", "common.access.set");
    }

    private int userId;
    private int accessType;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAccessType() {
        return accessType;
    }

    public void setAccessType(int accessType) {
        this.accessType = accessType;
    }

}

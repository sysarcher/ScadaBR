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
package br.org.scadabr.dao.jdbc;

import br.org.scadabr.db.DaoUtils;
import java.util.List;
import java.util.Random;

public class BaseDao extends DaoUtils {

    public final Random random = new Random();

    /**
     * Public constructor for code that needs to get stuff from the database.
     */
    public BaseDao() {
        super();
    }

    //
    // Convenience methods for storage of booleans.
    //
    protected String boolToChar(boolean b) {
        return b ? "Y" : "N";
    }

    protected boolean charToBool(String s) {
        return "Y".equals(s);
    }

    protected void deleteInChunks(String sql, List<Integer> ids) {
        int chunk = 1000;
        for (int i = 0; i < ids.size(); i += chunk) {
            String idStr = createDelimitedList(ids, i, i + chunk, ",", null);
            ejt.update(sql + " (" + idStr + ")");
        }
    }

    //
    // XID convenience methods
    //
    protected String generateUniqueXid(String prefix, String tableName) {
        String xid = generateXid(prefix);
        while (!isXidUnique(xid, tableName)) {
            xid = generateXid(prefix);
        }
        return xid;
    }

    protected boolean isXidUnique(String xid, int excludeId, String tableName) {
        return ejt.queryForObject(
                "select count(*) from " + tableName + " where xid=? and id<>?", 
                Integer.class, 
                xid, excludeId) == 0;
    }

    protected boolean isXidUnique(String xid, String tableName) {
        return ejt.queryForObject(
                "select count(*) from " + tableName + " where xid=?", 
                Integer.class, 
                xid) == 0;
    }
    
    public String generateXid(String prefix) {
        return prefix + generateRandomString(6, "0123456789");
    }

    public String generateRandomString(int length, String charSet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(charSet.charAt(random.nextInt(charSet.length())));
        }
        return sb.toString();
    }
    
}

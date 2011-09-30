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
package com.serotonin.mango.web.dwr.vmstat;

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatDataSourceVO;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatPointLocatorVO;
import com.serotonin.mango.web.dwr.DataSourceEditDwr;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.dwr.MethodFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Lohbihler
 */
public class VMStatEditDwr extends DataSourceEditDwr {
	private static final Logger LOG = LoggerFactory.getLogger(VMStatEditDwr.class);

	@MethodFilter
	public DwrResponseI18n saveVMStatDataSource(String name, String xid,
			int pollSeconds, int outputScale) {
		VMStatDataSourceVO ds = (VMStatDataSourceVO) Common.getUser()
				.getEditDataSource();

		ds.setXid(xid);
		ds.setName(name);
		ds.setPollSeconds(pollSeconds);
		ds.setOutputScale(outputScale);

		return tryDataSourceSave(ds);
	}

	@MethodFilter
	public DwrResponseI18n saveVMStatPointLocator(int id, String xid,
			String name, VMStatPointLocatorVO locator) {
		return validatePoint(id, xid, name, locator, null);
	}

}

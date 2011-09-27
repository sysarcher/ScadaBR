<%--
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
    along with this program.  If not, see http://www.gnu.org/licenses/.
--%><%@include file="/WEB-INF/tags/decl.tagf" %><%--
--%><%@tag body-content="empty" %><%--
--%><%@attribute name="allOption" type="java.lang.Boolean" %><%--
--%><%@attribute name="sst" type="java.lang.Boolean" %><%--
--%><%@tag import="com.serotonin.mango.rt.event.AlarmLevels"%><%--
--%><c:choose>
  <c:when test="${sst}">
    <c:if test="${allOption}">
      <sst:option value=""><fmt:message key="common.all"/></sst:option>
    </c:if>
    <sst:option value="<%= AlarmLevels.NONE.name() %>"><fmt:message key="<%= AlarmLevels.NONE.getI18nMessageKey() %>"/></sst:option>
    <sst:option value="<%= AlarmLevels.INFORMATION.name() %>"><fmt:message key="<%= AlarmLevels.INFORMATION.getI18nMessageKey()  %>"/></sst:option>
    <sst:option value="<%= AlarmLevels.URGENT.name() %>"><fmt:message key="<%= AlarmLevels.URGENT.getI18nMessageKey() %>"/></sst:option>
    <sst:option value="<%= AlarmLevels.CRITICAL.name() %>"><fmt:message key="<%= AlarmLevels.CRITICAL.getI18nMessageKey() %>"/></sst:option>
    <sst:option value="<%= AlarmLevels.LIFE_SAFETY.name() %>"><fmt:message key="<%= AlarmLevels.LIFE_SAFETY.getI18nMessageKey() %>"/></sst:option>
  </c:when>
  <c:otherwise>
    <c:if test="${allOption}">
      <option value=""><fmt:message key="common.all"/></option>
    </c:if>
    <option value="<%= AlarmLevels.NONE.name() %>"><fmt:message key="<%= AlarmLevels.NONE.getI18nMessageKey() %>"/></option>
    <option value="<%= AlarmLevels.INFORMATION.name() %>"><fmt:message key="<%= AlarmLevels.INFORMATION.getI18nMessageKey() %>"/></option>
    <option value="<%= AlarmLevels.URGENT.name() %>"><fmt:message key="<%= AlarmLevels.URGENT.getI18nMessageKey() %>"/></option>
    <option value="<%= AlarmLevels.CRITICAL.name() %>"><fmt:message key="<%= AlarmLevels.CRITICAL.getI18nMessageKey() %>"/></option>
    <option value="<%= AlarmLevels.LIFE_SAFETY.name() %>"><fmt:message key="<%= AlarmLevels.LIFE_SAFETY.getI18nMessageKey() %>"/></option>
  </c:otherwise>
</c:choose>

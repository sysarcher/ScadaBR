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
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<tag:page >

    <div data-dojo-type="dijit/form/Form" id="myForm" data-dojo-id="myForm"
         encType="multipart/form-data" action="login.htm" method="post">
        <div data-dojo-type="dojox.layout.TableContainer" data-dojo-props="cols:1" id="tc1">
            <spring:bind path="login.username">
                <div data-dojo-type="dijit.form.TextBox" name="username" title="<fmt:message key="login.userId"/>:" value="${status.value}"></div>
            </spring:bind>
            <spring:bind path="login.password">
                <div data-dojo-type="dijit.form.TextBox" name="password" title="<fmt:message key="login.password"/>:" value="${status.value}"></div>
            </spring:bind>
        </div>

        <button data-dojo-type="dijit/form/Button" type="submit" value="Submit"><fmt:message key="login.loginButton"/></button>
    </div>

</tag:page>
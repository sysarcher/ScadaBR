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

    <script>
        //A little bit clumsy but thats the only way to get this to work on firefox ....
        // Wait for parser to finish???
        require(["dojo/domReady!"], function () {
            setTimeout(function () {
                require(["dijit/focus", "dojo/dom"], function (focusUtil, dom) {
                    focusUtil.focus(dom.byId("username"));
                });
            }, 1000);
        });
    </script>

    <dijit:form action="login.htm" method="post">
        <dojox:tableContainer cols="1">
            <spring:bind path="login.username">
                    <dijit:validationTextBox i18nLabel="login.userId" />
            </spring:bind>
            <spring:bind path="login.password">
                <dijit:validationTextBox type="password" i18nLabel="login.password" />
            </spring:bind>
        </dojox:tableContainer>

        <dijit:button type="submit" i18nLabel="login.loginButton" />
    </dijit:form>

</tag:page>
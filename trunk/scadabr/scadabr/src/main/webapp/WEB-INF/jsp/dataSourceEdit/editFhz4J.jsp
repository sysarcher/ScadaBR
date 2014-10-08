<%--
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2009 Arne Pl�se.
    @author Arne Pl�se

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
<%@include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<tag:page>

    <jsp:body>

        <dijit:sidebarBorderContainer gutters="true" liveSplitters="true" >
            <dijit:leftContentPane id="pointTreeContentPane" splitter="true" >
                <div id="dataPointTree"></div>
            </dijit:leftContentPane>
            <dijit:centerContentPane >
                <dijit:headlineLayoutContainer >
                    <dijit:topContentPane >
                    </dijit:topContentPane>
                    <dijit:centerContentPane >
                        <div id="watchListTable"></div>
                    </dijit:centerContentPane>
                </dijit:headlineLayoutContainer>
            </dijit:centerContentPane>
        </dijit:sidebarBorderContainer>
    </jsp:body>
</tag:page>

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
    <jsp:body>
                <script type="text/javascript">
            var _dataSources;
            require([
                "scadabr/jsp/DataSources",
                "dojo/domReady!"
            ], function (DataSources) {
                _dataSources = new DataSources("dataSourcesList", "dataSourceTypesSelect", "addDataSource");
            });

        </script>

        <dijit:headlineLayoutContainer>
            <dijit:topContentPane>
                <div class="smallTitle titlePadding" style="float:left;">
                    <tag:img png="icon_ds" title="dsList.dataSources"/>
                    <fmt:message key="dsList.dataSources"/>
                    <tag:help id="dataSourceList"/>
                </div>

                <div class="titlePadding" style="float:right;">
                    <dijit:selectFromMap id="dataSourceTypesSelect" map="${dataSourceTypes}" value="${defaultDataSourceType}"></dijit:selectFromMap>
                    <dijit:button id="addDataSource" iconClass="scadaBrAddDataSourceIcon" i18nLabel="common.add"/>
                </div>

            </dijit:topContentPane>
            <dijit:centerContentPane id="dataSourcesList">
            </dijit:centerContentPane>
        </dijit:headlineLayoutContainer>

    </jsp:body>
</tag:page>
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
        <style>

            #pointTreeContentPane{
                width: 250px; 
            }

        </style>
        <script type="text/javascript">
            require([
                "scadabr/jsp/DataPoints",
                "dojo/domReady!"
            ], function (DataPoints) {
                var _dataPoints = new DataPoints("dataPointTree", "dataPointEdit");
            });

        </script>

        <dijit:sidebarBorderContainer gutters="true" liveSplitters="true" >
            <dijit:leftContentPane id="pointTreeContentPane" splitter="true" >
                <div id="dataPointTree"></div>
            </dijit:leftContentPane>
            <dijit:centerContentPane >
                <div data-dojo-type="dijit/layout/TabContainer" >
                    <div data-dojo-type="dijit/layout/ContentPane" title="Show data (chart)" data-dojo-props="selected:true">
                        <div id="dataPointDetailsChart"></div>
                    </div>
                    <div data-dojo-type="dijit/layout/ContentPane" title="Show data (table)" data-dojo-props="selected:true">
                        <div id="dataPointDetailsTable"></div>
                    </div>
                    <div data-dojo-type="dijit/layout/ContentPane" id="second" title="Edit Point">
                        <dijit:sidebarBorderContainer gutters="false">
                            <dijit:centerContentPane >
                                <div id="dataPointEdit"></div>
                            </dijit:centerContentPane>
                            <dijit:bottomContentPane >
                                <div style="float:right;">
                                    <dijit:button type="submit" i18nLabel="common.revert" />
                                    <dijit:button type="submit" i18nLabel="common.save" />
                                </div>
                            </dijit:bottomContentPane>
                        </dijit:sidebarBorderContainer>
                    </div>
                    <div data-dojo-type="dijit/layout/ContentPane" title="point Events and Notes" data-dojo-props="selected:true">
                        <div id="dataPointDetailsEventsAndNotes"></div>
                    </div>
                    <div data-dojo-type="dijit/layout/ContentPane" title="point usage" data-dojo-props="selected:true">
                        <div id="dataPointUsages"></div>
                    </div>

                </div>

            </dijit:centerContentPane>
        </dijit:sidebarBorderContainer>
    </jsp:body>
</tag:page>
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

        <dijit:headlineLayoutContainer >
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
            <dijit:centerContentPane>
                <dijit:sidebarBorderContainer gutters="true" liveSplitters="true">
                    <dijit:leftContentPane>
                        <div id="dataSourcesTree"></div>
                    </dijit:leftContentPane>
                    <dijit:centerContentPane id="dataSourcesList">

                    </dijit:centerContentPane>

                </dijit:sidebarBorderContainer>
            </dijit:centerContentPane>
        </dijit:headlineLayoutContainer>
    </jsp:body>
</tag:page>
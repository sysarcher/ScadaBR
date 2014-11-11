<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<tag:page helpId="dataSources">
    <jsp:body>
        <script type="text/javascript">
            var _dataSources;
            require([
                "scadabr/jsp/DataSources",
                "dojo/domReady!"
            ], function (DataSources) {
                _dataSources = new DataSources("dataSourcesTree", "treeNodeDetailsWidget");
            });

        </script>

        <dijit:sidebarBorderContainer gutters="true" liveSplitters="true">
            <dijit:leftContentPane>
                <div id="dataSourcesTree"></div>
            </dijit:leftContentPane>
            <dijit:centerContentPane >
                    <div id="treeNodeDetailsWidget" data-dojo-type="dojox/layout/ContentPane" title="Show data (chart)" data-dojo-props="selected:true, parseOnLoad: false, contentUrl: 'dataPointDetails/renderChart'">
                    </div>
            </dijit:centerContentPane>

        </dijit:sidebarBorderContainer>
    </jsp:body>
</tag:page>
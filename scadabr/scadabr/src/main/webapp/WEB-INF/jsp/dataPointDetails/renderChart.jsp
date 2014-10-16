<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<script src='../resources/dojo/dojo.js' data-dojo-config="isDebug: true, async: true, parseOnLoad: true"></script>
<div id="chartContentId">
    <div id="chartId" ></div>
    <dojox:tableContainer cols="2" style="float:left;">
        <input id="fromDate" label="von Datum:" title="Datum" data-dojo-type="dijit/form/DateTextBox" value="2014-09-21"/>
        <input id="toDate" label="bis Datum:" title="Datum" data-dojo-type="dijit/form/DateTextBox"/>
        <input id="fromTime" label="von Zeit:" title="Zeit" data-dojo-type="dojox/form/TimeSpinner"/>
        <input id="toTime" label="bis Zeit:" title="Zeit" data-dojo-type="dojox/form/TimeSpinner"/>
        <input id="checkInception" label="vom Anfang:" data-dojo-type="dijit/form/CheckBox" />
        <input id="mycheLatest" label="Bis Ende:" data-dojo-type="dijit/form/CheckBox"/>
    </dojox:tableContainer>
    <dijit:button type="submit" i18nLabel="login.loginButton" />
</div>

<script type="text/javascript">
    var md;
    var timeSpinner;
    require([
        "dojo/dom",
        "dojo/parser"
    ], function (dom, parser) {
        var chartContent = dom.byId("chartContentId");
        parser.parse(chartContent).then(function () {
            setupChart();
            //setup    
        });
    });

    var store;
    var chart;

    function setupChart() {
        require(["dojo/store/JsonRest",
            "dojox/charting/Chart",
            "dojox/charting/StoreSeries",
            "dojox/charting/plot2d/Lines",
            "dojox/charting/plot2d/Grid",
            "dojox/charting/axis2d/Default",
            "dojo/domReady!"
        ], function (JsonRest, Chart, StoreSeries) {
            store = new JsonRest({
                target: "rest/pointValues/${dataPoint.id}",
                idProperty: "x"
            });

            chart = new Chart("chartId");
            chart.addPlot("front_grid", {
                type: "Grid", hMajorLines: true, vMajorLines: false, hMinorLines: false, vMinorLines: false
            });
            chart.addPlot("default", {type: "Lines", markers: true});
            chart.addPlot("back_grid", {
                type: "Grid", hMajorLines: false, vMajorLines: true, hMinorLines: false, vMinorLines: true
            });

            var d = new Date(2014, 9, 10);
            var startTime = d.getTime() + d.getTimezoneOffset() * 60 * 1000;


            chart.addAxis("x", {
                title: "Timestamp",
//                from: startTime,
//                min: startTime,
                titleOrientation: "away",
                majorLabels: true, majorTicks: true, majorTick: {length: 10},
                minorLabels: true, minorTicks: true, minorTick: {length: 6},
                microTicks: true, microTick: {length: 3},
                majorTickStep: 1000 * 3600 * 24 * 7, // 7 Days
                minorTickStep: 1000 * 3600 * 24,
                microTickStep: 1000 * 3600 * 12,
                natural: false,
                fixed: true,
                labelFunc: function (text, value, precision) {
                    var d = new Date();
                    d.setTime(value);
                    // todo timestamp is utc not current timezone
                    return d.toLocaleString();
                }
            });
            chart.addAxis("y", {
                vertical: true,
                fixUpper: "major",
                includeZero: true,
                title: "${dataPoint.name}",
                stroke: "green"
            });

            chart.addSeries("Series A", new StoreSeries(store, {query: {from: new Date().getTime() - 3600 * 1000, to: new Date().getTime()}}, {x: "x", y: "y"}), {stroke: {color: "red"}});

            chart.render();

            chart.updateSeries("Series A", new StoreSeries(store, {query: {from: new Date().getTime() - 3600 * 1000 * 24 * 4, to: new Date().getTime()}}, {x: "x", y: "y"}));

        });
    }
</script>
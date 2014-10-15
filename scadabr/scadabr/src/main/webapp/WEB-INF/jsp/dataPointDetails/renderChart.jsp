<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<div id="renderContent"></div>

<script type="text/javascript">

    var chart;

    require(["dojo/request",
        "dojox/charting/Chart",
        "dojox/charting/plot2d/Lines",
        "dojox/charting/plot2d/Grid",
        "dojox/charting/axis2d/Default",
        "dojo/domReady!"],
            function (request, Chart) {

                request("rest/pointValues", {
                    query: {
                        id: ${dataPoint.id}
                    },
                    handleAs: "json"
                }).then(function (data) {
                    chart = new Chart("renderContent");
                    chart.addPlot("front_grid", {
                        type: "Grid", hMajorLines: true, vMajorLines: false
                    });
                    chart.addPlot("default", {type: "Lines", markers: true});
                    chart.addPlot("back_grid", {
                        type: "Grid", hMajorLines: false, vMajorLines: true
                    });
                    
                    chart.addAxis("x", {
                        title: "Timestamp",
                        titleOrientation: "away",
                        majorLabels: true, majorTicks: true, majorTick: {length: 10},
                        minorLabels: true, minorTicks: true, minorTick: {length: 6},
                        microTicks: true, microTick: {length: 3},
                        majorTickStep: 1000 * 3600 * 24 * 7, // 7 Days
                        minorTickStep: 1000 * 3600 * 24,
                        microTickStep: 1000 * 3600 * 3,
                        labelFunc: function (value) {
                            var d = new Date();
                            d.setTime(value);
                            return d.toLocaleString();
                        }
                    });
                    chart.addAxis("y", {vertical: true, title: "${dataPoint.name}"});
                    chart.addSeries("Series A", data.values);
                    //   chart.addSeries("Load Cell 1", new StoreSeries(store, { query: { id:28 }}, "y"));

                    chart.render();
                }, function (error) {
                    alert("Error: " + error);

                });
            });


</script>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" href="resources/scadabr.css">
        <script src='resources/dojo/dojo.js' data-dojo-config="isDebug: true, async: true, parseOnLoad: true"></script>
        <script>
            require(["dojo/parser",
                "dijit/layout/LayoutContainer",
                "dijit/layout/ContentPane",
                "dijit/Toolbar",
                "dijit/TooltipDialog",
                "dijit/form/Button",
                "dijit/form/ToggleButton",
                "dijit/form/DropDownButton",
                "dijit/form/CheckBox",
                "dijit/form/NumberSpinner",
                "dijit/form/TextBox",
                "dijit/ToolbarSeparator",
                "dijit/ColorPalette",
                "dojox/layout/TableContainer"]);
        </script>
    </head>

    <body class="soria">
        <style>
            html, body {
                width: 100%;
                height: 100%;
                margin: 0;
                overflow:hidden;
            }

            #mainLayout {
                width: 100%;
                height: 100%;
            }
        </style>

        <div data-dojo-type="dijit/layout/LayoutContainer" data-dojo-props="design:'headline'" id="mainLayout">
            <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="region: 'top'">
                <table width="100%" cellspacing="0" cellpadding="0" border="0" id="mainHeader">
                    <tr>
                        <td><img src="images/mangoLogoMed.jpg" alt="Logo"/></td>
                    </tr>
                </table>
                <div id="mainToolBar" data-dojo-type="dijit/Toolbar">
                    <div data-dojo-type="dijit/form/Button" data-dojo-props="iconClass:'scadaBrWatchListIcon', showLabel:false"><fmt:message key="header.watchlist"/>
                        <script type="dojo/connect" data-dojo-event="onClick">window.location = "watch_list.shtm";</script>
                    </div>
                    <div data-dojo-type="dijit/form/Button" data-dojo-props="iconClass:'scadaBrEventsIcon', showLabel:false"><fmt:message key="header.alarms"/>
                        <script type="dojo/connect" data-dojo-event="onClick">window.location = "events.shtm";</script>
                    </div>
                    <div data-dojo-type="dijit/form/Button" data-dojo-props="iconClass:'scadaBrPointHierarchyIcon', showLabel:false"><fmt:message key="header.pointHierarchy"/>
                        <script type="dojo/connect" data-dojo-event="onClick">window.location = "point_hierarchy.shtm";</script>
                    </div>

                    <span data-dojo-type="dijit/ToolbarSeparator"></span>
                    <div data-dojo-type="dijit/form/Button" data-dojo-props="iconClass:'scadaBrLogoutIcon', showLabel:false"><fmt:message key="header.logout"/>
                        <script type="dojo/connect" data-dojo-event="onClick">window.location = "logout.htm";</script>
                    </div>
                    <!-- USE style="float:right;" to right align button ...-->

                </div>
            </div>
            <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="region: 'center'">
                <jsp:doBody />
            </div>
        </div>

    </body>
</html>
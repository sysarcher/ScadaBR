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
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<tag:page>

    <jsp:body>
        <style>

        #watchListContainer {
            width: 100%;
            height: 100%;
        }
    </style>

        <script type="text/javascript">
            
            require(["dojo/parser",
                "dijit/layout/BorderContainer",
                "dijit/layout/ContentPane"
            ]);
    
    
            var selectedFolderNode;
            var tree;
            var myRestStore;

            require([
                "dojo/dom",
                "dojo/dom-construct",
                "dojo/_base/declare",
                "dojo/request",
                "dojo/store/Memory",
                "dojo/store/Observable",
                "dojo/store/JsonRest",
                "dijit/Tree",
                "dgrid/Grid",
                "dgrid/extensions/Pagination",
                "dgrid/Keyboard",
                "dgrid/Selection",
                "dojo/rpc/JsonService",
                "dojo/on",
                "dojo/domReady!"
            ], function(dom, domConstruct, declare, request, Memory, Observable, JsonRest, Tree, Grid, Pagination, Keyboard, Selection, JsonService, on) {
                var grid;
                var svc;
                request("events/", {
                    handleAs: "json"
                }).then(function(response) {
                    // Once the response is received, build an in-memory store
                    // with the data
                    var store = new Memory({data: response});
                    // Create a Grid instance using Pagination,
                    // referencing the store
                    grid = new (declare([Grid, Pagination, Keyboard, Selection]))({
                        store: store,
                        columns: {
                            id: {
                                label: "<fmt:message key="events.id"/>"
                            },
                            alarmLevel: {
                                label: "<fmt:message key="common.alarmLevel"/>",
                                renderCell: function(event, alarmLevel, default_node, options) {
                                    var node = domConstruct.create("img");
                                    var imgName;
                                    switch (alarmLevel) {
                                        case 1:
                                            imgName = 'flag_blue';
                                            if (event.active) {
                                                node.alt = '<fmt:message key="common.alarmLevel.info"/>';
                                            } else {
                                                node.alt = '<fmt:message key="common.alarmLevel.info.rtn"/>';
                                            }
                                            break;
                                        case  2:
                                            imgName = 'flag_yellow';
                                            if (event.active) {
                                                node.alt = '<fmt:message key="common.alarmLevel.urgent"/>';
                                            } else {
                                                node.alt = '<fmt:message key="common.alarmLevel.urgent.rtn"/>';
                                            }
                                            break;
                                        case  3:
                                            if (event.active) {
                                                node.alt = '<fmt:message key="common.alarmLevel.critical"/>';
                                            } else {
                                                node.alt = '<fmt:message key="common.alarmLevel.critical.rtn"/>';
                                            }
                                            imgName = 'flag_orange';
                                            break;
                                        case  4:
                                            if (event.active) {
                                                node.alt = '<fmt:message key="common.alarmLevel.lifeSafety"/>';
                                            } else {
                                                node.alt = '<fmt:message key="common.alarmLevel.lifeSafety.rtn"/>';
                                            }
                                            imgName = 'flag_red';
                                            break;
                                        default :
                                            node.alt = alarmLevel;
                                            return  node;
                                    }
                                    node.src = 'images/' + imgName + (event.active ? '' : '_off') + '.png';
                                    node.title = node.alt;
                                    return node;
                                }
                            },
                            activeTimestamp: {
                                label: '<fmt:message key="common.time"/>',
                                resizable: true
                            },
                            message: {
                                label: "Message",
                                resizable: true,
                                formatter: function(msg) {
                                    return msg;
                                }
                            },
                            rtnTimestamp: {
                                label: '<fmt:message key="common.inactiveTime"/>',
                                renderCell: function(event, timestamp, default_node, options) {
                                    var node = domConstruct.create("div");

                                    if (event.active) {
                                        node.innerHTML = '<fmt:message key="common.active"/>';
                                        var img = domConstruct.create("img", null, node);
                                        img.src = "images/flag_white.png";
                                        img.title = '<fmt:message key="common.active"/>';
                                        /*                                    
                                         on(img, "click", function(evt){
                                         console.log("CKLICKED: " + evt);
                                         });
                                         */
                                    } else {
                                        if (!event.rtnApplicable) {
                                            node.innerHTML = '<fmt:message key="common.nortn"/>';
                                        } else {
                                            node.innerHTML = timestamp + ' - ' + event.rtnMessage;

                                        }
                                    }
                                    return node;
                                }
                            },
                            acknowledged: {
                                label: '',
                                renderCell: function(event, acknowledged, default_node, options) {
                                    var img = domConstruct.create("img");

                                    if (acknowledged) {
                                        img.src = "images/tick_off.png";
                                        img.alt = '<fmt:message key="events.acknowledged"/>';
                                    } else {
                                        img.src = "images/tick.png";
                                        img.alt = '<fmt:message key="events.acknowledge"/>';
                                    }
                                    img.title = img.alt;

                                    return img;
                                }
                            }
                        },
                        loadingMessage: "Loading data...",
                        noDataMessage: "No results found.",
                        selectionMode: "single", // for Selection; only select a single row at a time
                        //cellNavigation: false, // for Keyboard; allow only row-level keyboard navigation
                        pagingLinks: 1,
                        pagingTextBox: true,
                        firstLastArrows: true,
                        pageSizeOptions: [10, 25, 50, 100]
                    }, "watchListTable");

                    //TODO move smd to server ...
                    svc = new JsonService({
                        serviceUrl: 'events/rpc', // Adress of the RPC service end point
                        timeout: 1000,
                        strictArgChecks: true,
                        methods: [{
                                name: 'acknowledgePendingEvent',
                                parameters: [
                                    {
                                        name: 'id',
                                        type: 'INTEGER'
                                    }
                                ]
                            },
                            {
                                name: 'acknowledgeAllPendingEvents',
                                parameters: []
                            }
                        ]
                    });

                    grid.on("dgrid-error", function(event) {
                        console.log(event.error.message);
                    });

                    grid.on(".dgrid-cell:click", function(evt) {
                        var cell = grid.cell(evt);
                        var data = cell.row.data;
                        if (cell.column.field === 'acknowledged') {
                            if (!data.acknowledged) {
                                svc.acknowledgePendingEvent(data.id).then(function(result) {
                                    grid.setStore(new Memory({data: result}));
                                });
                            }
                        }
                    });

                    on(dom.byId("acknowledgeAllPendingEventsImg"), "click", function() {
                        svc.acknowledgeAllPendingEvents().then(function(result) {
                            grid.setStore(new Memory({data: result}));
                        });
                    });

                });

                myRestStore = new JsonRest({
                    target: "dstree/",
                    getChildren: function(object, onComplete, onError) {
                        this.query({parentId: object.id}).then(onComplete, onError);
                    },
                    mayHaveChildren: function(object) {
                        return object.nodeType === "PF";
                    },
                    getRoot: function(onItem, onError) {
                        this.get("root").then(onItem, onError);
                    },
                    getLabel: function(object) {
                        return object.name;
                    }
                });
                // Create the Tree.
                tree = new Tree({
                    model: myRestStore
                }, "dataPointTree");
                tree.startup();
            });

        </script>

        <div data-dojo-type="dijit/layout/BorderContainer" data-dojo-props="design: 'sidebar', gutters:true, liveSplitters:true" id="watchListContainer" >
            <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="splitter:true, region: 'left'"  style="width: 200px;">
           <div id="dataPointTree"></div>
            </div>
            <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="splitter:true, region:'top'">
            TOP
            </div>
            <div data-dojo-type="dijit/layout/ContentPane" data-dojo-props="region: 'center'">
           <div id="watchListTable"></div>
           </div>
        </div>
    
</jsp:body>
</tag:page>

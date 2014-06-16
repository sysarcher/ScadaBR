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

            #pointTreeContentPane{
                width: 200px; 
            }

            #watchListContainer {
                width: 100%;
                height: 100%;
            }

            .dgrid-cell {
                border: none;
            }


        </style>

        <script type="text/javascript">
            var watchlist = {
                grid: undefined,
                gridStore: undefined,
                tree: undefined,
                treeRestStore: undefined,
                init: function(pointTreeNode, watchListNode, watchlistId) {
                    watchlist.createTree(pointTreeNode);
                    watchlist.createTable(watchListNode, watchlistId);
                },
                fetchSelectedWatchList: function(watchlistId) {
                    require(["dojo/request", "dojo/store/Memory"], function(request, Memory) {

                        request("rest/watchlists/", {
                            query: {id: watchlistId},
                            handleAs: "json"
                        }).then(function(data) {
                            console.log("WL DATA: " + data);
                            watchlist.gridStore = new Memory({data: data.points});
                            watchlist.grid.setStore(watchlist.gridStore);
                        }, function(err) {
                            console.log("WL ERR: " + err);
                        }, function(evt) {
                            console.log("WL EVT: " + evt);
                        });
                    });
                },
                createTable: function(watchListNode, watchlistId) {
                    require([
                        "dojo/_base/declare",
                        "dgrid/OnDemandGrid",
                        "dgrid/Keyboard",
                        "dgrid/Selection",
                        "dojo/ready"
                    ], function(declare, OnDemandGrid, Keyboard, Selection, ready) {
                        ready(function() {
                            // Create a Grid instance using Pagination,
                            // referencing the store
                            watchlist.grid = new (declare([OnDemandGrid, Keyboard, Selection]))({
                                showHeader: false,
                                columns: {
                                    chartType: {
                                        label: "ChartType"
                                    },
                                    id: {
                                        label: "Id"
                                    },
                                    settable: {
                                        label: "Settable"
                                    },
                                    canonicalName: {
                                        label: "Name"
                                    },
                                    timestamp: {
                                        lable: "Timestamp"
                                    },
                                    value: {
                                        lable: "Value"
                                    }
                                },
                                loadingMessage: "Loading data...",
                                noDataMessage: "No results found.",
                                selectionMode: "single" // for Selection; only select a single row at a time
                                        //cellNavigation: false, // for Keyboard; allow only row-level keyboard navigation
                            }, watchListNode);
                            //init
                            watchlist.fetchSelectedWatchList(watchlistId);
                        });
                    });
                },
                createTree: function(pointTreeNode) {
                    require([
                        "dojo/store/JsonRest",
                        "dijit/Tree",
                        "dojo/domReady!"
                    ], function(JsonRest, Tree) {
                        watchlist.treeRestStore = new JsonRest({
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
                        watchlist.tree = new Tree({
                            model: watchlist.treeRestStore
                        }, pointTreeNode);
                        watchlist.tree.startup();
                    });
                }
            };

//INIT
            watchlist.init("dataPointTree", "watchListTable", ${selectedWatchList});

        </script>

        <dijit:sidebarBorderContainer gutters="true" liveSplitters="true" >
            <dijit:leftContentPane id="pointTreeContentPane" splitter="true" >
                <div id="dataPointTree"></div>
            </dijit:leftContentPane>
            <dijit:centerContentPane >
                <dijit:headlineLayoutContainer >
                    <dijit:topContentPane >
                        <dijit:selectFromMap id="watchListSelect" value="${selectedWatchList}" map="${watchLists}" />
                    </dijit:topContentPane>
                    <dijit:centerContentPane >
                        <div id="watchListTable"></div>
                    </dijit:centerContentPane>
                </dijit:headlineLayoutContainer>
            </dijit:centerContentPane>
        </dijit:sidebarBorderContainer>
    </jsp:body>
</tag:page>

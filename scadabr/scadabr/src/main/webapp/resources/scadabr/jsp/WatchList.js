define(["dojo/_base/declare",
    "dojo/_base/lang",
    "dijit/Tree",
    "dojo/store/JsonRest",
    "dojo/dnd/Source",
    "dgrid/OnDemandGrid",
    "dgrid/Keyboard",
    "dgrid/Selection",
    "dojo/request",
    "dojo/store/Memory",
    "dojo/store/Observable",
    "dijit/tree/dndSource",
    "dgrid/extensions/DnD",
    "dojo/rpc/JsonService"
], function(declare, lang, Tree, JsonRest, DnDSource, OnDemandGrid, Keyboard, Selection, request, Memory, Observable, dndSource, DnD, JsonService) {

    return declare(null, {
        pointsTreeStore: null,
        pointsTree: null,
        watchlistGrid: null,
        watchlistId: null,
        constructor: function(pointsTreeNode, watchListNode, watchlistId) {
            this.watchlistId = watchlistId;
            this._initSvc();
            this._initPointsTree(pointsTreeNode);
            this._initWatchListTable(watchListNode, watchlistId);
        },
        _initSvc: function() {
            this.svc = new JsonService({
                serviceUrl: 'rpc/watchlists.json', // Adress of the RPC service end point
                timeout: 1000,
                strictArgChecks: true,
                methods: [{
                        name: 'addPointToWatchlist',
                        parameters: [
                            {
                                name: 'watchListId',
                                type: 'INTEGER'
                            },
                            {
                                name: 'index',
                                type: 'INTEGER'
                            },
                            {
                                name: 'dataPointId',
                                type: 'INTEGER'
                            }
                        ]
                    }

                ]
            });
        },
        _initPointsTree: function(pointsTreeNode) {
            this.pointsTreeStore = new JsonRest({
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
            this.pointsTree = new Tree({
                model: this.pointsTreeStore,
                dndController: dndSource
            }, pointsTreeNode);
        },
        _initWatchListTable: function(watchListNode, watchlistId) {
            this.watchlistGrid = new (declare([OnDemandGrid, Keyboard, Selection, DnD]))({
                sort: "order",
                showHeader: false,
                store: new Observable(new Memory({data: null})),
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
                dndParams: {
                    allowNested: true, // also pick up indirect children w/ dojoDndItem class
                    self: this,
                    checkAcceptance: function(source, nodes) {
                        console.log("DND EXT DROP " + nodes);
                        return true;
                    },
                    onDropInternal: function(source, nodes, copy, target) {

                    },
                    onDropExternal: function(source, nodes, copy, target) {
                        var grid = this.grid;
                        var store = this.grid.store;
                        nodes.forEach(function(node) {
                            var i = source.getItem(node.id);
                            var d = i.data;
                            var a = i.type.indexOf("treeNode");
                            if (i.type.indexOf("treeNode") >= 0) {
                                this.self.addPointToWatchlist(this.self.watchlistId, 1, d.item.id);
                                console.log("Dropped TreeNode" + d.item.name);
                            } else if (i.type.indexOf("dgrid-row") >= 0) {
                                console.log("Dropped dgrid col" + d.canonicalName);

                            }
                        }, this);
                    },
                    loadingMessage: "Loading data...",
                    noDataMessage: "No results found.",
                    selectionMode: "single" // for Selection; only select a single row at a time
                            //cellNavigation: false, // for Keyboard; allow only row-level keyboard navigation
                }
            }, watchListNode);
            //    var wlTarget = new Target(watchListNode, { accept: true });
            //get initial list...
            this.fetchWatchList(watchlistId);
        },
        fetchWatchList: function(watchlistId) {
            request("rest/watchlists/", {
                query: {id: watchlistId},
                handleAs: "json"
            }).then(lang.hitch(this, function(data) {
                console.log("WL DATA: " + data);
                this.watchlistGrid.store.setData(data.points);
                this.watchlistGrid.refresh();
            }), function(err) {
                console.log("WL ERR: " + err);
            }, function(evt) {
                console.log("WL EVT: " + evt);
            });
        },
        addPointToWatchlist: function(watchlistId, index, dataPointId) {
            var grid = this.watchlistGrid;
            this.svc.addPointToWatchlist(watchlistId, index, dataPointId).then(function(result) {
                console.log("DataPoint Added CB: ", result);
                grid.store.setData(result.points);
                grid.refresh();
            });

        }

    });
});
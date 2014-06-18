define(["dojo/_base/declare",
    "dojo/_base/lang",
    "dijit/Tree",
    "dojo/store/JsonRest",
    "dgrid/OnDemandGrid",
    "dgrid/Keyboard",
    "dgrid/Selection",
    "dojo/request",
    "dojo/store/Memory"
], function(declare, lang, Tree, JsonRest, OnDemandGrid, Keyboard, Selection, request, Memory) {

    return declare(null, {
        pointsTreeStore: null,
        pointsTree: null,
        watchlistGrid: null,
        constructor: function(pointsTreeNode, watchListNode, watchlistId) {
            this._initPointsTree(pointsTreeNode);
            this._initWatchListTable(watchListNode, watchlistId);
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
                model: this.pointsTreeStore
            }, pointsTreeNode);
        },
        _initWatchListTable: function(watchListNode, watchlistId) {
            this.watchlistGrid = new (declare([OnDemandGrid, Keyboard, Selection]))({
                showHeader: false,
                store: new Memory({data: null}),
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
        }

    });
});
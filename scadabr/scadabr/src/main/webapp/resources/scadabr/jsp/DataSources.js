define(["dojo/_base/declare",
    "dijit/registry",
    "dijit/Tree",
    "dojo/store/JsonRest",
    "dojo/store/Observable",
    "dijit/Menu",
    "dijit/MenuItem",
    "dojo/rpc/JsonService",
    "dojo/ready"
], function (declare, registry, Tree, JsonRest, Observable, Menu, MenuItem, JsonService, ready) {

    return declare(null, {
        store: null,
        tree: null,
        treeMenu: null,
        svc: null,
        treeNodeDetailsWidget: null,
        constructor: function (dataSourcesTreeNode, treeNodeDetailsWidgetId) {
            this._initSvc();
            this._initStore();
            this._initDataSourceTree(dataSourcesTreeNode);
            this._initMenu(dataSourcesTreeNode);
            var _this = this;
            ready(function () {
                _this.treeNodeDetailsWidget = registry.byId(treeNodeDetailsWidgetId);
            });
        },
        _initStore: function () {
            this.store = new Observable(new JsonRest({
                target: "dataSources/dsTree/",
                getChildren: function (object, onComplete, onError) {
                    switch (object.nodeType) {
                        case "ROOT" :
                            this.query({}).then(onComplete, onError);
                            break;
                        default :
                            alert("Unknown Type: " + object.nodeType);
                    }
                },
                mayHaveChildren: function (object) {
                    switch (object.nodeType) {
                        case "ROOT":
                            return true;
                        default:
                            return false;
                    }
                },
                getRoot: function (onItem, onError) {
                    onItem({id: "root",
                        name: "DataSources",
                        nodeType: "ROOT"});
                },
                getLabel: function (object) {
                    return object.name;
                },
                //inserted manually to catch the aspect of Tree to get this working -- Observable looks wired to me ... at least JSONRest does not woirk out of the box ...
                onChange: function (/*dojo/data/Item*/ /*===== item =====*/) {
                },
                onChildrenChange: function (/*===== parent, newChildrenList =====*/) {
                },
                onDelete: function (/*dojo/data/Item*/ /*===== item =====*/) {
                }

            }));

        },
        _initDataSourceTree: function (dataSourcesTreeNode) {
            this.tree = new Tree({
                model: this.store,
                detailController: this,
                onClick: function (node) {
                    switch (node.nodeType) {
                        case "ROOT":
                            break;
                        case "DataSource":
                            this.detailController.treeNodeDetailsWidget.set("href", "dataSources/dataSource?id=" + node.id);
                            break;
                        default:
                            this.detailController.clearDetailViewId();
                    }
                }
            }, dataSourcesTreeNode);
        },
        _initMenu: function (dataSourcesTreeNode) {
            this.treeMenu = new Menu({
                targetNodeIds: [dataSourcesTreeNode]
            });
            var _store = this.store;
            var _tree = this.tree;
            var _svc = this.svc;
            this.treeMenu.addChild(new MenuItem({
                iconClass: "dijitIconAdd",
                label: "add",
                onClick: function () {
                    _svc.addDataSource("META").then(function (result) {
                        _store.getChildren(_tree.lastFocused.item, function (children) {
                            _store.onChildrenChange(_tree.lastFocused.item, children);
                        }, function (error) {
                            alert(error);
                        });
                    }, function (error) {
                        alert(error);
                    });
                }
            }));
            this.treeMenu.addChild(new MenuItem({
                label: "Delete DataSource",
                disabled: true
            }));
            this.treeMenu.startup();
        },
        _initSvc: function () {
            this.svc = new JsonService({
                serviceUrl: 'dataSources/rpc/', // Adress of the RPC service end point
                timeout: 1000,
                strictArgChecks: true,
                methods: [{
                        name: 'addDataSource',
                        parameters: [
                            {
                                name: 'type',
                                type: 'STRING'
                            }
                        ]
                    },
                    {
                        name: 'startDataSource',
                        parameters: []
                    },
                    {
                        name: 'stopDataSource',
                        parameters: []
                    }

                ]
            });
        },
        setPointId: function (id) {

        },
        setFolderId: function (id) {

        },
        clearDetailViewId: function () {

        }
    });
});
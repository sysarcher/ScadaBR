define(["dojo/_base/declare",
    "dojo/_base/lang",
    "dijit/Tree",
    "dojo/on",
    "dijit/registry",
    "dojo/store/JsonRest",
    "dojo/store/Observable",
    "dijit/Menu",
    "dijit/MenuItem",
    "dojo/rpc/JsonService"
], function (declare, lang, Tree, on, registry, JsonRest, Observable, Menu, MenuItem, JsonService) {

    return declare(null, {
        store: null,
        tree: null,
        treeMenu: null,
        constructor: function (dataSourcesTreeNode) {
            this._initStore();
            this._initDataSourceTree(dataSourcesTreeNode);
            this._initMenu(dataSourcesTreeNode);
        },
        _initStore: function () {
            this.store = new Observable(new JsonRest({
                target: "rest/dataSources/lazyTree/",
                getChildren: function (object, onComplete, onError) {
                    this.query({parentId: object.id}).then(onComplete, onError);
                },
                mayHaveChildren: function (object) {
                    return object.nodeType === "PF";
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
                    if (node.nodeType === "DP") {
                        this.detailController.setPointId(node.id)
                    } else if (node.nodeType === "PF") {
                        this.detailController.setFolderId(node.id)
                    } else {
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
            this.treeMenu.addChild(new MenuItem({
                iconClass: "dijitIconAdd",
                label: "add",
                onClick: function () {
                    _store.put({dsType: "META"}).then(function (object) {
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
        setPointId: function (id) {

        },
        setFolderId: function (id) {

        },
        clearDetailViewId: function () {

        },
    });
});
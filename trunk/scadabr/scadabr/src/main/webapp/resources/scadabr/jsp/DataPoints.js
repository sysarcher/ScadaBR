define(["dojo/_base/declare",
    "dijit/Tree",
    "dojo/store/JsonRest",
    "dojo/store/Observable",
    "dijit/registry",
    "dijit/Menu",
    "dijit/MenuItem",
    "dijit/TooltipDialog",
    "dijit/form/TextBox",
    "dojo/keys",
    "dijit/popup",
    "dijit/PopupMenuItem",
    "dojo/rpc/JsonService"
], function (declare, Tree, JsonRest, Observable, registry, Menu, MenuItem, TooltipDialog, TextBox, keys, popup, PopupMenuItem, JsonService) {

    return declare(null, {
        tree: null,
        store: null,
        treeMenu: null,
        nodeNameDialog: null,
        svc: null,
        dataTypes: null,
        constructor: function (treeNodeId, tabWidgetId, dataTypes, localizedKeys) {
            this.dataTypes = dataTypes;
            this._initSvc();
            this.store = new Observable(new JsonRest({
                target: "lazyConfigTree/dataPoints/",
                getChildren: function (object, onComplete, onError) {
                    this.query({parentId: object.id}).then(onComplete, onError);
                },
                mayHaveChildren: function (object) {
                    return object.nodeType === "PF";
                },
                getRoot: function (onItem, onError) {
                    this.get("root").then(onItem, onError);
                },
                getLabel: function (object) {
                    return object.name;
                },
                getIdentity: function (object) {
                    return object.nodeType + "_" + object.id;
                },
                //inserted manually to catch the aspect of Tree to get this working -- Observable looks wired to me ... at least JSONRest does not woirk out of the box ...
                onChange: function (/*dojo/data/Item*/ /*===== item =====*/) {
                },
                onChildrenChange: function (/*===== parent, newChildrenList =====*/) {
                },
                onDelete: function (/*dojo/data/Item*/ /*===== item =====*/) {
                }

            }));
            // Create the Tree.
            this.tree = new Tree({
                model: this.store,
                detailController: this,
                onClick: function (node) {
                    switch (node.nodeType) {
                        case "DP":
                            this.detailController.setPointId(node.id);
                            break;
                        case "PF":
                            this.detailController.setFolderId(node.id);
                            break;
                        default:
                            this.detailController.clearDetailViewId();
                    }
                }
            }, treeNodeId);

            this.selectedTab = null;
            this.tabViewWidget = null;
            this.dpId = -1;
            this.pfId = 0;
            this.cleanUpBeforeChanage = function () {
            };
            this.setUpAfterChange = function () {
                if (((this.dpId === -1) && (this.pfId <= 0)) || (this.selectedTab === null)) {
                    return;
                }
                this.selectedTab.set("href", this.selectedTab.contentUrl + "?id=" + (this.dpId !== -1 ? this.dpId : this.pfId));
            };
            this.setSelectedTab = function (tab) {
                this.cleanUpBeforeChanage();
                this.selectedTab = tab;
                if (this.pfId === -1) {
                    //curently filter out folders
                    this.setUpAfterChange();
                }
            };
            this.setPointId = function (id) {
                this.cleanUpBeforeChanage();
                this.dpId = id;
                this.pfId = -1;
                this.setUpAfterChange();
            };
            this.setFolderId = function (id) {
                this.cleanUpBeforeChanage();
                this.pfId = id;
                this.dpId = -1;
                //curently filter out folders
                this.selectedTab.set(null);
                //  this.setUpAfterChange();
            };
            this.clearDetailViewId = function () {
                this.cleanUpBeforeChanage();
                this.dpId = -1;
                this.pfId = -1;
                this.setUpAfterChange();
            }

            var detailController = this;
            require(["dojo/ready"],
                    function (ready) {
                        ready(function () {
                            detailController.tabViewWidget = registry.byId(tabWidgetId);
                            detailController.setSelectedTab(detailController.tabViewWidget.selectedChildWidget);
                            detailController.tabViewWidget.watch("selectedChildWidget", function (name, oval, nval) {
                                detailController.setSelectedTab(nval);
                            });
                        });
                    });

            this.nodeNameInput = new TextBox({
            }
            );


            this.nodeNameDialog = new TooltipDialog({
                content: new TextBox({
                    treeNode: null,
                    setTreeNode: function (treeNode) {
                        this.treeNode = treeNode;
                        this.set('value', this.treeNode.item.name);
                    },
                    onKeyUp: function (event) {
                        if (event.keyCode === keys.ESCAPE) {
                            popup.close(this.nodeNameDialog);
                        } else if (event.keyCode === keys.ENTER) {
                            popup.close(this.nodeNameDialog);
                            this.treeNode.item.name = this.get('value');
                            var _treeNode = this.treeNode;
                            switch (this.treeNode.item.nodeType) {
                                case "PF" :
                                    _svc.renamePointFolder(_treeNode.item.id, _treeNode.item.name).then(function (object) {
                                        _store.onChange(_treeNode.item);
                                        this.treeNode.focus();
                                    }, function (error) {
                                        alert(error);
                                    });
                                    break;
                                case "DP" :
                                    _svc.renameDataPoint(_treeNode.item.id, _treeNode.item.name).then(function (object) {
                                        _store.onChange(_treeNode.item);
                                        _treeNode.focus();
                                    }, function (error) {
                                        alert(error);
                                    });
                                    break;
                            }
                        }
                    }
                }),
                setTreeNode: function (treeNode) {
                    this.content.setTreeNode(treeNode);
                },
                focusInput: function () {
                    this.content.focus();
                },
            });


            this.treeMenu = new Menu({
                targetNodeIds: [treeNodeId]
            });
            var _store = this.store;
            var _tree = this.tree;
            var _nodeNameDialog = this.nodeNameDialog;
            var _svc = this.svc;
            this.treeMenu.addChild(new MenuItem({
                iconClass: "dijitIconEdit",
                label: localizedKeys['common.edit.reanme'],
                onClick: function () {
                    if (_tree.lastFocused === null) {
                        return;
                    }
                    _nodeNameDialog.setTreeNode(_tree.lastFocused);

                    popup.open({
                        popup: _nodeNameDialog,
                        around: _tree.lastFocused.contentNode
                    });
                    _nodeNameDialog.focusInput();
                }
            }));
            this.treeMenu.addChild(new MenuItem({
                iconClass: "dijitIconAdd",
                label: localizedKeys['common.edit.add'],
                onClick: function () {
                    _svc.addPointFolder(_tree.lastFocused.item.id, "New Folder").then(function (object) {
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
            var dpAddMenu = new Menu({});
            for (var i = 0; i < this.dataTypes.length; i++) {
                dpAddMenu.addChild(new MenuItem({
                    iconClass: "dsAddIcon",
                    label: this.dataTypes[i].label,
                    _dataType: this.dataTypes[i].key,
                    onClick: function () {
                        _svc.addDataPoint(_tree.lastFocused.item.id, this._dataType, this.label).then(function (result) {
                            _store.getChildren(_tree.lastFocused.item, function (children) {
                                _store.onChildrenChange(_tree.lastFocused.item, children);
                            }, function (error) {
                                alert(error);
                            });
                        }, function (error) {
                            alert(error);
                        });
                    }
                })
                        );
            }
            this.treeMenu.addChild(new PopupMenuItem({
                iconClass: "dsAddIcon",
                label: "Add DataPoint",
                popup: dpAddMenu
            }));

            this.treeMenu.addChild(new MenuItem({
                label: "Rename Folder",
                disabled: true
            }));
            this.treeMenu.addChild(new MenuItem({
                label: "Rename DataPoint",
                disabled: true
            }));
            this.treeMenu.startup();

        },
        _initSvc: function () {
            this.svc = new JsonService({
                serviceUrl: 'dataPoints/rpc/', // Adress of the RPC service end point
                timeout: 1000,
                strictArgChecks: true,
                methods: [{
                        name: 'addPointFolder',
                        parameters: [
                            {
                                name: 'parentFolderId',
                                type: "INT"
                            },
                            {
                                name: 'type',
                                type: 'STRING'
                            }
                        ]
                    },
                    {
                        name: 'deletePointFolder',
                        parameters: [{
                                name: 'id',
                                type: 'INT'
                            }]
                    },
                    {
                        name: 'renamePointFolder',
                        parameters: [
                            {
                                name: 'id',
                                type: 'INT'
                            },
                            {
                                name: 'name',
                                type: 'STRING'
                            }
                        ]
                    },
                    {
                        name: 'addDataPoint',
                        parameters: [
                            {
                                name: 'parentFolderId',
                                type: "INT"
                            },
                            {
                                name: 'dataType',
                                type: 'STRING'
                            },
                            {
                                name: 'name',
                                type: 'STRING'
                            }
                        ]
                    },
                    {
                        name: 'deleteDataPoint',
                        parameters: [{
                                name: 'id',
                                type: 'INT'
                            }]
                    },
                    {
                        name: 'renameDataPoint',
                        parameters: [
                            {
                                name: 'id',
                                type: 'INT'
                            },
                            {
                                name: 'name',
                                type: 'STRING'
                            }
                        ]
                    }
                ]
            });
        }

    });
});
define(["dojo/_base/declare",
    "dijit/Tree",
    "dojo/request",
    "dojo/dom",
    "dojo/store/JsonRest",
    "dojo/store/Observable",
    "dijit/registry",
    "dijit/Menu",
    "dijit/MenuItem",
    "dijit/TooltipDialog",
    "dijit/form/TextBox",
    "dojo/keys",
    "dijit/popup",
    "dijit/CheckedMenuItem",
    "dijit/MenuSeparator",
    "dijit/PopupMenuItem"
], function (declare, Tree, request, dom, JsonRest, Observable, registry, Menu, MenuItem, TooltipDialog, TextBox, keys, popup, CheckedMenuItem, MenuSeparator, PopupMenuItem) {

    return declare(null, {
        tree: null,
        store: null,
        treeMenu: null,
        nodeNameDialog: null,
        constructor: function (treeNodeId, tabWidgetId, localizedKeys) {
            this.store = new Observable(new JsonRest({
                target: "rest/pointHierarchy/",
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
                //inserted manually to catch the aspect of Tree to get this working -- Observable looks wired to me ... at least JSONRest does not woirk out of the box ...
                onChange: function (/*dojo/data/Item*/ /*===== item =====*/) {
                    // summary:
                    //		Callback whenever an item has changed, so that Tree
                    //		can update the label, icon, etc.   Note that changes
                    //		to an item's children or parent(s) will trigger an
                    //		onChildrenChange() so you can ignore those changes here.
                    // tags:
                    //		callback
                },
                onChildrenChange: function (/*===== parent, newChildrenList =====*/) {
                    // summary:
                    //		Callback to do notifications about new, updated, or deleted items.
                    // parent: dojo/data/Item
                    // newChildrenList: Object[]
                    //		Items from the store
                    // tags:
                    //		callback
                },
                onDelete: function (/*dojo/data/Item*/ /*===== item =====*/) {
                    // summary:
                    //		Callback when an item has been deleted.
                    //		Actually we have no way of knowing this with the new dojo.store API,
                    //		so this method is never called (but it's left here since Tree connects
                    //		to it).
                    // tags:
                    //		callback
                }

            }));
            // Create the Tree.
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
            }, treeNodeId);

            this.selectedTab = null;
            this.tabViewWidget = null;
            this.dpId = -1;
            this.pfId = 0;
            this.cleanUpBeforeChanage = function () {
                if (this.selectedTab !== null) {
                    var contentNode = dom.byId(this.selectedTab.contentId);
                    var formWidgets = registry.findWidgets(contentNode);
                    formWidgets.forEach(function (widget) {
                        widget.destroyRecursive();
                    });
                }
            };
            this.setUpAfterChange = function () {
                if (((this.dpId === -1) && (this.pfId <= 0)) || (this.selectedTab === null)) {
                    return;
                }

                var contentNode = dom.byId(this.selectedTab.contentId);

                request.get(this.selectedTab.contentUrl, {
                    query: {
                        id: this.dpId !== -1 ? this.dpId : this.pfId
                    }
                }).then(
                        function (response) {
                            contentNode.innerHTML = response;
                            var scripts = contentNode.getElementsByTagName("script");
                            for (var i = 0; i < scripts.length; i++) {
                                eval(scripts[i].innerHTML);
                            }
                        },
                        function (error) {
                            contentNode.innerHTML = "<div class=\"error\">" + error + "<div>";
                        }
                );
            };
            this.setSelectedTab = function (tab) {
                this.cleanUpBeforeChanage();
                this.selectedTab = tab;
                this.setUpAfterChange();
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
                this.setUpAfterChange();
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
//                style: "width: 300px;",
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
                            _store.put(this.treeNode.item);
                            _store.onChange(this.treeNode.item);
                            this.treeNode.focus();
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
                    _store.put({parentId: _tree.lastFocused.item.id, nodeType: "PF", name: "New Folder"}).then(function (object) {
                        _store.getChildren(_tree.lastFocused.item, function(children) {
                           _store.onChildrenChange(_tree.lastFocused.item, children);
                        }, function(error) {
                        alert(error);
                        });
                    }, function (error) {
                        alert(error);
                    });

                }
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

        }

    });
});
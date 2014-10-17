define(["dojo/_base/declare",
    "dijit/Tree",
    "dojo/request",
    "dojo/dom",
    "dojo/store/JsonRest",
    "dijit/registry",
    "dijit/Menu",
    "dijit/MenuItem",
    "dijit/CheckedMenuItem",
    "dijit/MenuSeparator",
    "dijit/PopupMenuItem"
], function (declare, Tree, request, dom, JsonRest, registry, Menu, MenuItem, CheckedMenuItem, MenuSeparator, PopupMenuItem) {

    return declare(null, {
        tree: null,
        store: null,
        treeMenu: null,
        constructor: function (treeNodeId, tabWidgetId) {
            this.store = new JsonRest({
                target: "rest/pointHierarchy",
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
                }
            });
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
                    
            this.treeMenu = new Menu({
                targetNodeIds: [treeNodeId]
            });
            this.treeMenu.addChild(new MenuItem({
                iconClass: "dijitEditorIcon dijitEditorIconCut",
                label: "Add Folder"
            }));
            this.treeMenu.addChild(new MenuItem({
                iconClass: "dijitEditorIcon dijitEditorIconCut",
                label: "Delete Folder",
                disabled: true
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
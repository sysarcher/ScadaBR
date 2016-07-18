define(["dojo/_base/declare",
    "dojo/request",
    "dojo/json",
    "dijit/Tree",
    "dijit/Menu",
    "dijit/MenuItem",
    "dijit/ConfirmDialog",
    "dijit/form/TextBox",
    "dojo/keys",
    "dijit/TooltipDialog",
    "dijit/popup",
    "dojo/i18n!scadabr/desktop/nls/messages"
], function (declare, request, json, Tree, Menu, MenuItem, ConfirmDialog, TextBox, keys, TooltipDialog, popup, messages) {
    var MyTreeNode = declare([Tree._TreeNode], {
        _setLabelAttr: {node: "labelNode", type: "innerHTML"}
    });
    return declare("scadabr/desktop/NavigationTree", [Tree], {
        region: 'left',
        splitter: 'true',
        baseHref: null,
        nodeMenues: null,
        postCreate: function () {
            this.nodeMenues = {};
            this.inherited(arguments);
        },
        destroy: function () {
            this.destroyNodeMenues();
        },
        _createTreeNode: function (args) {
            var result = new MyTreeNode(args);
            var nodeMenu = this.getNodeMenuFor(args.item.nodeType);
            nodeMenu.bindDomNode(result.domNode);
            return result;
        },
        getNodeMenuFor: function (nodeType) {
            var nodeMenu = this.nodeMenues[nodeType];
            if (nodeMenu === undefined) {
                switch (nodeType) {
                    case "ROOT":
                        nodeMenu = new Menu();
                        nodeMenu.addChild(this.createAddFolderMenuItem());
                        nodeMenu.addChild(this.createAddDataPointNodeMenu());
                        break;
                    case "POINT_FOLDER":
                        nodeMenu = new Menu();
                        nodeMenu.addChild(this.createAddFolderMenuItem());
                        nodeMenu.addChild(this.createAddDataPointNodeMenu());
                        nodeMenu.addChild(this.createRenameNodeMenuItem());
                        nodeMenu.addChild(this.createDeleteNodeMenuItem());
                        break;
                    case "DATA_POINT":
                        nodeMenu = new Menu();
                        nodeMenu.addChild(this.createRenameNodeMenuItem());
                        nodeMenu.addChild(this.createDeleteNodeMenuItem());
                        break;
                    default:
                        alert("Unknown node for NodeMenue: " + nodeType);
                }
                this.nodeMenues[nodeType] = nodeMenu;
            }
            return nodeMenu;
        },
        createAddFolderMenuItem: function () {
            var tree = this.tree;
            return new MenuItem({
                iconClass: "dijitIconAdd",
                label: messages['common.add'],
                onClick: function () {
                    var addFolderDialog = new ConfirmDialog({
                        title: "New Folder name localize ME!",
                        content: new TextBox({
                            value: "New Folder localize ME!",
                            name: "folderName",
                            onKeyUp: function (event) {
                                switch (event.keyCode) {
                                    case keys.ESCAPE:
                                        addFolderDialog.onCancel();
                                        break;
                                    case keys.ENTER:
                                        addFolderDialog._onSubmit();
                                        break;
                                }
                            }
                        }),
                        execute: function (formContents) {
                            tree.model.add({name: formContents.folderName, nodeType: "POINT_FOLDER", scadaBrType: "POINT_FOLDER"}, {parent: tree.selectedItem});
                        }
                    });
                    addFolderDialog.show();
                }

            });
        },
        createAddDataPointNodeMenu: function () {
            var tree = this.tree;
            var addDataPointNodeMenu = new Menu({});
            request("RPC/DataPoints/DataTypes", tree.model.prepareRequestParams.GET).then(function (dataTypes) {
                dataTypes = json.parse(dataTypes);
                var dataTypeKeys = Object.keys(dataTypes);
                for (var i = 0; i < dataTypeKeys.length; i++) {
                    addDataPointNodeMenu.addChild(new MenuItem({
                        iconClass: "dijitIconAdd",
                        label: "ADD DP: " + dataTypes[dataTypeKeys[i]],
                        _dataTypeKey: dataTypeKeys[i],
                        _dataTypeLabel: dataTypes[dataTypeKeys[i]],
                        onClick: function () {
                            var dataTypeKey = this._dataTypeKey;
                            var dataTypeLabel = this._dataTypeLabel;
                            var addDataPointDialog = new ConfirmDialog({
                                title: "New Data Point Name localize ME!",
                                content: new TextBox({
                                    value: dataTypeLabel,
                                    name: "dataPointName",
                                    onKeyUp: function (event) {
                                        switch (event.keyCode) {
                                            case keys.ESCAPE:
                                                addDataPointDialog.onCancel();
                                                break;
                                            case keys.ENTER:
                                                addDataPointDialog._onSubmit();
                                                break;
                                        }
                                    }
                                }),
                                execute: function (formContents) {
                                    tree.model.add({name: formContents.dataPointName, nodeType: "DATA_POINT", dataType: dataTypeKey, scadaBrType: ["DATA_POINT", dataTypeKey].join(".")}, {parent: tree.selectedItem});
                                }
                            });
                            addDataPointDialog.show();
                        }
                    }));
                }
            }, function (error) {
                alert(error);
            });
            return addDataPointNodeMenu;
        },
        createDeleteNodeMenuItem: function () {
            var tree = this.tree;
            return new MenuItem({
                iconClass: "dijitIconDelete",
                label: messages['common.delete'],
                onClick: function () {

                    switch (tree.selectedItem.nodeType) {
                        case "ROOT":
                            alert("Wrong node " + tree.selectedItem);
                            break;
                        default :
                            tree.model.delete(tree.selectedItem);
                    }
                }
            });
        },
        createRenameNodeMenuItem: function () {
            var tree = this.tree;
            return new MenuItem({
                iconClass: "dijitIconEdit",
                label: messages['common.rename'],
                onClick: function () {
                    if (tree.selectedNode === null) {
                        return;
                    }
                    var nodeNameDialog = tree.createNodeNameDialog();
                    popup.open({
                        popup: nodeNameDialog,
                        around: tree.selectedNode.contentNode
                    });
                    nodeNameDialog.focus();
                }

            });
        },
        createNodeNameDialog: function () {
            var tree = this.tree;
            var nodeNameDialog = new TooltipDialog({
                content: new TextBox({
                    value: tree.selectedItem.name,
                    onKeyUp: function (event) {
                        if (event.keyCode === keys.ESCAPE) {
                            popup.close(nodeNameDialog);
//TODO ?? check if nessecary                            nodeNameDialog.destroy();
                        } else if (event.keyCode === keys.ENTER) {
                            popup.close(nodeNameDialog);
                            var treeNode = tree.selectedNode;
                            var node = tree.selectedItem;
                            node.name = this.get('value');
                            tree.model.put(node);
                            treeNode.focus();
                        }
                    }
                })
            });
            return nodeNameDialog;
        },
        destroyNodeMenues: function () {

        },
        getIconClass: function (item, opened) {
            switch (item.nodeType) {
                case "ROOT":
                    return "dsIcon";
                case "PointFolder":
                    return (!item || this.model.mayHaveChildren(item)) ? (opened ? "dijitFolderOpened" : "dijitFolderClosed") : "dijitLeaf";
                case "DataPoint":
                    return item.enabled ? "plRunningIcon" : "plStoppedIcon";
                default:
                    return (!item || this.model.mayHaveChildren(item)) ? (opened ? "dijitFolderOpened" : "dijitFolderClosed") : "dijitLeaf";
            }
        },
        onClick: function (node) {
            var path = this.get('path');
            for (var i = 0; i < path.length; i++) {
                path[i] = path[i].id;
            }
            window.location = this.baseHref + "?path=" + path;
        }
    });
});

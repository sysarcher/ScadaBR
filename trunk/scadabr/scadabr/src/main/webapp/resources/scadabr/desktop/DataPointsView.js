define(["dojo/_base/declare",
    "scadabr/desktop/TreeModel",
    "scadabr/desktop/NavigationTree",
    "dojo/request",
    "dojo/json",
    "dijit/Menu",
    "dijit/MenuItem",
    "dijit/TooltipDialog",
    "dijit/ConfirmDialog",
    "dijit/form/TextBox",
    "dojo/keys",
    "dijit/popup",
    "dijit/PopupMenuItem",
    "dijit/layout/BorderContainer",
    "dijit/layout/ContentPane",
    "dojo/i18n!scadabr/desktop/nls/messages"
], function (declare, TreeModel, NavigationTree, request, json, Menu, MenuItem, TooltipDialog, ConfirmDialog, TextBox, keys, popup, PopupMenuItem, BorderContainer, ContentPane, messages) {

    return declare("scadabr/desktop/DataPointsView", [BorderContainer], {
        gutters: true,
        liveSplitters: true,
        tree: null,
        detailView: null,
        model: null,
        dataTypes: [],
        restBaseUrl: "REST/",
        postCreate: function () {
            this.inherited(arguments);

            this.model = new TreeModel("REST/");
            this.tree = new NavigationTree({model: this.model});
            this.addChild(this.tree);

            this._initTreeNodeMenues();
            this._initDetailViewModel();
        },
        _initDetailViewModel: function () {
            this.detailView = new ContentPane({region: 'center'});
            this.addChild(this.detailView);

            this.tree.set('path', ['ROOT']);

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
                this.selectedTab.set("href", this.selectedTab.contentUrl + "?id=" + (this.dpId !== -1 ? this.dpId : this.pfId)).then(function (succ) {
                    alert("Succ" + succ)
                }, function (err) {
                    alert("ERR:" + err);
                });
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
            /*TODO            require(["dojo/ready"],
             function (ready) {
             ready(function () {
             detailController.tabViewWidget = registry.byId(tabWidgetId);
             detailController.setSelectedTab(detailController.tabViewWidget.selectedChildWidget);
             detailController.tabViewWidget.watch("selectedChildWidget", function (name, oval, nval) {
             detailController.setSelectedTab(nval);
             });
             });
             });
             */
        },
        _initTreeNodeMenues: function () {
//            this.tree.rootNodeMenu.addChild(addFolderMenuItem);
//TODO            this.tree.pointFolderNodeMenu.addChild(addFolderMenuItem);
//            this.tree.pointFolderNodeMenu.addChild(editMenuItem);
//            this.tree.pointFolderNodeMenu.addChild(deleteNodeMenuItem);
//            this.tree.dataPointNodeMenu.addChild(editMenuItem);
//            this.tree.dataPointNodeMenu.addChild(deleteNodeMenuItem);

            var dpAddMenu = new Menu({});
            for (var i = 0; i < this.dataTypes.length; i++) {
                dpAddMenu.addChild(new MenuItem({
                    dataPoints: this,
                    iconClass: "dsAddIcon",
                    label: this.dataTypes[i].label,
                    _dataType: this.dataTypes[i],
                    onClick: function () {
                        var selectedItem = this.dataPoints.tree.selectedItem;
                        var url = this.dataPoints.restBaseUrl;
                        var model = this.dataPoints.model;
                        var dataType = this._dataType;
                        var addDataPointDialog = new ConfirmDialog({
                            title: "New Data Point Name localize ME!",
                            content: new TextBox({
                                value: this.label,
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
                                switch (selectedItem.nodeType) {
                                    case "ROOT":
                                        break;
                                    case "POINT_FOLDER":
                                        url = url + selectedItem.id + "/children";
                                        break;
                                    default :
                                        alert("Wrong node " + selectedItem);
                                        return;
                                }
                                request(url, {
                                    handleAs: "json",
                                    method: "POST",
                                    headers: {
                                        Accept: "application/json",
                                        "Content-Type": "application/json"
                                    },
                                    data: json.stringify({name: formContents.dataPointName, nodeType: "DATA_POINT", dataType: dataType.key, scadaBrType: ["DATA_POINT", dataType.key].join(".")})
                                }).then(function (object) {
                                    model.getChildren(selectedItem, function (children) {
                                        model.onChildrenChange(selectedItem, children);
                                    }, function (error) {
                                        alert(error);
                                    });
                                }, function (error) {
                                    alert(error);
                                });
                            }
                        });
                        addDataPointDialog.show();

                        /*                        _svc.addDataPoint(_tree.lastFocused.item.id, this._dataType, this.label).then(function (result) {
                         _store.getChildren(_tree.lastFocused.item, function (children) {
                         _store.onChildrenChange(_tree.lastFocused.item, children);
                         }, function (error) {
                         alert(error);
                         });
                         }, function (error) {
                         alert(error);
                         });
                         */
                    }
                }));
            }
            /*
             this.tree.pointFolderNodeMenu.addChild(new PopupMenuItem({
             iconClass: "dsAddIcon",
             label: "Add DataPoint",
             popup: dpAddMenu
             
             }));
             this.tree.pointFolderNodeMenu.addChild(new MenuItem({
             label: "Rename Folder",
             disabled: true
             
             }));
             this.tree.dataPointNodeMenu.addChild(new MenuItem({
             label: "Rename DataPoint",
             disabled: true
             
             }));
             */
        }
    });
});
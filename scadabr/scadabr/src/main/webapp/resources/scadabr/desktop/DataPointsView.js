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
        baseHref: null,
        dataTypes: [],
        restBaseUrl: "REST/",
        postCreate: function () {
            this.inherited(arguments);

            this.model = new TreeModel("REST/");
            this.tree = new NavigationTree({model: this.model, baseHref: this.baseHref});
            this.tree.set('path', ['ROOT']);
            this.addChild(this.tree);

            this._initDetailViewModel();
        },
        setTreePath: function (path) {
            this.tree.set('path', path);
            //TODO set DetailView
        },
        _initDetailViewModel: function () {
            this.detailView = new ContentPane({region: 'center'});
            this.addChild(this.detailView);
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
        }
    });
});
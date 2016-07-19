define(["dojo/_base/declare",
    "scadabr/desktop/TreeModel",
    "scadabr/desktop/NavigationTree",
    "dijit/layout/BorderContainer",
    "dijit/layout/ContentPane",
    "dijit/layout/TabContainer",
    "dojo/i18n!scadabr/desktop/nls/messages"
], function (declare, TreeModel, NavigationTree, BorderContainer, ContentPane, TabContainer, messages) {

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
            this.tree.startup();
            this.addChild(this.tree);

            this._initDetailViewModel();
        },
        setCurrentId: function (id) {
            this.tree.selectNode(id);
            //TODO set DetailView
        },
        _initDetailViewModel: function () {
            this.detailView = new TabContainer({region: 'center'});
            this.detailView.chartDataView = new ContentPane({title: "Show data (chart)", selected: true});
            this.detailView.addChild(this.detailView.chartDataView);
            this.detailView.tableDataView = new ContentPane({title: "Show data (table)"});
            this.detailView.addChild(this.detailView.tableDataView);
            this.detailView.editDataView = new ContentPane({title: "Edit"});
            this.detailView.addChild(this.detailView.editDataView);
            this.detailView.eventsDataView = new ContentPane({title: "Point Events and Notes"});
            this.detailView.addChild(this.detailView.eventsDataView);
            this.detailView.usageDataView = new ContentPane({title: "Point Usage"});
            this.detailView.addChild(this.detailView.usageDataView);
            this.addChild(this.detailView);
        }
    });
});
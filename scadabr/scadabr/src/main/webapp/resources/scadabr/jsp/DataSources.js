define(["dojo/_base/declare",
    "dojo/_base/lang",
    "dijit/Tree",
    "dojo/on",
    "dijit/registry",
    "dojo/store/JsonRest",
    "dojo/rpc/JsonService"
], function(declare, lang, Tree, on, registry, JsonRest, JsonService) {

    return declare(null, {
        dataSourcesStore: null,
        dataSourcesTree: null,
        constructor: function(dataSourceTreeNode) {
            this._initSvc();
            this._initDataSourceTree(dataSourceTreeNode);
        },
        _initSvc: function() {
            this.svc = new JsonService({
                serviceUrl: 'rpc/datasources/', // Adress of the RPC service end point
                timeout: 1000,
                strictArgChecks: true,
                methods: [{
                        name: 'addDataSource',
                        parameters: [
                            {
                                name: 'dataSourceType'
                            }
                        ]
                    },
                    {
                        name: 'removeDataSource',
                        parameters: []
                    }

                ]
            });
        },
        _initDataSourceTree: function(dataSourcesTreeNode) {
            this.dataSourcesStore = new JsonRest({
                target: "rest/dataSources/lazyTree/",
                getChildren: function(object, onComplete, onError) {
                    switch (object.nodeType) {
                        case "ROOT" : 
                    this.query({}).then(onComplete, onError);
                break;
            default: onComplete({}); 
        }
//                    this.query({parentId: object.id}).then(onComplete, onError);
                },
                mayHaveChildren: function(object) {
                    switch (object.nodeType) {
                        case "ROOT" : return true;
                        default: return false;
                    }
                },
                getRoot: function(onItem, onError) {
                    onItem({id:"root",
                        name:"DataSources",
                    nodeType:"ROOT"});
                },
                getLabel: function(object) {
                    return object.name;
                }
            });
            // Create the Tree.
            this.dataSourcesTree = new Tree({
                model: this.dataSourcesStore
            }, dataSourcesTreeNode);
        },
        wireEvents: function(btnAckAll) {
            on(registry.byId(btnAckAll).domNode, "click", lang.hitch(this, function() {
                this.svc.acknowledgeAllPendingEvents().then(lang.hitch(this, function(result) {
                    this.grid.store.setData(result);
                    this.grid.refresh();
                }));
            }));
            this.grid.on("dgrid-error", function(event) {
                console.log(event.error.message);
            });
        }
    });
});
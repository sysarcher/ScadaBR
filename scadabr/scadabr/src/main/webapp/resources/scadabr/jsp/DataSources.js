define(["dojo/_base/declare",
    "dojo/_base/lang",
    "dojo/on",
    "dijit/registry",
    "dojo/dom-construct",
    "dojo/request",
    "dojo/store/Memory",
    "dgrid/Grid",
    "dgrid/extensions/Pagination",
    "dgrid/Keyboard",
    "dgrid/Selection",
    "dijit/form/Button",
    "dojo/rpc/JsonService",
    "dojo/i18n!scadabr/nls/events",
    "dojo/i18n!scadabr/nls/common"
], function(declare, lang, on, registry, domConstruct, request, Memory, Grid, Pagination, Keyboard, Selection, Button, JsonService, events, common) {

    return declare(null, {
        constructor: function(dataSourceTableNode, dataSourceTypesSelect, addDataSourceBtn) {
            this._initSvc();
            this._initDataSourceTable(dataSourceTableNode);
        },
        _initSvc: function() {
            this.svc = new JsonService({
                serviceUrl: 'rpc/datasources.json', // Adress of the RPC service end point
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
        _initDataSourceTable: function(dataSourceTableNode) {
            this.grid = new (declare([Grid, Pagination, Keyboard, Selection]))({
                store: new Memory(),
                columns: {
                    name: {
                        label: "Name"
                    },
                    typeKey: {
                        label: 'TypeKey'
                },
                                    id: {
                        label: '',
                        renderCell: lang.hitch(this, function(dataSource, dataSourceId, default_node, options) {
                            var myIconClass;
                            var myLabel;
                            myIconClass = 'scadaBrDeleteIcon';
                            myLabel = "Edit DataSource";

                            var btnAck = new Button({
                                myObj: this,
                                dataSourceId: dataSourceId,
                                showLabel: false,
                                iconClass: myIconClass,
                                label: myLabel,
                                onClick: function() {
                                    console.log("BTN Edit THIS: ", this);
                                    window.location = "editDataSources/editFhz.shtml?id=1";
                                    
                                    //this.myObj.editDataSource(this.dataSourceId);
                                }
                            }, default_node.appendChild(document.createElement("div")));
                            btnAck._destroyOnRemove = true;

                        })
                    }

            },
                loadingMessage: "Loading data...",
                noDataMessage: "No results found.",
                selectionMode: "single", // for Selection; only select a single row at a time
                //cellNavigation: false, // for Keyboard; allow only row-level keyboard navigation
                pagingLinks: 1,
                pagingTextBox: true,
                firstLastArrows: true,
                pageSizeOptions: [10, 25, 50, 100]
            }, dataSourceTableNode);
            request("rest/dataSources/", {
                handleAs: "json"
            }).then(lang.hitch(this, function(response) {
                this.grid.store.setData(response);
                this.grid.refresh();
            }));
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
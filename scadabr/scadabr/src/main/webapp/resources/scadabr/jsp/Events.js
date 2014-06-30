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
        constructor: function(pendingAlarmsTableNode) {
            this._initSvc();
            this._initPendingAlarmsTable(pendingAlarmsTableNode);
        },
        _initSvc: function() {
            this.svc = new JsonService({
                serviceUrl: 'rpc/events.json', // Adress of the RPC service end point
                timeout: 1000,
                strictArgChecks: true,
                methods: [{
                        name: 'acknowledgePendingEvent',
                        parameters: [
                            {
                                name: 'id',
                                type: 'INTEGER'
                            }
                        ]
                    },
                    {
                        name: 'acknowledgeAllPendingEvents',
                        parameters: []
                    }

                ]
            });
        },
        _initPendingAlarmsTable: function(pendingAlarmsTableNode) {
            this.grid = new (declare([Grid, Pagination, Keyboard, Selection]))({
                store: new Memory(),
                columns: {
                    id: {
                        label: events.id
                    },
                    alarmLevel: {
                        label: common.alarmLevel,
                        renderCell: function(event, alarmLevel, default_node, options) {
                            var node = domConstruct.create("img");
                            var imgName;
                            switch (alarmLevel) {
                                case 1:
                                    imgName = 'flag_blue';
                                    if (event.active) {
                                        node.alt = common.alarmLevel_info;
                                    } else {
                                        node.alt = common.alarmLevel_info_rtn;
                                    }
                                    break;
                                case  2:
                                    imgName = 'flag_yellow';
                                    if (event.active) {
                                        node.alt = common.alarmLevel_urgent;
                                    } else {
                                        node.alt = common.alarmLevel_urgent_rtn;
                                    }
                                    break;
                                case  3:
                                    if (event.active) {
                                        node.alt = common.alarmLevel.critical;
                                    } else {
                                        node.alt = common.alarmLevel_critical_rtn;
                                    }
                                    imgName = 'flag_orange';
                                    break;
                                case  4:
                                    if (event.active) {
                                        node.alt = common.alarmLevel_lifeSafety;
                                    } else {
                                        node.alt = common.alarmLevel_lifeSafety_rtn;
                                    }
                                    imgName = 'flag_red';
                                    break;
                                default :
                                    node.alt = alarmLevel;
                                    return  node;
                            }
                            node.src = 'images/' + imgName + (event.active ? '' : '_off') + '.png';
                            node.title = node.alt;
                            return node;
                        }
                    },
                    activeTimestamp: {
                        label: common.time,
                        resizable: true
                    },
                    message: {
                        label: "Message",
                        resizable: true,
                        formatter: function(msg) {
                            return msg;
                        }
                    },
                    rtnTimestamp: {
                        label: common.inactiveTime,
                        renderCell: function(event, timestamp, default_node, options) {
                            var node = domConstruct.create("div");
                            if (event.active) {
                                node.innerHTML = common.active;
                                var img = domConstruct.create("img", null, node);
                                img.src = "images/flag_white.png";
                                img.title = common.active;
                            } else {
                                if (!event.rtnApplicable) {
                                    node.innerHTML = common.nortn;
                                } else {
                                    node.innerHTML = timestamp + ' - ' + event.rtnMessage;
                                }
                            }
                            return node;
                        }
                    },
                    acknowledged: {
                        label: '',
                        renderCell: lang.hitch(this, function(event, acknowledged, default_node, options) {
                            var myIconClass;
                            var myLabel;
                            if (acknowledged) {
                                myIconClass = 'scadaBrCantDoActionIcon';
                                myLabel = events.acknowledged;
                            } else {
                                myIconClass = 'scadaBrDoActionIcon';
                                myLabel = events.acknowledge;
                            }

                            var btnAck = new Button({
                                myObj: this,
                                eventId: event.id,
                                showLabel: false,
                                iconClass: myIconClass,
                                label: myLabel,
                                onClick: function() {
                                    console.log("BTN ACK THIS: ", this);
                                    this.myObj.svc.acknowledgePendingEvent(this.eventId).then(lang.hitch(this.myObj, function(result) {
                                        console.log("BTN ACK CB: ", this);
                                        this.grid.store.setData(result);
                                        this.grid.refresh();
                                    }));
                                }
                            }, default_node.appendChild(document.createElement("div")));
                            btnAck._destroyOnRemove = true;
                            var btnSilence = new Button({
                                eventId: event.id,
                                label: "Sil",
                                iconClass: 'scadaBrDoActionIcon',
                                showLabel: true
                            }, default_node.appendChild(document.createElement("div")));
                            btnSilence._destroyOnRemove = true;
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
            }, pendingAlarmsTableNode);
            request("events/", {
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
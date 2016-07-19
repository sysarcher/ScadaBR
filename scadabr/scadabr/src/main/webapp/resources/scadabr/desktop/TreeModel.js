define(["dojo/_base/declare",
    "dojo/request",
    "dojo/json",
    "dijit/tree/model",
    "dojo/i18n!scadabr/desktop/nls/messages"
], function (declare, request, json, model, messages) {

    var prepareRequestParams = function (method, data) {
        var result = {
            handleAs: "json",
            method: method,
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json"
            }
        };
        if (data !== undefined) {
            result.data = json.stringify(data);
        }
        return result;
    };

    prepareRequestParams.GET = function () {
        return prepareRequestParams("GET");
    };

    prepareRequestParams.PUT = function (data) {
        return prepareRequestParams("PUT", data);
    };

    prepareRequestParams.POST = function (data) {
        return prepareRequestParams("POST", data);
    };

    prepareRequestParams.DELETE = function () {
        return prepareRequestParams("DELETE");
    };

    return declare("scadabr/desktop/TreeModel", [model], {
        ROOT: {id: "ROOT", name: "PointFolders", nodeType: "ROOT"},
        prepareRequestParams: prepareRequestParams,
        restBaseUrl: null,
        constructor: function (restBaseUrl) {
            this.inherited(arguments);
            this.restBaseUrl = restBaseUrl;
        },
        getIdentity: function (object) {
            return object.id;
        },
        getChildren: function (object, onComplete, onError) {
            switch (object.nodeType) {
                case "ROOT":
                    request(this.restBaseUrl + "pointFolders/children", prepareRequestParams.GET()).then(onComplete, onError);
                    break;
                case "POINT_FOLDER":
                    request(this.restBaseUrl + object.id + "/children", prepareRequestParams.GET()).then(onComplete, onError);
                    break;
                default:
                    alert("No children for: " + object);
            }
        },
        mayHaveChildren: function (object) {
            return object.nodeType === "POINT_FOLDER" || object.id === "ROOT";
        },
        getRoot: function (onItem, onError) {
            onItem(this.ROOT);
        },
        getLabel: function (object) {
            return object.name;
        },
        add: function (object, options) {
            var self = this;
            var url;
            if (options.parent.id === "ROOT") {
                url = this.restBaseUrl;
            } else {
                url = this.restBaseUrl + options.parent.id + "/children";
            }
            request(url, prepareRequestParams.POST(object)).then(function (objectFromServer) {
                self.getChildren(options.parent, function (children) {
                    self.onChildrenChange(options.parent, children);
                }, function (error) {
                    alert(error);
                });
            }, function (error) {
                alert(error);
            });

        },
        put: function (object) {
            var self = this;
            request(this.restBaseUrl, prepareRequestParams.PUT(object)).then(function (objectFromServer) {
                self.onChange(objectFromServer);
            }, function (error) {
                alert(error);
            });
            return Promise;
        },
        refresh: function (id) {
            var self = this;
            request(this.restBaseUrl + id, prepareRequestParams.GET()).then(function (objectFromServer) {
                self.onChange(objectFromServer);
            }, function (error) {
                alert(error);
            });
            return Promise;
        },
        delete: function (object) {
            var self = this;
            request(this.restBaseUrl + object.id, prepareRequestParams.DELETE()).then(function () {
                self.onDelete(object);
            }, function (error) {
                alert(error);
            });
        },
        fetchTreePathOfId: function (id) {
            return request("RPC/DataPoints/" + id + "/treePath", prepareRequestParams.GET());
        },
        //inserted manually to catch the aspect of Tree to get this working -- Observable looks wired to me ... at least JSONRest does not woirk out of the box ...
        onChange: function (/*dojo/data/Item*/ /*===== item =====*/) {
        },
        onChildrenChange: function (/*===== parent, newChildrenList =====*/) {
        },
        onDelete: function (/*dojo/data/Item*/ /*===== item =====*/) {
        }

    });
});
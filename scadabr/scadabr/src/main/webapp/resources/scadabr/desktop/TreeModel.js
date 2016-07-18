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
        if (data !== "undefined") {
            result.data= json.stringify(data);
        }
        return result;
    };
    
    prepareRequestParams.GET = function() {
        return prepareRequestParams("GET");
    };
    
    prepareRequestParams.PUT = function(data) {
        return prepareRequestParams("PUT", data);
    };
    
    prepareRequestParams.POST = function(data) {
        return prepareRequestParams("POST", data);
    };
    
    return declare("scadabr/desktop/TreeModel", [model], {
        ROOT: {id: "ROOT", name: "PointFolders", nodeType: "ROOT"},
        restBaseUrl: null,
        constructor: function(restBaseUrl) {
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
        //inserted manually to catch the aspect of Tree to get this working -- Observable looks wired to me ... at least JSONRest does not woirk out of the box ...
        onChange: function (/*dojo/data/Item*/ /*===== item =====*/) {
        },
        onChildrenChange: function (/*===== parent, newChildrenList =====*/) {
        },
        onDelete: function (/*dojo/data/Item*/ /*===== item =====*/) {
        }

    });
});
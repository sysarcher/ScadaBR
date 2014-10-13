define(["dojo/_base/declare",
    "dijit/Tree",
    "dojo/request",
    "dojo/dom",
    "dojo/store/JsonRest"
], function (declare, Tree, request, dom, JsonRest) {

    return declare(null, {
        tree: null,
        store: null,
        constructor: function (parentNode, dataPointEditNode) {
            this.store = new JsonRest({
                target: "rest/pointHierarchy/",
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
                onClick: function (node) {
                    //TODO Use a ContentPane ???
                    var resultDiv = dom.byId(dataPointEditNode);
                    // DataPoint node??
                    if (node.nodeType !== "DP") {
                        this.cleanNode(resultDiv);
                        resultDiv.innerHTML = null;
                        return;
                    }
                    var cleanNode = this.cleanNode; // Todo or use hitch ???
                    // Request the html fragment
                    request.get("dataPointDetails/editCommonProperties", {
                        query: {
                            id: node.id,
                        }
                    }).then(
                            function (response) {
                                cleanNode(resultDiv);
                                resultDiv.innerHTML = response;
                                require(["dojo/parser"], function (parser) {
                                    parser.parse(resultDiv).then(function () {
                                        var scripts = resultDiv.getElementsByTagName("script");
                                        for (var i = 0; i < scripts.length; i++) {
                                            eval(scripts[i].innerHTML);
                                        }
                                    });
                                });
                            },
                            function (error) {
                                cleanNode(resultDiv);
                                // Display the error returned
                                resultDiv.innerHTML = "<div class=\"error\">" + error + "<div>";
                            }
                    );
                },
                cleanNode: function (node) {
                    //Destroy the dijit widgets
                    require(["dijit/registry"], function (registry) {
                        var formWidgets = registry.findWidgets(node);
                        formWidgets.forEach(function (widget) {
                            widget.destroyRecursive();
                        });
                    });
                }
            }, parentNode);
        }
    });
});
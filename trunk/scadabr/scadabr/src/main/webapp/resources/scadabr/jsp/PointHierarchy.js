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
                    var resultDiv = dom.byId(dataPointEditNode);
                    // DataPoint node??
                    if (node.nodeType !== "DP") {
                        resultDiv.innerHTML = null;
                        return;
                    }

                    // Request the text file
                    request.get("pointEdit/common?id=" + node.id).then(
                            function (response) {
                                // Display the text file content
                                resultDiv.innerHTML = response;
                            },
                            function (error) {
                                // Display the error returned
                                resultDiv.innerHTML = "<div class=\"error\">" + error + "<div>";
                            }
                    );
                }

            }, parentNode);
        }
    });
});
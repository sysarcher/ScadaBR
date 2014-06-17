define(["dojo/_base/declare",
    "dijit/Tree",
    "dojo/store/JsonRest"
], function(declare, Tree, JsonRest) {

    return declare(null, {
        tree: null,
        store: null,
        constructor: function(parentNode) {
            this.store = new JsonRest({
                target: "dstree/",
                getChildren: function(object, onComplete, onError) {
                    this.query({parentId: object.id}).then(onComplete, onError);
                },
                mayHaveChildren: function(object) {
                    return object.nodeType === "PF";
                },
                getRoot: function(onItem, onError) {
                    this.get("root").then(onItem, onError);
                },
                getLabel: function(object) {
                    return object.name;
                }
            });
            // Create the Tree.
            this.tree = new Tree({
                model: this.store
            }, parentNode);
        }
    });

});
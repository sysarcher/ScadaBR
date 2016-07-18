define(["dojo/_base/declare",
    "dijit/Tree",
    "dijit/Menu",
    "dojo/i18n!scadabr/desktop/nls/messages"
], function (declare, Tree, Menu, messages) {
    var MyTreeNode = declare([Tree._TreeNode], {
        _setLabelAttr: {node: "labelNode", type: "innerHTML"}
    });

    return declare("scadabr/desktop/NavigationTree", [Tree], {
        region: 'left',
        splitter: 'true',
        rootNodeMenu: null,
        pointFolderNodeMenu: null,
        dataPointNodeMenu: null,
        postCreate: function () {
            this.rootNodeMenu = new Menu();
            this.pointFolderNodeMenu = new Menu();
            this.dataPointNodeMenu = new Menu();
            this.inherited(arguments);
        },
        startup: function () {
            this.inherited(arguments);
            this.rootNodeMenu.startup();
            this.pointFolderNodeMenu.startup();
            this.dataPointNodeMenu.startup();
        },
        destroy: function () {
            this.rootNodeMenu.destroy();
            this.pointFolderNodeMenu.destroy();
            this.dataPointNodeMenu.destroy();
            this.inherited(arguments);
        },
        _createTreeNode: function (args) {
            var result = new MyTreeNode(args);
            switch (args.item.nodeType) {
                case "ROOT":
                    this.rootNodeMenu.bindDomNode(result.domNode);
                    break;
                case "PointFolder":
                    this.pointFolderNodeMenu.bindDomNode(result.domNode);
                    break;
                case "DataPoint":
                    this.dataPointNodeMenu.bindDomNode(result.domNode);
                    break;
                default:
            }
            return result;
        },
        getIconClass: function (item, opened) {
            switch (item.nodeType) {
                case "ROOT":
                    return "dsIcon";
                case "PointFolder":
                    return (!item || this.model.mayHaveChildren(item)) ? (opened ? "dijitFolderOpened" : "dijitFolderClosed") : "dijitLeaf";
                case "DataPoint":
                    return item.enabled ? "plRunningIcon" : "plStoppedIcon";
                default:
                    return (!item || this.model.mayHaveChildren(item)) ? (opened ? "dijitFolderOpened" : "dijitFolderClosed") : "dijitLeaf";
            }
        },
        onClick: function (node) {
            window.location = window.location.href + node.nodeType + "?" + "id=" + node.if;
        }
    });
});

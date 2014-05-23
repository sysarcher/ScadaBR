<%--
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.serotonin.mango.Common"%>
<tag:page >
    <script type="text/javascript">
        var selectedFolderNode;
        var tree;
        var myRestStore;

        window.onload = function() {

            require([
                "dojo/store/JsonRest",
                "dijit/Tree",
                "dojo/domReady!"
            ], function(JsonRest, Tree) {

                myRestStore = new JsonRest({
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
                tree = new Tree({
                    model: myRestStore
                }, "treeDiv");
                tree.startup();
            });
        }

    </script>

    <table>
        <tr>
            <td valign="top">
                <div class="borderDivPadded">
                    <table width="100%">
                        <tr>
                            <td>
                                <span class="smallTitle"><fmt:message key="pointHierarchy.hierarchy"/></span>
                                <tag:help id="pointHierarchy"/>
                            </td>
                            <td align="right">
                                <tag:img png="folder_add" title="common.add" onclick="showRoot()"/>
                                <tag:img png="save" title="common.save" onclick="save()"/>
                            </td>
                        </tr>
                        <tr><td class="formError" id="errorMessage"></td></tr>
                    </table>
                    <div id="treeDiv"></div>
                    <!--TODO make this work ... div data-dojo-type="dijit/Tree" id="myTree" data-dojo-props="model: myRestStore"></div-->
                </div>
            </td>

            <td valign="top">
                <div id="folderEditDiv" class="borderDivPadded" style="display:none;">
                    <table width="100%">
                        <tr>
                            <td class="smallTitle"><fmt:message key="pointHierarchy.details"/></td>
                            <td align="right">
                                <tag:img id="deleteImg" png="delete" title="common.delete" onclick="deleteFolder();"/>
                                <tag:img id="saveImg" png="save" title="common.save" onclick="saveFolder();"/>
                            </td>
                        </tr>
                    </table>

                    <table>
                        <tr>
                            <td class="formLabelRequired"><fmt:message key="pointHierarchy.name"/></td>
                            <td class="formField"><input id="folderName" type="text"/></td>
                        </tr>
                    </table>
                </div>
            </td>
        </tr>
    </table>
</tag:page>
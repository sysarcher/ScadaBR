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
        var observableStore;
        var myModel;
        var myStore;

    window.onload = function() {
  
  
  require([
    "dojo/dom",
    "dojo/store/JsonRest",
    "dojo/store/Memory",
    "dojo/store/Observable",
    "dijit/tree/ObjectStoreModel", 
    "dijit/Tree",
    "dojo/domReady!"
], function(dom, JsonRest, Memory, Observable, ObjectStoreModel, Tree){

    // Create test store, adding the getChildren() method required by ObjectStoreModel
    myStore = new Memory({
      data: [
        { id: 0, name:'The earth', nodeType:'planet'},
          { id: 1, name:'Africa', nodeType:'continent', parentId: 0},
            { id: 7, name:'Egypt', nodeType:'country', parentId: 1 },
            { id: 8, name:'Kenya', nodeType:'country', parentId: 1 },
              { id: 11, name:'Nairobi', nodeType:'city', parentId: 8 },
              { id: 12, name:'Mombasa', nodeType:'city', parentId: 8 },
            { id: 9, name:'Sudan', nodeType:'country', parentId: 1 },
              { id: 13, name:'Khartoum', nodeType:'city', parentId: 9 },
          { id: 2, name:'Asia', nodeType:'continent', parentId: 0 },
            { id: 10, name:'China', nodeType:'country', parentId: 2 },
            { id: 14, name:'India', nodeType:'country', parentId: 2 },
            { id: 15, name:'Russia', nodeType:'country', parentId: 2 },
            { id: 16, name:'Mongolia', nodeType:'country', parentId: 2 },
          { id: 3, name:'Oceania', nodeType:'continent', parentId: 0},
          { id: 4, name:'Europe', nodeType:'continent', parentId: 0 },
            { id: 17, name:'Germany', nodeType:'country', parentId: 4 },
            { id: 18, name:'France', nodeType:'country', parentId: 4 },
            { id: 19, name:'Spain', nodeType:'country', parentId: 4 },
            { id: 20, name:'Italy', nodeType:'country', parentId: 4 },
          { id: 5, name:'North America', nodeType:'continent', parentId: 0 },
          { id: 6, name:'South America', nodeType:'continent', parentId: 0 }
      ],
      getChildren: function(object){
        var result = this.query({parentId: object.id});
        return result;
      },
      mayHaveChildren: function(object){
        console.log("MAY HAVE CHILDREN CALLED");
        // if true, we might be missing the data, false and nothing should be done
        return true; //object.type = "PF";
      },
      getRoot: function(onItem, onError){
        console.log("GET ROOT CALLED ");
        var result = this.query({id: 0});
        if (result != null) {
          onItem(result[0]);
        } else {
            onError(null);
        }
      },
      getLabel: function(object){
        console.log("GET LABEL CALLED " + object);
        // just get the name
        return object.name;
      }
    });


    myRestStore = new JsonRest({
      target: "dstree/",
      getChildren: function(object, onComplete, onError){
        console.log("GET CHILDREN CALLED " + object);
        this.query({parentId: object.id}).then(function(children){
            console.log("CHILDS: " + children);
        });
        this.query({parentId: object.id}).then(onComplete, onError);
      },
      mayHaveChildren: function(object){
        console.log("MAY HAVE CHILDREN CALLED");
        // if true, we might be missing the data, false and nothing should be done
        return object.nodeType === "PF";
      },
      getRoot: function(onItem, onError){
        console.log("GET ROOT CALLED ");
        // get the root object, we will do a get() and callback the result
        this.get("root").then(onItem, onError);
      },
      getLabel: function(object){
        console.log("GET LABEL CALLED " + object);
        // just get the name
        return object.name;
      }
    });
    
    
    //Direct connected will work ???
    observableStore = new Observable(myStore);

    // Create the model
    myModel = new ObjectStoreModel({
        store: observableStore,
        query: {id: 0}
    });


    // Create the Tree.
    tree = new Tree({
        model: myRestStore
    }, "treeDiv");
    tree.startup();
});
}  

function showRoot() {
    console.log("MODEL.root " + myModel.root);
    myRestStore.get(0).then(function(item){
        console.log(item);
        myRestStore.getChildren(item).then(function(children) {
            
        });
    });
    
    myRestStore.get(0);
    console.log(myRestStore.get(0));
    console.log(myRestStore.getChildren(myRestStore.get(0)));
    console.log("MODEL.root " + myModel.root);
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
                    <div data-dojo-type="dijit/Tree" id="myTree" data-dojo-props="model: myModel"></div>
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
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<div id="divDpEditCommon">
    <script type="text/javascript">
        console.log("script start");
        //   alert("Script run");
        require(["dojo/dom", "dojo/on", "dojo/request", "dojo/dom-form", "dijit/registry"],
                function (dom, on, request, domForm, registry) {

                    var cleanNode = function (node) {
                        //Destroy the dijit widgets
                        var formWidgets = registry.findWidgets(node);
                        formWidgets.forEach(function (widget) {
                            widget.destroyRecursive();
                        });
                    }
                    var parentNode = dom.byId('divDpEditCommon').parentNode;
                    var form = dom.byId('dpEditCommon');
                    // Attach the onsubmit event handler of the form
                    on(form, "submit", function (evt) {
                        //   alert("Form submitt");
                        // prevent the page from navigating after submit
                        evt.stopPropagation();
                        evt.preventDefault();

                        // Post the data to the server
                        request.post("pointEdit/common", {
                            query: {
                                id: ${dataPoint.id},
                            },
                            // Send the username and password
                            data: domForm.toObject(form),
                            // Wait 2 seconds for a response
                            timeout: 20000

                        }).then(
                                function (response) {
                                    cleanNode(parentNode)
                                    parentNode.innerHTML = response;
                                    require(["dojo/parser"], function (parser) {
                                        parser.parse(parentNode).then(function () {
                                            var scripts = parentNode.getElementsByTagName("script");
                                            for (var i = 0; i < scripts.length; i++) {
                                                eval(scripts[i].innerHTML);
                                            }
                                        });
                                    });
                                },
                                function (error) {
                                    // Display the error returned
                                    cleanNode(parentNode)
                                    parentNode.innerHTML = "<div class=\"error\">" + error + "<div>";
                                });
                    });
                }
        );
        console.log("script end");
    </script>
    <dijit:form id="dpEditCommon" action="pointEdit/common" method="post">
        <dojox:tableContainer cols="1">
            <dijit:validationTextBox i18nLabel="pointEdit.props.name"  path="dataPoint.name"/>
            <dijit:validationTextBox i18nLabel="pointEdit.props.name"  path="dataPoint."/>
        </dojox:tableContainer>    
        <dijit:button type="submit" i18nLabel="login.loginButton" />

    </dijit:form>
</div>
<%-- Page Fragment for dynamic AJax inclusion --%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<script>
    require([
        "dojo/dom",
        "dojo/parser"
    ], function (dom, parser) {
        var editCommonProperties = dom.byId("editCommonPropertiesId");
        parser.parse(editCommonProperties).then(function () {
            //setup    
        });
    });
</script>
<div id="editCommonPropertiesId">
    <dojox:tableContainer cols="1">
        <dijit:validationTextBox i18nLabel="pointEdit.props.name"  path="dataPoint.name"/>
    </dojox:tableContainer>
</div>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<script>
    require([
        "dojo/dom",
        "dojo/parser",
        "scadabr/jsp/dataSources/PointLocator"
    ], function (dom, parser, PointLocator) {
        var editCommonProperties = dom.byId("editCommonPropertiesId");
        parser.parse(editCommonProperties).then(function () {
            _PointLocator = new PointLocator("pointLocatorVoProperies", ${pointLocator.id});
        }, function (error) {
            alert(error);
        });
    });
</script>
<div id="editCommonPropertiesId">
    <form id="pointLocatorVoProperies">
        <dojox:tableContainer cols="1">
            <dijit:validationTextBox i18nLabel="pointEdit.props.name"  path="pointLocator.name"/>
        </dojox:tableContainer>
        <dijit:button type="submit" i18nLabel="login.loginButton" />
    </form>
</div>
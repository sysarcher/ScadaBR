<%@ taglib prefix="dijit" uri="/WEB-INF/tld/dijit.tld" %>
<%@ taglib prefix="dojox" uri="/WEB-INF/tld/dojox.tld" %>
<script>
    require([
        "dojo/dom",
        "dojo/parser",
        "scadabr/jsp/dataSources/PointLocator"
    ], function (dom, parser, PointLocator) {
        var _plFormParseWrapper = dom.byId("plFormParseWrapperId");
        try {
            parser.parse(_plFormParseWrapper).then(function () {
                _PointLocator = new PointLocator("plFormId", ${pointLocator.id});
            }, function (err) {
                alert(err);
            });
        } catch (error) {
            alert(error);
        }
    });
</script>
<div id="plFormParseWrapperId">
    <form id="plFormId">
        <dojox:tableContainer cols="1">
            <dijit:validationTextBox i18nLabel="pointEdit.props.name"  path="pointLocator.name"/>
        <jsp:doBody />
        </dojox:tableContainer>
        <dijit:button type="submit" i18nLabel="login.loginButton" />
    </form>
</div>
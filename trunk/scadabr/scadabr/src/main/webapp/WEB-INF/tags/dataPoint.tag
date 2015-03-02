<%@ taglib prefix="dijit" uri="/WEB-INF/tld/dijit.tld" %>
<%@ taglib prefix="dojox" uri="/WEB-INF/tld/dojox.tld" %>
<script>
    require([
        "dojo/dom",
        "dojo/parser",
        "scadabr/AjaxFormPost"
    ], function (dom, parser, AjaxFormPost) {
        var _dpFormParseWrapper = dom.byId("dpFormParseWrapperId");
        try {
            parser.parse(_dpFormParseWrapper).then(function () {
                var _DpFormPost = new AjaxFormPost("dpFormId", "dataPointDetails/editProperties?id=" + ${dataPoint.id});
            }, function (err) {
                alert(err);
            });
        } catch (error) {
            alert(error);
        }
    });
</script>
<div id="dpFormParseWrapperId">
    <form id="dpFormId">
        <dojox:tableContainer cols="1">
            <dijit:validationTextBox i18nLabel="pointEdit.props.name" name="name"  value="${dataPoint.name}"/>
            <jsp:doBody />
        </dojox:tableContainer>
        <dijit:button type="submit" i18nLabel="login.loginButton" />
    </form>
</div>
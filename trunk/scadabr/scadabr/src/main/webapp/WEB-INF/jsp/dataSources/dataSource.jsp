<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<script>
    require([
        "dojo/dom",
        "dojo/parser",
        "scadabr/jsp/dataSources/DataSource"
    ], function (dom, parser, DataSource) {
        var editCommonProperties = dom.byId("editCommonPropertiesId");
        parser.parse(editCommonProperties).then(function () {
            _DataSource = new DataSource("DataSourceVoProperies", ${dataSource.id});
        }, function (error) {
            alert(error);
        });
    });
</script>
<div id="editCommonPropertiesId">
    <form id="DataSourceVoProperies">
        <dojox:tableContainer cols="1">
            <dijit:validationTextBox i18nLabel="pointEdit.props.name"  path="dataSource.name"/>
            <dijit:validationTextBox i18nLabel="common.xid"  path="dataSource.xid"/>
        </dojox:tableContainer>
        <dijit:button type="submit" i18nLabel="login.loginButton" />
    </form>
</div>
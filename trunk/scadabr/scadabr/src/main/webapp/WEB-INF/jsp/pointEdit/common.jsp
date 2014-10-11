<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<dijit:form action="login" method="post">

    <dojox:tableContainer cols="1">
        <dijit:validationTextBox i18nLabel="pointEdit.props.name"  path="dataPoint.name"/>
    </dojox:tableContainer>    
</dijit:form>
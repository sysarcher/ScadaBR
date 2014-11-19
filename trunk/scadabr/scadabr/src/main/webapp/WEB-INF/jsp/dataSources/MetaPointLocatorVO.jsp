<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<tag:pointLocator>
    <jsp:body>

        <dijit:select path="pointLocator.updateEvent"  i18nLabel="dsEdit.meta.event" items="${pointLocator.updateEvents}"/>
       
        <dijit:textBox i18nLabel="dsEdit.meta.event.cron"  path="pointLocator.updateCronPattern"/>
        <dijit:numberSpinner i18nLabel="dsEdit.meta.delay"  path="pointLocator.executionDelaySeconds"/>
        <dijit:textarea i18nLabel="dsEdit.meta.script"  path="pointLocator.script"/>
    </jsp:body>
</tag:pointLocator>
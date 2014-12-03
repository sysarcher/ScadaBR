<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<tag:pointLocator>
    <jsp:body>

        <dijit:select path="pointLocator.updateEvent"  i18nLabel="dsEdit.meta.event" items="${pointLocator.updateEvents}"/>
       
        <dijit:validationTextBox i18nLabel="dsEdit.meta.event.cron"  name="updateCronPattern" value="${pointLocator.updateCronPattern}"/>
        <dijit:numberSpinner i18nLabel="dsEdit.meta.delay"  name="executionDelaySeconds" number="${pointLocator.executionDelaySeconds}"/>
        <dijit:textarea i18nLabel="dsEdit.meta.script"  name="script" text="${pointLocator.script}"/>
    </jsp:body>
</tag:pointLocator>
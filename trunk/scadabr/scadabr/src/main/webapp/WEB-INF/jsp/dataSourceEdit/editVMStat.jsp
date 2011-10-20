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
<%@page import="com.serotonin.mango.vo.dataSource.vmstat.VMStatAttributes"%>
<%@include file="/WEB-INF/jsp/include/tech.jsp" %>

<%@page import="com.serotonin.mango.vo.dataSource.vmstat.VMStatDataSourceVO"%>
<%@page import="com.serotonin.mango.vo.dataSource.vmstat.VMStatPointLocatorVO"%>
<tag:dsEditHead dsDwr="VMStatEditDwr" />

    <script type="text/javascript">
        var oldAttributeLabel = "<fmt:message key="${locator.attribute.i18nMessageKey}"/>";
        
        function saveDataSourceImpl() {
            VMStatEditDwr.saveVMStatDataSource($get("dataSourceName"), $get("dataSourceXid"), $get("pollSeconds"),
            $get("outputScale"), saveDataSourceCB);
        }
  
        function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
            pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key="dsEdit.vmstat.attribute"/>";
            pointListColumnFunctions[pointListColumnFunctions.length] =
                function(p) { return p.pointLocator.configurationDescription; };
        }
  
        function editPointCBImpl(locator) {
            $set("attribute", locator.attribute);
        }
  
        function savePointImpl(locator) {
            delete locator.settable;
            delete locator.mangoDataType;
      
            locator.attribute = $get("attribute");
      
            VMStatEditDwr.saveVMStatPointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
        }
        
        function attributeChanged(attributes) {
            var num = attributes.selectedIndex;
            var newAttributeLabel =  attributes.options[num].text;
            
            if (($get("name") == oldAttributeLabel) || ($get("name") == "")) {
                $set("name", newAttributeLabel);
                oldAttributeLabel = newAttributeLabel;
            }
        }
        
    </script>

    <c:set var="dsDesc"><fmt:message key="dsEdit.vmstat.desc"/></c:set>
    <c:set var="dsHelpId" value="vmstatDS"/>
    <%@include file="/WEB-INF/jsp/dataSourceEdit/dsHead.jspf" %>
    <tr>
        <td class="formLabelRequired"><fmt:message key="dsEdit.vmstat.pollSeconds"/></td>
        <td class="formField"><input id="pollSeconds" type="text" value="${dataSource.pollSeconds}"/></td>
    </tr>
    <tr>
        <td class="formLabelRequired"><fmt:message key="dsEdit.vmstat.outputScale"/></td>
        <td class="formField">
            <sst:select id="outputScale" value="${dataSource.outputScale}">
                <tag:exportCodesOptions sst="true" optionList="<%= VMStatDataSourceVO.OUTPUT_SCALE_CODES.getIdKeys()%>"/>
            </sst:select>
        </td>
    </tr>
    <%@ include file="/WEB-INF/jsp/dataSourceEdit/dsEventsFoot.jspf" %>

    <tag:pointList pointHelpId="vmstatPP">
        <tr>
            <td class="formLabelRequired"><fmt:message key="dsEdit.vmstat.attribute"/></td>
            <td class="formField">
                <select id="attribute" value="${locator.attribute}" onchange="attributeChanged(this)">
                    <tag:localizableEnumOptions enumValues="<%= VMStatAttributes.values()%>"/>
                </select>
            </td>
        </tr>
    </tag:pointList>
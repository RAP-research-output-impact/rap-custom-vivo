<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying search error message -->

<div id="search-form">
    <form action="" method="GET">
        <input type="text" name="querytext" value="${querytext}" />
        <strong>AND</strong>
        <select name="facetAsText">
            <#if facetAsText?has_content>
                <#list facetsAsText as fat>
                    <option value="${fat.fieldName}" <#if fat.fieldName == facetAsText>selected</#if>>${fat.publicName}</option>
                </#list>
            <#else>
                <#list facetsAsText as fat>
                    <option value="${fat.fieldName}">${fat.publicName}</option>
                </#list>
            </#if>
        </select>
        <#if facetTextValue?has_content>
            <input type="text" name="facetTextValue" value="${facetTextValue}"/>
        <#else>
            <input type="text" name="facetTextValue"/>
        </#if>
        <#if classGroupURI?has_content>
            <input type="hidden" name="classgroup" value="${classGroupURI}" />
        </#if>
        <input type="submit" value="Go"/>
    </form>
    <h1>No matching results.</h1>
</div>
<div style="width: 75%; float: right">
    <#include "search-help.ftl">
</div>

<@showAddress statement />

<#macro showAddress statement>
    [${statement.number}] ${statement.fullAddress}
    <#if false && statement.org??>
        <span class="org-enhanced"><br/><a href="${profileUrl(statement.uri("org"))}">${statement.orgName}</a></span>
    </#if>
</#macro>

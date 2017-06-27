<@showAddress statement />

<#macro showAddress statement>
    <#if statement.org??>
        ${statement.number}) <a href="${profileUrl(statement.uri("org"))}">${statement.orgName}</a>
    <#else>
        ${statement.fullAddress}
    </#if>
</#macro>

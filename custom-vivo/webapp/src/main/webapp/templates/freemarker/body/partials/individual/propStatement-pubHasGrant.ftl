<@showGrant statement />

<#macro showGrant statement>
    <#-- the calling template generates the enclosing li here -->
        <span class="pub_meta">Funding Agency:</span>
        <span>${statement.funderName!}</span>
    </li>
    <li>
        <span class="pub_meta">Grant Number:</span>
        <span>${statement.grantId!}</span>
</#macro>

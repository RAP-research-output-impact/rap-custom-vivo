<#if partnerResearcherName??>
  <h2 class="copubdept-title">Co-publications by ${name} (DTU) and ${partnerResearcherName} (${collabOrg})</h2>
<#else>
  <h2 class="copubdept-title">Co-publications by DTU Researcher ${name} - DTU and ${collabOrg}</h2>
</#if>

<div class="copubdept-sub">${pubs?size} total co-publications</div>
<hr/>

<#include "copubs-list.ftl">

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual-vivo.css" />')}

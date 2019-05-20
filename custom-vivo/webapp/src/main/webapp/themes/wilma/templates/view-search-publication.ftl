  <div class="publication-short-view"> 
    <h5>
    <#assign publication = publication[0]>
    <#if publication.p??>
    <a href="${individual.profileUrl}"">
    </#if>
    <#if publication.title??>
      ${publication.title!}
    </#if>
    <#if publication.p??>
      </a>&nbsp;
    </#if>
    </h6>
    <#if publication.authorList??>
       <em>
       <#assign count = 0>
       <#assign authors = publication.authorList?split(";")>
       <#assign authorLength = authors?size>
       <#list authors as author>
           <#assign count = count + 1>
	   <#if (count == 4) && (authorLength gt 4) >
              [et al.]
	   <#elseif count == 1>
               ${author}
	   <#elseif count lt 5>
	       , ${author}
	   </#if>
       </#list>
       </em>
       <br/>
    </#if>
    ${publication.journal!}
    <span style="margin-left: 3em;">${publication.date!}</style>
    <br/>
    ${publication.wosId!}
    <#if publication.referenceCount??>
        <span style="margin-left: 3em;">References: ${publication.referenceCount!}</style>
    </#if>
    <#if publication.citationCount??>
        <span style="margin-left: 3em;">Citations: ${publication.citationCount!}</style>
    </#if>
</div>

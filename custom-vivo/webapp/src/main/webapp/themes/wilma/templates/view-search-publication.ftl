    <h6>
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


 <div class="copubdept-pubmeta">
    <h6><a href="${profileUrl(publication.p)}"">${publication.title}</a>&nbsp;<span class="listDateTime">${publication.date[0..9]}</span></h6>
    <div class="pub-ids">
        <#if publication.doi?has_content>
            <span class="pub-id-link">Full Text via DOI:&nbsp;<a href="http://doi.org/${publication.doi}"  title="Full Text via DOI" target="external">${publication.doi}</a></span>
        </#if>
        <#if publication.wosId?has_content>
            <#-- Change WoS link to match customer code -->
            <span class="pub-id-link">Web of Science:&nbsp;<a href="http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&SrcApp=VIVO&SrcAuth=TRINTCEL&KeyUT=${publication.wosId}&DestLinkType=FullRecord&DestApp=WOS_CPL"  title="View in Web of Science" target="external">${publication.wosId}</a></span>
        </#if>
    </div>
    <#if publication.wosId?has_content>
        <div class="pub-ids">
            <span class="counts">
                References:&nbsp;<a href="http://apps.webofknowledge.com/InterService.do?product=WOS&toPID=WOS&action=AllCitationService&isLinks=yes&highlighted_tab=WOS&last_prod=WOS&fromPID=WOS&srcDesc=RET2WOS&srcAlt=Back+to+Web+of+Science&UT=${publication.wosId}&search_mode=CitedRefList&SID=D6oIIYbSLV2HqN3nOCS&parentProduct=WOS&recid=${publication.wosId}&fromRightPanel=true&cacheurlFromRightClick=no" title="View references in Web of Science" target="external">${publication.refCount}</a>
                Citations:&nbsp;<a href="http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&amp;SrcApp=VIVO&amp;SrcAuth=TRINTCEL&amp;KeyUT=${publication.wosId}&amp;DestLinkType=CitingArticles&amp;DestApp=WOS_CPL" title="View citations in Web of Science" target="external">${publication.citeCount}</a>
            </span>
        </div>
     </#if>
     <#if publication.authors?has_content>
         <ul class="authors">
             <#list so.authors?split(";") as au>
                 <li>${au}</li>
             </#list>
         </ul>
     </#if>
</div>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual-vivo.css" />')}

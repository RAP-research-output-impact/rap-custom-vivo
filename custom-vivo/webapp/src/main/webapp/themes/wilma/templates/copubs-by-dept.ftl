<h2>Collaboration by department - DTU and ${collabOrg}</h2>
<#if collabSubName?has_content>
    <h3>${pubs?size} co-publications for ${mainOrg} and ${collabSubName}, [${collabOrg}]</h3>
<#else>
    <h3>${pubs?size} co-publications for ${mainOrg} and ${collabOrg}</h3>
</#if>
<hr/>
<#list pubs as pub>
    <div class="copubdept-pubmeta">
        <h6><a href="${profileUrl(pub.p)}"">${pub.title}</a>&nbsp;<span class="listDateTime">${pub.date[0..9]}</span></h6>
        <div class="pub-ids">
            <#if pub.doi?has_content>
                <span class="pub-id-link">Full Text via DOI:&nbsp;<a href="http://doi.org/${pub.doi}"  title="Full Text via DOI" target="external">${pub.doi}</a></span>
            </#if>
            <#if pub.wosId?has_content>
                <#-- Change WoS link to match customer code -->
                <span class="pub-id-link">
                    Web of Science:&nbsp;<a href="http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&SrcApp=VIVO&SrcAuth=TRINTCEL&KeyUT=${pub.wosId}&DestLinkType=FullRecord&DestApp=WOS_CPL" title="View in Web of Science" target="external">${pub.wosId}</a>
                </span>
            </#if>
        </div>
        <#if pub.wosId?has_content>
            <div class="pub-ids">
                <span class="counts">
                    References:&nbsp;<a href="http://apps.webofknowledge.com/InterService.do?product=WOS&toPID=WOS&action=AllCitationService&isLinks=yes&highlighted_tab=WOS&last_prod=WOS&fromPID=WOS&srcDesc=RET2WOS&srcAlt=Back+to+Web+of+Science&UT=${pub.wosId}&search_mode=CitedRefList&SID=D6oIIYbSLV2HqN3nOCS&parentProduct=WOS&recid=${pub.wosId}&fromRightPanel=true&cacheurlFromRightClick=no" title="View references in Web of Science" target="external">${pub.refCount}</a>
                    Citations:&nbsp;<a href="http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&amp;SrcApp=VIVO&amp;SrcAuth=TRINTCEL&amp;KeyUT=${pub.wosId}&amp;DestLinkType=CitingArticles&amp;DestApp=WOS_CPL" title="View citations in Web of Science" target="external">${pub.citeCount}</a>
                </span>
            </div>
        </#if>
        <table id="copub-authors">
            <tr>
                <th>${mainOrg}</th>
                <#list pub.subOrg as so>
                    <th>${so.subOrgName}</th>
                </#list>
            </tr>
            <tr>
                <td>
                    <#if pub.dtuSubOrg?has_content>
                        <ul class="authors">
                            <#list pub.dtuSubOrg as so>
                                <#if so.authors?has_content>
                                    <#list so.authors?split(";") as au>
                                        <li>${au}</li>
                                    </#list>
                                </#if>
                            </#list>
                        </ul>
                    </#if>
                </td>
                <#list pub.subOrg as so>
                    <td>
                        <#if so.authors?has_content>
                            <ul class="authors">
                                <#list so.authors?split(";") as au>
                                    <li>${au}</li>
                                </#list>
                            </ul>
                        </#if>
                    </td>
                </#list>
            </tr>
        </table>
    </div>
</#list>
<div id="footer-spacer">
</div>
${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual-vivo.css" />')}

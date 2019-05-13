<#-- Publication pages -->
<#include "individual-setup.ftl">
<#import "lib-microformats.ftl" as mf>
<#import "lib-properties.ftl" as p>

<#global pg=propertyGroups>

<#-- helper to get data properties -->
<#function gdp prop>
    <#assign val = pg.getProperty(prop)!>
    <#if val?has_content>
        <#if val.statements[0]??>
            <#return val.statements[0].value>
        </#if>
    </#if>
</#function>

<#assign bibo = "http://purl.org/ontology/bibo/">
<#assign vivo = "http://vivoweb.org/ontology/core#">
<#assign wos = "http://webofscience.com/ontology/wos#">
<#assign abstractp = bibo + "abstract">
<#assign abstract = gdp(abstractp)!>
<#assign volumep = bibo + "volume">
<#assign volume = gdp(volumep)!>
<#assign issuep = bibo + "issue">
<#assign issue = gdp(issuep)!>
<#assign pageStartp = bibo + "pageStart">
<#assign pageStart = gdp(pageStartp)!>
<#assign pageEndp = bibo + "pageEnd">
<#assign pageEnd = gdp(pageEndp)!>
<#assign issnp = bibo + "issn">
<#assign issn = gdp(issnp)!>
<#assign doip = "http://purl.org/ontology/bibo/doi">
<#assign pmidp = "http://purl.org/ontology/bibo/pmid">
<#assign pmcidp = "http://vivoweb.org/ontology/core#pmcid">
<#assign wosp = "http://webofscience.com/ontology/wos#wosId">
<#assign refp = "http://webofscience.com/ontology/wos#referenceCount">
<#assign citep = "http://webofscience.com/ontology/wos#citationCount">

<#--Number of labels present-->
<#if !labelCount??>
    <#assign labelCount = 0 >
</#if>
<#--Number of available locales-->
<#if !localesCount??>
    <#assign localesCount = 1>
</#if>
<#--Number of distinct languages represented, with no language tag counting as a language, across labels-->
<#if !languageCount??>
    <#assign languageCount = 1>
</#if>

<#-- Default individual profile page template -->
<#--@dumpAll /-->
<div class="box-individual-publication">


<section id="individual-intro" class="vcard" role="region" <@mf.sectionSchema individual/>>
    <section id="share-contact" role="region">
        <#-- No images -->
    </section>
    <!-- start section individual-info -->
    <section id="individual-info" ${infoClass!} role="region">
        <#include "individual-adminPanel.ftl">

        <#if individualProductExtensionPreHeader??>
            ${individualProductExtensionPreHeader}
        </#if>

        <header>
            <#if relatedSubject??>
                <h2>${relatedSubject.relatingPredicateDomainPublic} for ${relatedSubject.name}</h2>
                <p><a href="${relatedSubject.url}" title="${i18n().return_to(relatedSubject.name)}">&larr; ${i18n().return_to(relatedSubject.name)}</a></p>
            <#else>
                <h1 itemprop="name">
                    <#-- Label -->
                    <@p.label individual editable labelCount localesCount languageCount/>
                </h1>
            </#if>
        </header>

    <#if individualProductExtension??>
        ${individualProductExtension}
    <#else>
            </section> <!-- individual-info -->
        </section> <!-- individual-intro -->
    </#if>

<#assign nameForOtherGroup = "${i18n().other}">


<#assign doi=gdp(doip)!>
<#assign pmid=gdp(pmidp)!>
<#assign wosId=gdp(wosp)!>
<#assign refs=gdp(refp)!>
<#assign cites=gdp(citep)!>

<!-- authors -->
<div class="pub_authors-box">
  <#if pg.getProperty(vivo + "relatedBy", vivo + "Authorship")??>
    <@p.objectProperty pg.getProperty(vivo + "relatedBy", vivo + "Authorship") false />
  </#if>
</div>
<!-- end .authors-box -->

<!-- journal -->
<div class="pub_journal">

  <h2>Journal of Physical Chemistry</h2>

<!-- publication attributes -->
<ul class="pub_meta-list">
  <li>
    <span class="pub_meta">Volume:</span>
    <span class="pub_meta-value">${volume}</span>
  </li>
  <li>
    <span class="pub_meta">Issue:</span>
    <span class="pub_meta-value">${issue}</span>
  </li>
  <li>
    <span class="pub_meta">Pages:</span>
    <span class="pub_meta-value">${pageStart}-${pageEnd}</span>
  </li>
  <li>
    <span class="pub_meta">ISSN:</span>
    <span class="pub_meta-value">${issn}</span>
  </li>
  <li>
    <span class="pub_meta">DOI:</span>
    <span class="pub_meta-value"><a href="http://doi.org/${doi}" title="Full Text via DOI" target="external">${doi}</a></span>
  </li>
  <#if pg.getProperty(vivo + "dateTimeValue")??>
  <li>
    <span class="pub_meta">Published:</span>
    <span class="pub_meta-value"><@p.objectProperty pg.getProperty(vivo + "dateTimeValue") false/></span>
  </li>
  </#if>
  <li>
    <span class="pub_meta">Web of Science:</span>
    <span class="pub_meta-value"><a href="http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&SrcApp=VIVO&SrcAuth=TRINTCEL&KeyUT=${wosId}&DestLinkType=FullRecord&DestApp=WOS_CPL" title="View in Web of Science" target="external">${wosId}</a></span>
  </li>
  <li>
    <span class="pub_meta">References:</span>
    <a href="" class="pub_meta-value"><a href="http://apps.webofknowledge.com/InterService.do?product=WOS&toPID=WOS&action=AllCitationService&isLinks=yes&highlighted_tab=WOS&last_prod=WOS&fromPID=WOS&srcDesc=RET2WOS&srcAlt=Back+to+Web+of+Science&UT=${wosId}&search_mode=CitedRefList&SID=D6oIIYbSLV2HqN3nOCS&parentProduct=WOS&recid=${wosId}&fromRightPanel=true&cacheurlFromRightClick=no" title="View references in Web of Science" target="external">${refs}</a></a>
  </li>
  <li>
    <span class="pub_meta">Citations:</span>
    <a href="" class="pub_meta-value"><a href="http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&SrcApp=VIVO&SrcAuth=TRINTCEL&KeyUT=${wosId}&DestLinkType=CitingArticles&DestApp=WOS_CPL" title="View citations in Web of Science" target="external">${cites}</a></a>
  </li>
</ul>

<!-- abstract -->
<div class="pub_abstract">
  <h3>Abstract</h3>
  <p>${abstract}</p>
</div>

<div class="pub_keywords">
  <!-- keywords -->
  <h3>Keywords</h3>
  <ul class="one-line-list">
    <#if pg.getProperty(wos + "authorKeyword")??>
      <@p.dataPropertyListing pg.getProperty(wos + "authorKeyword") false />
    </#if>
    <#if pg.getProperty(wos + "keywordPlus")??>
      <@p.dataPropertyListing pg.getProperty(wos + "keywordPlus") false />
    </#if>
  </ul>
</div>

<!-- categories/classification -->
<div class="pub_categories">
  <h3>Categories/Classification</h3>

<#-- Research areas do not appear to be present in the RDF data 2019-05-13 -->
 <#if pg.getProperty(wos + "hasSubjectArea")??>
  <div class="pub_keywords-enumeration clearfix">
    <h4>Research Areas</h4>
    <ul class="one-line-list">
      <@p.objectProperty pg.getProperty(wos + "hasSubjectArea") false />
    </ul>
  </div>
 </#if>

 <#if pg.getProperty(wos + "hasCategory")??>
  <div class="pub_keywords-enumeration clearfix">
    <h4>Web of Science Categories</h4>
    <ul class="one-line-list">
      <@p.objectProperty pg.getProperty(wos + "hasCategory") false />
    </ul>
  </div>
 </#if>
</div>
<!-- end .pub_categories -->

<!-- Author addresses -->
<#if pg.getProperty(vivo + "relatedBy", wos + "Address")??>
<div class="pub_author-addresses">
  <h3>Author Addresses</h3>
  <ul>
    <@p.objectProperty pg.getProperty(vivo + "relatedBy", wos + "Address") false />
  </ul>
</div>
</#if>

<!-- Other details -->
<div class="pub_other-details">

  <ul class="pub_meta-list">
    <#if pg.getProperty(vivo + "relatedBy", wos + "Grant")??>
      <@p.objectProperty pg.getProperty(vivo + "relatedBy", wos + "Grant") false />
    </#if>
  </ul>

  <ul>
    <#-- Not included in RDF 2019-05-13 ? 
    <li>
      <span class="pub_meta">Publisher</span>
      <span class="pub_meta-value">AMER CHEMICAL SOC, 1155 16TH ST NW. WASHINGTON, DC 20036 USA</span>
    </li>
    -->
    <li>
      <span class="pub_meta">Document Type</span>
      <span class="pub_meta-value"><@p.mostSpecificTypes individual /></span>
    </li>
    <#-- Language not yet available? 2019-05-13
    <li>
      <span class="pub_meta">Langauge</span>
      <span class="pub_meta-value">English</span>
    </li>
    -->
  </ul>

</div>
<!-- end other-details -->

</div>
<!-- end pub_journal -->

</div>
<!-- end .box-individual-publication -->


<#assign skipThis = propertyGroups.pullProperty (citep)!>
<#assign skipThis = propertyGroups.pullProperty (refp)!>

<script>
    var imagesPath = '${urls.images}';
        var individualUri = '${individual.uri!}';
        var individualPhoto = '${individual.thumbNail!}';
        var exportQrCodeUrl = '${urls.base}/qrcode?uri=${individual.uri!}';
        var baseUrl = '${urls.base}';
    var i18nStrings = {
        displayLess: '${i18n().display_less}',
        displayMoreEllipsis: '${i18n().display_more_ellipsis}',
        showMoreContent: '${i18n().show_more_content}',
        verboseTurnOff: '${i18n().verbose_turn_off}',
        researchAreaTooltipOne: '${i18n().research_area_tooltip_one}',
        researchAreaTooltipTwo: '${i18n().research_area_tooltip_two}'
    };
    var i18nStringsUriRdf = {
        shareProfileUri: '${i18n().share_profile_uri}',
        viewRDFProfile: '${i18n().view_profile_in_rdf}',
        closeString: '${i18n().close}'
    };
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual.css" />')}

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/jquery_plugins/qtip/jquery.qtip-1.0.0-rc3.min.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/moreLessController.js"></script>')}

<script type="text/javascript">
    i18n_confirmDelete = "${i18n().confirm_delete}"
</script>


${scripts.add('<script type="text/javascript" src="https://d1bxh8uas1mnw7.cloudfront.net/assets/embed.js"></script>')}

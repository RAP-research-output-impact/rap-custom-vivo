<#-- Publication pages -->
<#include "individual-setup.ftl">
<#import "lib-microformats.ftl" as mf>

<#assign doip = "http://purl.org/ontology/bibo/doi">
<#assign pmidp = "http://purl.org/ontology/bibo/pmid">
<#assign pmcidp = "http://vivoweb.org/ontology/core#pmcid">
<#assign wosp = "http://webofscience.com/ontology/wos#wosId">
<#assign refp = "http://webofscience.com/ontology/wos#referenceCount">
<#assign citep = "http://webofscience.com/ontology/wos#citationCount">
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
		    Functional Independent Scaling Relation for ORR/OER Catalysts
                    <#-- Label -->
                    <#-- @p.label individual editable labelCount localesCount languageCount/ -->
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
  <div class="pub_author-name">
    <a href="">Christensen, R</a> (Christensen, Rune)<sup>[ 1 ]</sup>
  </div>
  <div class="pub_author-name">
    <a href="">Hansen, HA</a> (Hansen, Heine A.)<sup>[ 1 ]</sup>
  </div>
  <div class="pub_author-name">
    <a href="">Dickens, CF</a> (Dickens, Colin F.)<sup>[ 2, 3 ]</sup>
  </div>
  <div class="pub_author-name">
    <a href="">Norskov, JK</a> (Norskov, Jens K.)<sup>[ 2, 3 ]</sup>
  </div>
  <div class="pub_author-name">
    <a href="">Vegge, T</a>(Vegge, Tejs)<sup>[ 1 ]</sup>
  </div>
</div>
<!-- end .authors-box -->

<!-- journal -->
<div class="pub_journal">

  <h2>Journal of Physical Chemistry</h2>

<!-- publication attributes -->
<ul class="pub_meta-list">
  <li>
    <span class="pub_meta">Volume:</span>
    <span class="pub_meta-value">120</span>
  </li>
  <li>
    <span class="pub_meta">Issue:</span>
    <span class="pub_meta-value">43</span>
  </li>
  <li>
    <span class="pub_meta">Pages:</span>
    <span class="pub_meta-value">24910-24916</span>
  </li>
  <li>
    <span class="pub_meta">ISSN:</span>
    <span class="pub_meta-value">1932-7447</span>
  </li>
  <li>
    <span class="pub_meta">DOI:</span>
    <span class="pub_meta-value"><a href="">10.1021/acs.jpcc.6b09141</a></span>
  </li>
  <li>
    <span class="pub_meta">Published:</span>
    <span class="pub_meta-value">NOV 3 2016</span>
  </li>
  <li>
    <span class="pub_meta">Web of Science:</span>
    <span class="pub_meta-value">WOS:00038719800043</span>
  </li>
  <li>
    <span class="pub_meta">References:</span>
    <a href="" class="pub_meta-value">48</a>
  </li>
  <li>
    <span class="pub_meta">Citations:</span>
    <a href="" class="pub_meta-value">7</a>
  </li>
</ul>

<!-- abstract -->
<div class="pub_abstract">
  <h3>Abstract</h3>
  <p>A widely used adsorption energy scaling relation between OH* and OOH* intermediates in the oxygen reduction reaction (ORR) and oxygen evolution reaction (OER) has previously been determined using density functional theory and shown to dictate a minimum thermodynamic overpotential for both reactions.  Here, we show that the oxygen-oxygen bond in the OOH* intermediate is, however, not well described with the previosuly used class of exchange-correlation functionals. By quantifying and correctiont the systematic error, an improved description of gaseous peroxide species versus experimental data and a reduction in calculational uncertainty is obtained. For adsorbates, we find that the systematic error largely cancels the vdW interaction missing in the original determination of the scaling relation. An improved scaling relation, which is fully independent of the applied exchange-correlation functional, is obtained and found to differ by 0.1 eV from the original. This largely confirms that, although obtained with a method suffering from systematic errors, the previously obtained scaling relation is applicable for predictions of catalytic activity.</p>
</div>

<div class="pub_keywords">
  <!-- keywords -->
  <h3>Keywords</h3>
  <ul class="one-line-list">
    <li>OXYGEN REDUCTION REACTION;</li>
    <li>AUGMENTED WAVE METHOD;</li>
    <li>EVOLUTION REACTION;</li>
    <li>OXIDE SURFACES;</li>
    <li>METAL SURFACES;</li>
    <li>ELECTROCATALYSTS;</li>
    <li>DENSITY;</li>
    <li>APPROXIMATION;</li>
    <li>OPPORTUNITIES;</li>
    <li>UNIVERSALITY;</li>
  </ul>
</div>

<!-- categories/classification -->
<div class="pub_categories">
  <h3>Categories</h3>

  <div class="pub_keywords-enumeration clearfix">
    <h4>Research Areas</h4>
    <ul class="one-line-list">
      <li>Chemistry;</li>
      <li>Science &amp; Technology - Other Topics;</li>
      <li>Materials Science;</li>
    </ul>
  </div>

  <div class="pub_keywords-enumeration clearfix">
    <h4>Web of Science Categories</h4>
    <ul class="one-line-list">
      <li>Chemistry, Physical;</li>
      <li>Nanoscience &amp; Nanotechnology;</li>
      <li>Materials Science, Multidisciplinary;</li>
    </ul>
  </div>
</div>
<!-- end .pub_categories -->

<!-- Author addresses -->
<div class="pub_author-addresses">
  <h3>Author Addresses</h3>
  <ul>
    <li>[ 1 ] Tech Univ Denmark, Dept. Energy Convers &amp; Storage. Fyskivej Bld. 309, DK-2800 Lyngby, Denmark</li>
    <li>[ 2 ] SLAC Natl Accelerator Lab, SUNCAT Ctr Interface Sci &amp; Catalysis, 2575 Sand Hill Rd, Menlo Pk, CA 94025 USA</li>
    <li>[ 3 ] Stanford Univ. Dept Chem Engn, Stanford, CA 94305 USA</li>
  </ul>
</div>

<!-- Other details -->
<div class="pub_other-details">

  <ul class="pub_meta-list">
    <li>
      <span class="pub_meta">Funding Agency</span>
      <span>VILLUM FONDEN (V-SUSTAIN)</span>
    </li>
    <li>
      <span class="pub_meta">Grant Number</span>
      <span><9455/span>
    </li>
  </ul>

  <ul>
    <li>
      <span class="pub_meta">Publisher</span>
      <span class="pub_meta-value">AMER CHEMICAL SOC, 1155 16TH ST NW. WASHINGTON, DC 20036 USA</span>
    </li>
    <li>
      <span class="pub_meta">Document Type</span>
      <span class="pub_meta-value">Article</span>
    </li>
    <li>
      <span class="pub_meta">Langauge</span>
      <span class="pub_meta-value">English</span>
    </li>
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

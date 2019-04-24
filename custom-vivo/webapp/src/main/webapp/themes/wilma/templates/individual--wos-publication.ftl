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
                <h1 class="fn" itemprop="name">
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
<p>
<a href="">Christensen, R</a> (Christensen, Rune)<sup>[ 1 ]</sup>; <a href="">Hansen, HA</a> (Hansen, Heine A.)<sup>[ 1 ]</sup>; <a href="">Dickens, CF</a> (Dickens, Colin F.)<sup>[ 2, 3 ]</sup>; <a href="">Norskov, JK</a> (Norskov, Jens K.)<sup>[ 2, 3 ]</sup>; <a href="">Vegge, T</a>(Vegge, Tejs)<sup>[ 1 ]</sup>
</p>

<!-- journal -->
<p>JOURNAL OF PHYSICAL CHEMISTRY C</p>

<!-- publication attributes -->
<dl>
  <dt>Volume</dt>
  <dd>120</dd>
  <dt>Issue</dt>
  <dd>43</dd>
  <dt>Pages</dt>
  <dd>24910-24916</dd>
  <dt>ISSN</dt>
  <dd>1932-7447</dd>
  <dt>DOI</dt>
  <dd><a href="">10.1021/acs.jpcc.6b09141</a></dd>
  <dt>Published</dt>
  <dd>NOV 3 2016</dd>
  <dt>Web of Science</dt>
  <dd>WOS:00038719800043</dd>
  <dt>References</dt>
  <dd><a href="">48</a></dd>
  <dt>Citations</dt>
  <dd><a href="">7</a></dd>
</dl>

<!-- abstract -->
<h4>Abstract</h4>
<p>A widely used adsorption energy scaling relation between OH* and OOH* intermediates in the oxygen reduction reaction (ORR) and oxygen evolution reaction (OER) has previously been determined using density functional theory and shown to dictate a minimum thermodynamic overpotential for both reactions.  Here, we show that the oxygen-oxygen bond in the OOH* intermediate is, however, not well described with the previosuly used class of exchange-correlation functionals. By quantifying and correctiont the systematic error, an improved description of gaseous peroxide species versus experimental data and a reduction in calculational uncertainty is obtained. For adsorbates, we find that the systematic error largely cancels the vdW interaction missing in the original determination of the scaling relation. An improved scaling relation, which is fully independent of the applied exchange-correlation functional, is obtained and found to differ by 0.1 eV from the original. This largely confirms that, although obtained with a method suffering from systematic errors, the previously obtained scaling relation is applicable for predictions of catalytic activity.</p>

<!-- keywords -->
<h4>Keywords</h4>
<ul>
  <li>OXYGEN REDUCTION REACTION</li>
  <li>AUGMENTED WAVE METHOD</li>
  <li>EVOLUTION REACTION</li>
  <li>OXIDE SURFACES</li>
  <li>METAL SURFACES</li>
  <li>ELECTROCATALYSTS</li>
  <li>DENSITY</li>
  <li>APPROXIMATION</li>
  <li>OPPORTUNITIES</li>
  <li>UNIVERSALITY</li>
</ul>

<!-- categories/classification -->
<h4>Categories/Classification</h4>
<h4>Research Areas</h4>
  <ul>
    <li>Chemistry</li>
    <li>Science &amp; Technology - Other Topics</li>
    <li>Materials Science</li>
  </ul>
<h4>Web of Science Categories</h4>
  <ul>
    <li>Chemistry, Physical</li>
    <li>Nanoscience &amp; Nanotechnology</li>
    <li>Materials Science, Multidisciplinary</li>
  </ul>

<!-- Author addresses -->
<ul>
  <li>[ 1 ] Tech Univ Denmark, Dept. Energy Convers &amp; Storage. Fyskivej Bld. 309, DK-2800 Lyngby, Denmark</li>
  <li>[ 2 ] SLAC Natl Accelerator Lab, SUNCAT Ctr Interface Sci &amp; Catalysis, 2575 Sand Hill Rd, Menlo Pk, CA 94025 USA</li>
  <li>[ 3 ] Stanford Univ. Dept Chem Engn, Stanford, CA 94305 USA</li>
</ul>

<!-- Other details -->

<dl>
  <dt>Funding Agency</dt>
  <dd>VILLUM FONDEN (V-SUSTAIN)</dd>
  <dt>Grant Number</dt>
  <dd>9455</dd>
</dl>

<dl>
  <dt>Publisher</dt>
  <dd>AMER CHEMICAL SOC, 1155 16TH ST NW. WASHINGTON, DC 20036 USA</dd>
  <dt>Document Type</dt>
  <dd>Article</dd>
  <dt>Langauge</dt>
  <dd>English</dd>
</dl>

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

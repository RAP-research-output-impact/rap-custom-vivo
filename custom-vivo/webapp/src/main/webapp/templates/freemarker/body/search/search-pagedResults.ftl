<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for displaying paged search results -->

<script src="${urls.theme}/js/jquery.corner.js"></script>

<div class="contentsBrowseGroup">
    <#-- Refinement links -->
    <div class="searchTOC-box">
        <table border="1" class="searchTOC">
            <tr>
                <td>
                    <h4 class="hitCount">Results: ${hitCount}</h4>
                    <hr width="80%"/>
                    <div class="searchTOC-header">Use Facets to Refine Results</div>
                </td>
            </tr>
            <#if classGroupLinks?has_content>
                <tr class="search-facets-head">
                    <td>
                        <h4 class="search-facets-title"><div class="search-facets-toggle">+</div>Type</h4>
                    </td>
                </tr>
                <tr class="search-facets" style="display: none;">
                    <td>
                        <ul class="search-facets">
                        <#list classGroupLinks as link>
                            <li><a href="${link.url}" title="${i18n().class_group_link}">${link.text}</a><span>(${link.count})</span></li>
                        </#list>
                        </ul>           
                    </td>
                </tr>
            </#if>
            <#if classLinks?has_content>
                <tr class="search-facets-head">
                    <td>
                        <h4 class="search-facets-title"><div class="search-facets-toggle">+</div>Sub-Type</h4>
                    </td>
                </tr>
                <tr class="search-facets" style="display: none;">
                    <td>
                        <ul class="search-facets">
                            <#list classLinks as link>
                                <li><a href="${link.url}" title="${i18n().class_link}">${link.text}</a><span>(${link.count})</span></li>
                            </#list>
                        </ul>
                    </td>
                </tr>
            </#if>
            <#if facets?has_content>
                <#list facets as facet>
                    <tr class="search-facets-head">
                        <td style="align: left">
                            <h4 class="search-facets-title"><div class="search-facets-toggle">+</div>${facet.publicName}</h4>
                        </td>
                    </tr>
                    <tr class="search-facets" style="display: none;">
                        <td>
                            <ul>
                                <#list facet.categories as category>
                                    <li><a href="${category.url}" title="${category.text}">${category.text}</a><span>(${category.count})</span></li>
                                </#list>
                            </ul>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
    <div id="search-form">
        <form action="" method="GET">
            <input type="text" name="querytext" value="${querytext}" />
            <strong>AND</strong>
            <select name="facetAsText">
                <#if facetAsText?has_content>
                    <#list facetsAsText as fat>
                        <option value="${fat.fieldName}" <#if fat.fieldName == facetAsText>selected</#if>>${fat.publicName}</option>
                    </#list>
                <#else>
                    <#list facetsAsText as fat>
                        <option value="${fat.fieldName}">${fat.publicName}</option>
                    </#list>
                </#if>
            </select>
            <#if facetTextValue?has_content>
                <input type="text" name="facetTextValue" value="${facetTextValue}"/>
            <#else>
                <input type="text" name="facetTextValue"/>
            </#if>
            <#if classGroupURI?has_content>
                <input type="hidden" name="classgroup" value="${classGroupURI}" />
            </#if>
            <input type="submit" value="Go"/>
        </form>
        <!--
        <span id="searchHelp" style="text-align: right;"><a href="${urls.base}/searchHelp" title="${i18n().search_help}">${i18n().not_expected_results}</a></span>
        -->
    </div>
    <div style="width: 75%; float: right;">
        <#list RAPQueryReduce as link>
            <div class="qr-box">
                <span class="qr-text">${link.text}</span>
                <span class="qr-link" onClick="window.location.href='${link.url}';"><a href="${link.url}">X</a></span>
            </div>
        </#list>
    </div>
    <script>
        var facetsOpen = 0;
        $("div.searchTOC-box").corner();
        $(".search-facets-head").click(function() {
            if (facetsOpen) {
                if (facetsOpen == this) {
                    $(this).next("tr").hide();
                    $(this).find(".search-facets-toggle").html('+');
                    facetsOpen = 0;
                } else {
                    $(facetsOpen).next("tr").hide();
                    $(facetsOpen).find(".search-facets-toggle").html('+');
                    $(this).next("tr").show();
                    $(this).find(".search-facets-toggle").html('-');
                    facetsOpen = this;
                }
            } else {
                $(this).next("tr").show();
                $(this).find(".search-facets-toggle").html('-');
                facetsOpen = this;
            }
        });
    </script>
    <#-- Search results -->
    <div style="float: right; text-align: left; width: 75%;">
        <ul class="searchhits">
            <#list individuals as individual>
                <li>                        
                    <@shortView uri=individual.uri viewContext="search" />
                </li>
            </#list>
        </ul>
        <#-- Paging controls -->
        <#if (pagingLinks?size > 0)>
            <div class="searchpages">
                Pages: 
                <#if prevPage??><a class="prev" href="${prevPage}" title="${i18n().previous}">${i18n().previous}</a></#if>
                <#list pagingLinks as link>
                    <#if link.url??>
                        <a href="${link.url}" title="${i18n().page_link}">${link.text}</a>
                    <#else>
                        <span>${link.text}</span> <#-- no link if current page -->
                    </#if>
                </#list>
                <#if nextPage??><a class="next" href="${nextPage}" title="${i18n().next_capitalized}">${i18n().next_capitalized}</a></#if>
            </div>
        </#if>
    </div>
    <br />
    <#-- VIVO OpenSocial Extension by UCSF -->
    <#if openSocial??>
        <#if openSocial.visible>
        <h3>OpenSocial</h3>
            <script type="text/javascript" language="javascript">
                // find the 'Search' gadget(s).
                var searchGadgets = my.findGadgetsAttachingTo("gadgets-search");
                var keyword = '${querytext}';
                // add params to these gadgets
                if (keyword) {
                    for (var i = 0; i < searchGadgets.length; i++) {
                        var searchGadget = searchGadgets[i];
                        searchGadget.additionalParams = searchGadget.additionalParams || {};
                        searchGadget.additionalParams["keyword"] = keyword;
                    }
                }
                else {  // remove these gadgets
                    my.removeGadgets(searchGadgets);
                }
            </script>

            <div id="gadgets-search" class="gadgets-gadget-parent" style="display:inline-block"></div>
        </#if>
    </#if>
</div> <!-- end contentsBrowseGroup -->

${stylesheets.add('<link rel="stylesheet" href="//code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />',
  				  '<link rel="stylesheet" href="${urls.base}/css/search.css" />')}

${headScripts.add('<script src="//code.jquery.com/ui/1.10.3/jquery-ui.js"></script>',
				  '<script type="text/javascript" src="${urls.base}/js/jquery_plugins/qtip/jquery.qtip-1.0.0-rc3.min.js"></script>',
                  '<script type="text/javascript" src="${urls.base}/js/tiny_mce/tiny_mce.js"></script>'
                  )}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/searchDownload.js"></script>')}

<#-- $This file is distributed under the terms of the license in LICENSE$  -->

<@widget name="login" include="assets" />

<#--
        With release 1.6, the home page no longer uses the "browse by" class group/classes display.
        If you prefer to use the "browse by" display, replace the import statement below with the
        following include statement:

            <#include "browse-classgroups.ftl">

        Also ensure that the homePage.geoFocusMaps flag in the runtime.properties file is commented
        out.
-->
<#import "lib-home-page.ftl" as lh>

<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "head.ftl">
        <#if geoFocusMapsEnabled >
            <#include "geoFocusMapScripts.ftl">
        </#if>
        <script type="text/javascript" src="${urls.base}/js/homePageUtils.js?version=x"></script>
        <script src="${urls.base}/js/d3.min.js"></script>
        <script src="${urls.theme}/js/topojson.min.js"></script>
        <script src="${urls.theme}/js/datamaps.world.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/datamaps/0.5.8/datamaps.world.hires.min.js"></script>
    </head>

    <body class="${bodyClasses!}" onload="${bodyOnload!}">
    <#-- supplies the faculty count to the js function that generates a random row number for the search query -->
        <@lh.facultyMemberCount  vClassGroups! />
        <#include "identity.ftl">

        <#include "menu.ftl">

        <section id="intro" role="region">
            <table class="hbox-container" width="100%" height="100%" border="1" style="min-height: 600px; margin-top: 60px;">
                <tr height="50%">
                    <td width="50%" align="right" style="marging-right: 20px;">
                        <div class="hbox">
                            <p class="hbox-title">
                                Explore Global Collaboration
                            </p>
                            <p>
                                <a href="${urls.base}/copub"><img src="${urls.theme}/images/worldmap.png"/></a>
                            </p>
                            <div class="page-home-go">
                                <button class="go-button" onClick="window.location.href='${urls.base}/copub'">Go</button>
                            </div>
                        </div>
                    </td>
                    <td width="50%" align="left" style="marging-left: 20px;">
                        <div class="hbox">
                            <form id="search-homepage" action="${urls.search}" name="search-home" role="search" method="post" >
                                <p class="hbox-title">
                                    Search DTU publications in Web of Science
                                </p>
                                <p>
                                    <section id="search-home" role="region">
                                        <div id="search-home-field">
                                                <input type="text" name="querytext" class="search-homepage" value="" autocapitalize="off" />
                                        </div>
                                        <div style="text-align: left;">AND</div>
                                        <div>
                                            <select name="facetAsText">
                                                <option value="facetext_wos-category_en">Subject categories</option>
                                                <option value="facetext_document-type_en">Document types</option>
                                                <option value="facetext_research-area_en">Research areas</option>
                                                <option value="facetext_publication-year_en">Publication years</option>
                                                <option value="facetext_organization-enhanced_en">Organi.-Enhanced</option>
                                                <option value="facetext_journal_en">Journals</option>
                                                <option value="facetext_conference_en">Conferences</option>
                                                <option value="facetext_country_en">Countries</option>
                                                <option value="facetext_funding-agency_en">Funding Agencies</option>
                                            </select>
                                        </div>
                                        <div>
                                            <input class="search-homepage-facet" type="text" name="facetTextValue"/>
                                        </div>
                                    </section>
                                </p>
                                <div id="page-home-search">
                                    <input class="go-button" type="submit" value="Go"/>
                                </div>
                            </form>
                        </div>
                    </td>
                </tr>
                <tr height="50%">
                    <td width="50%" align="right" style="marging-right: 20px;">
                    </td>
                    <td width="50%" align="left" style="marging-left: 20px;">
                    </td>
                </tr>
            </table>
        </section> <!-- #intro -->

        <@widget name="login" />

        <#include "footer.ftl">
        <#-- builds a json object that is used by js to render the academic departments section -->
        <@lh.listAcademicDepartments />
    </body>
</html>

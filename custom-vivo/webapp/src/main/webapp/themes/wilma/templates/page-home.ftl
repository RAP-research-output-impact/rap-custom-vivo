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
        <script src="${urls.theme}/js/copub-util.js?ver=CACHEVERSION"></script>
        <script src="${urls.theme}/js/assessment.js?ver=CACHEVERSION"></script>
        <script>
            const urls_base = "${urls.base}";
            $(document).ready(function() {
                copub_update ();
                rapas_update ();
                $("div.VivoAccount").hide();
            });
        </script>
    </head>

    <body class="${bodyClasses!}" onload="${bodyOnload!}">
        <div id="main-content">
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
                                    DTU collaboration
                                </p>
                                <p>
                                    <a href="${urls.base}/copub-choose"><img src="${urls.theme}/images/worldmap.png"/></a>
                                </p>
                                <div class="page-home-go">
                                    <button class="go-button" onClick="window.location.href='${urls.base}/copub-choose'">Go</button>
                                </div>
                                <div class="page-home-copub-update">
                                <div>
                            </div>
                        </td>
                        <td width="50%" align="left" style="marging-left: 20px;">
                            <div class="hbox">
                                <p class="hbox-title">
                                    DTU publications
                                </p>
                                <p>
                                    <a href="${urls.base}/search?querytext=&classgroup=http%3A%2F%2Fvivoweb.org%2Fontology%23vitroClassGrouppublications"
                                       title="Publications"><img src="${urls.theme}/images/publications.png"/></a>
                                </p>
                                <div class="page-home-go">
                                    <button class="go-button" onClick="window.location.href='${urls.base}/search?querytext=&classgroup=http%3A%2F%2Fvivoweb.org%2Fontology%23vitroClassGrouppublications'">Go</button>
                                </div>
                                <div class="page-home-copub-update">
                                <div>
                            </div>
                        </td>
                    </tr>
                    <tr height="50%">
                        <td width="50%" align="right" style="marging-right: 20px;">
                            <div class="hbox">
                                <p class="hbox-title">
                                    DTU researchers
                                </p>
                                <p>
                                    <a href="${urls.base}/dtu-researchers" title="Researchers"><img src="${urls.theme}/images/researchers.png"/></a>
                                </p>
                                <div class="page-home-go">
                                    <button class="go-button" onClick="window.location.href='${urls.base}/dtu-researchers'">Go</button>
                                </div>
                                <div class="page-home-rapas-update">
                                <div>
                            </div>
                        </td>
                        <td width="50%" align="left" style="marging-left: 20px;">
                            <div class="hbox">
                                <p class="hbox-title">
                                    DTU units
                                </p>
                                <p>
                                    <a href="${urls.base}/dtu-units" title="Units"><img src="${urls.theme}/images/units.png"/></a>
                                </p>
                                <div class="page-home-go">
                                    <button class="go-button" onClick="window.location.href='${urls.base}/dtu-units'">Go</button>
                                </div>
                                <div class="page-home-rapas-update">
                                <div>
                            </div>
                        </td>
                    </tr>
                </table>
            </section>

            <@widget name="login" />

        </div>
        <#include "footer.ftl">
        <#-- builds a json object that is used by js to render the academic departments section -->
        <@lh.listAcademicDepartments />
    </body>
</html>

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
            <h2>${i18n().intro_title}</h2>

            <p>${i18n().intro_para1}</p>
            <p>${i18n().intro_para2}</p>

            <section id="search-home" role="region">
                <h3>${i18n().intro_searchvivo} <span class="search-filter-selected">filteredSearch</span></h3>

                <fieldset>
                    <legend>${i18n().search_form}</legend>
                    <form id="search-homepage" action="${urls.search}" name="search-home" role="search" method="post" >
                        <div id="search-home-field">
                            <input type="text" name="querytext" class="search-homepage" value="" autocapitalize="off" />
                            <input type="submit" value="${i18n().search_button}" class="search" />
                            <input type="hidden" name="classgroup"  value="" autocapitalize="off" />
                        </div>

                        <a class="filter-search filter-default" href="#" title="${i18n().intro_filtersearch}">
                            <span class="displace">${i18n().intro_filtersearch}</span>
                        </a>

                        <ul id="filter-search-nav">
                            <li><a class="active" href="">${i18n().all_capitalized}</a></li>
                            <@lh.allClassGroupNames vClassGroups! />
                        </ul>
                    </form>
                </fieldset>
            </section> <!-- #search-home -->

        </section> <!-- #intro -->

        <@widget name="login" />

        <!-- worldmap -->
        <section class="home-sections" id="worldmap">
            <h2>Co-publication Worldmap</h2>
            <div id="copub-map-container">
                <div id="copub-map"></div>
                <div id="copub-map-info">
                    <ul id="map-org-list">
                    </ul>
                </div>
            </div>
        </section>



        <#include "footer.ftl">
        <#-- builds a json object that is used by js to render the academic departments section -->
        <@lh.listAcademicDepartments />
    <script>
        var i18nStrings = {
            researcherString: '${i18n().researcher}',
            researchersString: '${i18n().researchers}',
            currentlyNoResearchers: '${i18n().currently_no_researchers}',
            countriesAndRegions: '${i18n().countries_and_regions}',
            countriesString: '${i18n().countries}',
            regionsString: '${i18n().regions}',
            statesString: '${i18n().map_states_string}',
            stateString: '${i18n().map_state_string}',
            statewideLocations: '${i18n().statewide_locations}',
            researchersInString: '${i18n().researchers_in}',
            inString: '${i18n().in}',
            noFacultyFound: '${i18n().no_faculty_found}',
            placeholderImage: '${i18n().placeholder_image}',
            viewAllFaculty: '${i18n().view_all_faculty}',
            viewAllString: '${i18n().view_all}',
            viewAllDepartments: '${i18n().view_all_departments}',
            noDepartmentsFound: '${i18n().no_departments_found}'
        };
        // set the 'limmit search' text and alignment
        if  ( $('input.search-homepage').css('text-align') == "right" ) {
             $('input.search-homepage').attr("value","${i18n().limit_search} \u2192");
        }

        //
        // co-pub world map
        //
        $('#copub-map-container').addClass('spinner');
        var base = "${urls.base}";
        var profileBase = base + "/individual?uri="
        var serviceBase = base + "/vds/report/"
        var mapData = serviceBase + "worldmap/";
        var countryData = serviceBase + "country/";

        var pubCounts = loadData(mapData, prepMapData);

        // See: https://bost.ocks.org/mike/bubble-map/
        // Create radius for bubles.
        var radius = d3.scale.sqrt()
            .domain([1, 5000])
            .range([5, 20]);

        function prepMapData(data) {
          var out = [];
          for (var i =0, j = data.summary.length; i < j; i++) {
            var d = {}
            var count = data.summary[i].publications;
            if (count > 0) {
                d['centered'] = data.summary[i].code;
                d['publications'] = count;
                d['fillKey'] = 'default';
                d['radius'] = radius(count);
                out.push(d)
            };
          }
          if (out.length > 0) {
            makeMap(out);
          } else {
            $('#copub-map-container').removeClass('spinner');
            $('#worldmap').hide();
          }
        }

        function makeMap(data) {
          document.getElementById("copub-map-info").style.visibility = "hidden";
          var countries = Datamap.prototype.worldTopo.objects.world.geometries;
          var countryKey = {}
          for (var i = 0, j = countries.length; i < j; i++) {
            countryKey[countries[i].id] = countries[i].properties.name;
          }
          //var dtucenter = [12.521533, 55.785797]
          var map = new Datamap({
            element: document.getElementById("copub-map"),
            scope: 'world',
            responsive: false,
            //mousewheel zoom
            done: function(datamap) {
                   datamap.svg.call(d3.behavior.zoom().on("zoom", redraw));

                   function redraw() {
                        datamap.svg.selectAll("g").attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
                   }
            },
            geographyConfig: {
              popupOnHover: true,
              highlightOnHover: true,
              highlightFillColor: '#DCDCDC',
              highlightBorderColor: 'white',
              hideHawaiiAndAlaska : true
            },
            bublesConfig: {
                key: null
            },
            fills: {
              defaultFill: '#d6cece',
              default: '#b20000',
            },
            data: data
          });
          map.bubbles(data, {
            popupTemplate: function(geo, data) {
              var country = countryKey[data.centered];
              return '<div class="hoverinfo">' + country + ' ' + data.publications + ' co-publications</div>'
            }
          });

          //https://stackoverflow.com/a/34958824/758157
          d3.selectAll(".datamaps-bubble").on('click', function(bubble) {
            //console.log(bubble);
            document.getElementById("copub-map-info").style.visibility = "visible";
            loadData(countryData + bubble.centered, orgList);
          });
          $('#copub-map-container').removeClass('spinner');
        }

        function orgList(data) {
          var contentDiv = document.getElementById("map-org-list");
          contentDiv.innerHTML = "";
          for (var i =0, j = data.orgs.length; i < j; i++){
            liHTML = "<li><a href=\"" + profileBase+ data.orgs[i].org + "\">" + data.orgs[i].name + "</a> (" + data.orgs[i].publications + ")</li>";
            contentDiv.innerHTML += liHTML;
          }
        }


        function loadData(url, callback) {

          var xhr = new XMLHttpRequest();
            xhr.open('GET', url );
            xhr.onload = function() {
                if (xhr.status === 200) {
                    var response = JSON.parse(xhr.response)
                    callback(response);
                }
                else {
                    alert('Request failed.  Returned status of ' + xhr.status);
                }
            };
            xhr.send();
        }

    </script>
    </body>
</html>

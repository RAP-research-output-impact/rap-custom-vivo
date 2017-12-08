<#import "lib-home-page.ftl" as lh>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="${urls.base}/js/d3.min.js"></script>
<script src="${urls.theme}/js/topojson.min.js"></script>
<script src="${urls.theme}/js/datamaps.world.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/datamaps/0.5.8/datamaps.world.hires.min.js"></script>
<script src="${urls.theme}/js/copub.js"></script>
<script src="${urls.theme}/js/jquery.sortElements.js"></script>

<!-- worldmap -->
<section class="home-sections" id="worldmap">
    <div id="copub-map-container">
        <div id="copub-map">
            <button id="zoom-button-out" class="zoom-button" data-zoom="out">-</button>
            <div id="zoom-info"></div>
            <button id="zoom-button-in" class="zoom-button" data-zoom="in">+</button>
        </div>
        <div id="copub-map-info" style="display: inline-block; float: left;">
            <table id="map-org-list">
                <thead>
                    <tr>
                        <th style="text-align: left; vertical-align: middle; width: 600px;">
                            <div id="sort-org">Organisation
                                <span class="sort-dir"></span>
                            </div>
                            <form style="display: inline-block; float: right;">
                                <input id="copub-filter" type="text" size="30" placeholder="filter by..." style="margin: 0px;"/>
                            </form>
                        </th>
                        <th id="sort-pub" style="text-align: right; vertical-align: middle;">
                            Co-publications
                            <span class="sort-dir"></span>
                        </th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div id="min-map" style="display: inline-block; float: right; margin-top: 20px; border: 1px solid #aaaaaa; background-color: white;">
            <img src="${urls.theme}/images/worldmap.png"/>
        </div>
    </div>
</section>

<#-- builds a json object that is used by js to render the academic departments section -->
<@lh.listAcademicDepartments/>

<script>
    $("#copub-map-info").hide();
    $('#min-map').hide();
    $("#copub-filter").keyup(function() {
        $(".map-org-org").each(function() {
            if ($(this).text().search(new RegExp($("#copub-filter").val(), "i")) != -1) {
                $(this).parent().show();
            } else {
                $(this).parent().hide();
            }
        });
    });
//  ??? is this actually used?
    var i18nStrings = {
        researcherString:       '${i18n().researcher}',
        researchersString:      '${i18n().researchers}',
        currentlyNoResearchers: '${i18n().currently_no_researchers}',
        countriesAndRegions:    '${i18n().countries_and_regions}',
        countriesString:        '${i18n().countries}',
        regionsString:          '${i18n().regions}',
        statesString:           '${i18n().map_states_string}',
        stateString:            '${i18n().map_state_string}',
        statewideLocations:     '${i18n().statewide_locations}',
        researchersInString:    '${i18n().researchers_in}',
        inString:               '${i18n().in}',
        noFacultyFound:         '${i18n().no_faculty_found}',
        placeholderImage:       '${i18n().placeholder_image}',
        viewAllFaculty:         '${i18n().view_all_faculty}',
        viewAllString:          '${i18n().view_all}',
        viewAllDepartments:     '${i18n().view_all_departments}',
        noDepartmentsFound:     '${i18n().no_departments_found}'
    };
//  ??? is this actually used?
    // set the 'limit search' text and alignment
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
    // Create radius for bubbles.
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
        var countries = Datamaps.prototype.worldTopo.objects.world.geometries;
        var countryKey = {}
        for (var i = 0, j = countries.length; i < j; i++) {
            countryKey[countries[i].id] = countries[i].properties.name;
        }
        var map = new Datamapy(data);
        console.log (map);
        map.instance.bubbles(data, {
            popupTemplate:
                function(geo, data) {
                    var country = countryKey[data.centered];
                    return '<div class="hoverinfo">' + country + ' ' + data.publications + ' co-publications</div>'
                }
        });
        $("#min-map").click(function() {
            $('#copub-map').show();
            $('#copub-map-info').hide();
            $('#min-map').hide();
        });
        d3.selectAll(".datamaps-bubble").on('click', function(bubble) {
            $("#copub-filter").val("")
            loadData(countryData + bubble.centered, orgList);
            var irec = $('#copub-map-info').get(0).getBoundingClientRect();
            var view = Math.max(document.documentElement.clientHeight, window.innerHeight);
            console.log (irec);
            console.log (view);
            $('#copub-map').hide();
            $('#min-map').show();
            $('#copub-map-info').show();
            if ((irec.top + 110) > view) {
                window.scrollTo(0, (irec.top + 110 - view + document.documentElement.scrollTop));
            }
        });
        map.instance.svg.selectAll('.datamaps-bubble')
            .each(function (d) {
                d3.select(this).attr('ro', d3.select(this).attr('r'))
        });
        $('#copub-map-container').removeClass('spinner');
    }

    function orgList(data) {
        var tbody = "";
        for (var i = 0, j = data.orgs.length; i < j; i++){
            tbody += "<tr><td class=\"map-org-org sort-org\"><a href=\"" + profileBase + data.orgs[i].org + "\">" + data.orgs[i].name +
                     "</a></td><td class=\"sort-pub\" style=\"text-align: right;\">" + data.orgs[i].publications + "</td></tr>";
        }
        $("#map-org-list tbody").html(tbody);
        $("#sort-pub .sort-dir").html ('&uarr;');
        $('#sort-org').each(function() {
            var inverse = false;
            $(this).click(function() {
                $("td.sort-org").sortElements(function(a, b) {
                    return $.text([a]) > $.text([b]) ?
                           inverse ? -1 : 1
                         : inverse ? 1 : -1;
                }, function() {
                    return this.parentNode;
                });
                if (inverse) {
                    $("#sort-org .sort-dir").html ('&uarr;');
                } else {
                    $("#sort-org .sort-dir").html ('&darr;');
                }
                $("#sort-pub .sort-dir").html ('');
                inverse = !inverse;
            });
        });
        $('#sort-pub').each(function() {
            var inverse = false;
            $(this).click(function() {
                $("td.sort-pub").sortElements(function(a, b) {
                    return Number($.text([a])) > Number($.text([b])) ?
                           inverse ? -1 : 1
                         : inverse ? 1 : -1;
                }, function() {
                    return this.parentNode;
                });
                if (inverse) {
                    $("#sort-pub .sort-dir").html ('&uarr;');
                } else {
                    $("#sort-pub .sort-dir").html ('&darr;');
                }
                $("#sort-org .sort-dir").html ('');
                inverse = !inverse;
            });
        });
    }

    function loadData(url, callback) {
        var xhr = new XMLHttpRequest();

        xhr.open('GET', url);
        xhr.onload = function() {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.response)
                callback(response);
            } else {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    }
</script>

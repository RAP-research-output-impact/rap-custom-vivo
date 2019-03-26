<#import "lib-home-page.ftl" as lh>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="${urls.base}/js/d3.min.js"></script>
<script src="${urls.theme}/js/topojson.min.js"></script>
<script src="${urls.theme}/js/datamaps.world.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/datamaps/0.5.8/datamaps.world.hires.min.js"></script>
<script src="${urls.theme}/js/copub.js"></script>
<script src="${urls.theme}/js/jquery.sortElements.js"></script>

<section class="home-sections" id="worldmap">
    <div id="copub-breadcrumbs" style="margin-top: 20px;">
        <a href="../copub-choose">Ext. collaboration</a>
        &gt;
        <span id="bc-dep">
            <select id="dtu-dep" name="dtu-dep">
                <option value="">Entire university</option>
                <option value="dtusuborg-centre-for-oil-and-gas-dtu">Centre for Oil and Gas - DTU</option>
                <option value="dtusuborg-dtu-aqua">DTU Aqua</option>
                <option value="dtusuborg-dtu-bioengineering">DTU Bioengineering</option>
                <option value="dtusuborg-dtu-bioinformatics">DTU Bioinformatics</option>
                <option value="dtusuborg-dtu-biosustain">DTU Biosustain</option>
                <option value="dtusuborg-dtu-business">DTU Business</option>
                <option value="dtusuborg-dtu-chemical-engineering">DTU Chemical Engineering</option>
                <option value="dtusuborg-dtu-chemistry">DTU Chemistry</option>
                <option value="dtusuborg-dtu-civil-engineering">DTU Civil Engineering</option>
                <option value="dtusuborg-dtu-compute">DTU Compute</option>
                <option value="dtusuborg-dtu-danchip">DTU Danchip</option>
                <option value="dtusuborg-dtu-diplom">DTU Diplom</option>
                <option value="dtusuborg-dtu-electrical-engineering">DTU Electrical Engineering</option>
                <option value="dtusuborg-dtu-energy">DTU Energy</option>
                <option value="dtusuborg-dtu-environment">DTU Environment</option>
                <option value="dtusuborg-dtu-food">DTU Food</option>
                <option value="dtusuborg-dtu-fotonik">DTU Fotonik</option>
                <option value="dtusuborg-dtu-health-tech">DTU Health Tech</option>
                <option value="dtusuborg-dtu-management-engineering">DTU Management Engineering</option>
                <option value="dtusuborg-dtu-mechanical-engineering">DTU Mechanical Engineering</option>
                <option value="dtusuborg-dtu-nanotech">DTU Nanotech</option>
                <option value="dtusuborg-dtu-nutech">DTU Nutech</option>
                <option value="dtusuborg-dtu-physics">DTU Physics</option>
                <option value="dtusuborg-dtu-space">DTU Space</option>
                <option value="dtusuborg-dtu-systems-biology">DTU Systems Biology</option>
                <option value="dtusuborg-dtu-vet">DTU Vet</option>
                <option value="dtusuborg-dtu-wind-energy">DTU Wind Energy</option>
                <option value="dtusuborg-dtu-department-unknown">DTU department unknown</option>
                <option value="dtusuborg-ris-dtu">Risø DTU</option>
            </select>
        </span>
        <span id="bc-dep-view">
        </span>
        <script>
            const urlParams = new URLSearchParams(window.location.search);
            $("#dtu-dep").val(urlParams.get('dept'));
        </script>
        &gt;
        <span id="bc-world-map">
           Partners 
        </span>
        <span id="bc-world-map-link">
            <a id="bc-world-map-link-anchor">World map</a>
        </span>
        &gt;
        <span id="bc-country">
        </span>
        <span id="bc-range">
            From
            <select id="year-from" name="year-from">
                <option value="2007">2007</option>
                <option value="2008">2008</option>
                <option value="2009">2009</option>
                <option value="2010">2010</option>
                <option value="2011">2011</option>
                <option value="2012">2012</option>
                <option value="2013">2013</option>
                <option value="2014" selected="selected">2014</option>
                <option value="2015">2015</option>
                <option value="2016">2016</option>
                <option value="2017">2017</option>
                <option value="2018">2018</option>
                <option value="2019">2019</option>
            </select>
            -
            <select id="year-to" name="year-to">
                <option value="2007">2007</option>
                <option value="2008">2008</option>
                <option value="2009">2009</option>
                <option value="2010">2010</option>
                <option value="2011">2011</option>
                <option value="2012">2012</option>
                <option value="2013">2013</option>
                <option value="2014">2014</option>
                <option value="2015">2015</option>
                <option value="2016">2016</option>
                <option value="2017">2017</option>
                <option value="2018">2018</option>
                <option value="2019" selected="selected">2019</option>
            </select>
        </span>
        <span id="bc-range-view">
        </span>
    </div>
    <div id="copub-map-container">
        <div id="copub-map-info" style="display: inline-block; float: left;">
            <table>
                <tr>
                    <td>
                        <table id="map-org-list">
                            <thead>
                                <tr>
                                    <th style="text-align: left; vertical-align: middle; min-width: 600px; background-color: #3d423d; color: white;">
                                        <div id="sort-org" style="color: white;">
                                            Partners
                                            <div class="sort-dir" style="height: 23px;"></div>
                                        </div>
                                        <form style="display: inline-block; float: right;" onSubmit="return (false);">
                                            <input id="copub-filter" type="text" size="30" placeholder="Type here to shorten list" style="margin: 0px;"/>
                                        </form>
                                    </th>
                                    <th id="sort-pub" style="text-align: right; vertical-align: middle; min-width: 200px; background-color: #3d423d; color: white;">
                                        Co-publications
                                        <div class="sort-dir"></div>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                            </tbody>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</section>

<#-- builds a json object that is used by js to render the academic departments section -->
<@lh.listAcademicDepartments/>

<script>
    $("#copub-map-info").show();
    $("#bc-world-map-link").hide();
    $("#bc-world-map-link-anchor").click(function() {
        $("#bc-world-map-link").hide();
        $("#bc-world-map").show();
        $("#bc-country").hide();
        $("#copub-map-heading").show();
        $('#copub-map').hide();
        $('#copub-map-info').hide();
        $("#bc-range").show();
        $("#bc-range-view").hide();
        $("#bc-dep").show();
        $("#bc-dep-view").hide();
    });
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
    var base = "${urls.base}";
    var profileBase = base + "/individual?uri="
    var serviceBase = base + "/vds/report/"

    fetchPartnerData();

    $("#dtu-dep").change(function() {
        console.log ("dtu-dep changed");
        fetchPartnerData();
    });
    $("#year-from").change(function() {
        console.log ("year-from changed");
        fetchPartnerData();
    });
    $("#year-to").change(function() {
        console.log ("year-to changed");
        fetchPartnerData();
    });

    function fetchPartnerData() {
        var mapData = serviceBase + "partners?dept=" + $("#dtu-dep").val() + "&startYear=" + $('#year-from').val() + "&endYear=" + $('#year-to').val();
        console.log ("loading: " + mapData);
        $('#partners-container').addClass('spinner');
        loadData(mapData, orgList);
    }

    function orgList(data) {
        var tbody = "";
        for (var i = 0, j = data.partners.length; i < j; i++){
            tbody += "<tr><td class=\"map-org-org sort-org\"><a href=\"" + profileBase + data.partners[i].partner + "\">" + data.partners[i].name +
                     "</a></td><td class=\"sort-pub\" style=\"text-align: right;\">" + data.partners[i].publications + "</td></tr>";
        }
        $("#map-org-list tbody").html(tbody);
        $("#sort-pub .sort-dir").html (sortArrow (1, 1));
        $("#sort-org .sort-dir").html (sortArrow (0, 0));
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
                    $("#sort-org .sort-dir").html (sortArrow (1, 1));
                } else {
                    $("#sort-org .sort-dir").html (sortArrow (0, 1));
                }
                $("#sort-pub .sort-dir").html (sortArrow (0, 0));
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
                    $("#sort-pub .sort-dir").html (sortArrow (1, 1));
                } else {
                    $("#sort-pub .sort-dir").html (sortArrow (0, 1));
                }
                $("#sort-org .sort-dir").html (sortArrow (0, 0));
                inverse = !inverse;
            });
        });
        $('#partners-container').removeClass('spinner');
    }

    function sortArrow(up, used) {
        var svg = '<svg height="14" width="24">';
        if (up) {
            if (used) {
                svg += '<polygon points="12,2 22,12 2,12 12,2" style="fill:red;stroke:white;stroke-width:2" />';
            } else {
                svg += '<polygon points="12,2 22,12 2,12 12,2" style="fill:none;stroke:white;stroke-width:2" />';
            }
        } else {
            if (used) {
                svg += '<polygon points="12,12 2,2 22,2 12,12" style="fill:red;stroke:white;stroke-width:2" />';
            } else {
                svg += '<polygon points="12,12 2,2 22,2 12,12" style="fill:none;stroke:white;stroke-width:2" />';
            }
        }
        svg += '</svg>';
        return svg;
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

<#import "lib-home-page.ftl" as lh>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="${urls.base}/js/d3.min.js"></script>
<script src="${urls.theme}/js/topojson.min.js"></script>
<script src="${urls.theme}/js/datamaps.world.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/datamaps/0.5.8/datamaps.world.hires.min.js"></script>
<script src="${urls.theme}/js/copub.js"></script>
<script src="${urls.theme}/js/copub-util.js"></script>
<script src="${urls.theme}/js/jquery.sortElements.js"></script>

<!-- worldmap -->
<section class="home-sections" id="worldmap">
    <div id="copub-breadcrumbs" style="margin-top: 20px;">
        <a href="../copub-choose">Ext. collaboration</a>
        &gt;
        <span id="bc-dept-container">
        </span>
        &gt;
        <span id="bc-world-map">
            World map
        </span>
        <span id="bc-world-map-link">
            <a id="bc-world-map-link-anchor">World map</a>
        </span>
        &gt;
        <span id="bc-country">
        </span>
        <span id="bc-range-container">
        </span>
    </div>
    <div id="copub-map-container">
        <table id="map-country-list" style="margin-bottom: 60px; margin-left: 10px;">
            <thead>
                <tr>
                    <th style="text-align: left; vertical-align: middle; min-width: 600px; background-color: #3d423d; color: white;">
                        <div id="sort-cty" style="color: white;">
                            Country
                            <div class="sort-dir" style="height: 23px;"></div>
                        </div>
                        <form style="display: inline-block; float: right;" onSubmit="return (false);">
                            <input id="copub-cty-filter" type="text" size="30" placeholder="Type here to shorten list" style="margin: 0px;"/>
                        </form>
                    </th>
                    <th id="sort-cty-pub" style="text-align: right; vertical-align: middle; min-width: 200px; background-color: #3d423d; color: white;">
                        Co-publications
                        <div class="sort-dir"></div>
                    </th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
        <div id="copub-map-info" style="display: inline-block; float: left;">
            <table>
                <tr>
                    <td>
                        <table id="map-org-list">
                            <thead>
                                <tr>
                                    <th style="text-align: left; vertical-align: middle; min-width: 600px; background-color: #3d423d; color: white;">
                                        <div id="sort-org" style="color: white;">
                                            Collaboration partners
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
    // See: https://bost.ocks.org/mike/bubble-map/
    // Create radius for bubbles.
    var radius = d3.scale.sqrt()
        .domain([1, 5000])
        .range([5, 20]);
    const urls_base = "${urls.base}";

    $(document).ready(function() {
        dept_bc_setup("bc-dept-container", fetchMapData);
        range_bc_setup("bc-range-container", fetchMapData);
        $("#copub-map-info").hide();
        $("#bc-world-map-link").hide();
        $("#bc-world-map-link-anchor").click(function() {
            $("#bc-world-map-link").hide();
            $("#bc-world-map").show();
            $("#bc-country").hide();
            $('#map-country-list').show();
            $('#copub-map-info').hide();
            dept_bc_edit();
            range_bc_edit();
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
        $("#copub-cty-filter").keyup(function() {
            $(".map-cty-cty").each(function() {
                if ($(this).text().search(new RegExp($("#copub-cty-filter").val(), "i")) != -1) {
                    $(this).parent().show();
                } else {
                    $(this).parent().hide();
                }
            });
        });
        fetchMapData();
    });

    function fetchMapData() {
        var mapData = "${urls.base}/vds/report/worldmap?dept=" + dept_val() + "&startYear=" + range_from_val() + "&endYear=" + range_to_val();
        console.log ("loading: " + mapData);
        $("#map-country-list tbody").html("");
        loadData(mapData, prepMapData);
    }

    function prepMapData(data) {
        var countries = Datamaps.prototype.worldTopo.objects.world.geometries;
        var countryKey = {}
        for (var i = 0, j = countries.length; i < j; i++) {
            countryKey[countries[i].id] = countries[i].properties.name;
        }
        var html;
        for (var i = 0, j = data.summary.length; i < j; i++) {
            var d = {};
            var code = data.summary[i].code;
            var name;
            if (countryKey[code]) {
                name = countryKey[code];
            } else {
                name = code;
            }
            var count = data.summary[i].publications;
            html += "<tr><td class=\"sort-cty map-cty-cty\"><a href=\"javascript:partnerList ('" + code + "', '" + name + "');\">" + name +
                    "</a></td><td class=\"sort-cty-pub\">" + count + "</td></tr>";
        }
        $("#map-country-list tbody").html(html);
        $("#copub-cty-filter").val("");
        $("#sort-cty-pub .sort-dir").html (sortArrow (1, 1));
        $("#sort-cty .sort-dir").html (sortArrow (0, 0));
        var inverse1 = false;
        var inverse2 = false;
        $('#sort-cty').each(function() {
            $(this).click(function() {
                $("td.sort-cty").sortElements(function(a, b) {
                    return $.text([a]) > $.text([b]) ?
                           inverse1 ? -1 : 1
                         : inverse1 ? 1 : -1;
                }, function() {
                    return this.parentNode;
                });
                if (inverse1) {
                    $("#sort-cty .sort-dir").html (sortArrow (1, 1));
                } else {
                    $("#sort-cty .sort-dir").html (sortArrow (0, 1));
                }
                $("#sort-cty-pub .sort-dir").html (sortArrow (0, 0));
                inverse1 = !inverse1;
                inverse2 = false;
            });
        });
        $('#sort-cty-pub').each(function() {
            $(this).click(function() {
                $("td.sort-cty-pub").sortElements(function(a, b) {
                    return Number($.text([a])) > Number($.text([b])) ?
                           inverse2 ? -1 : 1
                         : inverse2 ? 1 : -1;
                }, function() {
                    return this.parentNode;
                });
                if (inverse2) {
                    $("#sort-cty-pub .sort-dir").html (sortArrow (1, 1));
                } else {
                    $("#sort-cty-pub .sort-dir").html (sortArrow (0, 1));
                }
                $("#sort-cty .sort-dir").html (sortArrow (0, 0));
                inverse2 = !inverse2;
                inverse1 = false;
            });
        });
    }

    function partnerList(code, name) {
        $("#copub-filter").val("");
        $("#map-org-list tbody").html("<tr><td colspan=\"2\">Loading...</td></tr>");
        var countryDataURL = "${urls.base}/vds/report/country/" + code + "?dept=" + dept_val() + "&startYear=" + range_from_val() +
                             "&endYear=" + range_to_val();
        loadData(countryDataURL, orgList);

        $("#bc-world-map-link").show();
        $("#bc-world-map").hide();
        dept_bc_view();
        range_bc_view();

        $("#bc-country").html(name + " &gt; ");
        $("#bc-country").show();
        $('#map-country-list').hide();
        $('#copub-map-info').show();
    }

</script>

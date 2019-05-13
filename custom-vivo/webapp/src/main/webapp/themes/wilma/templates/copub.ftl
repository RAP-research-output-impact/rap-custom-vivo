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
    <div id="copub-map-heading">
        <div id="copub-map-sub-title" style="text-align: center;">
            Zoom using your mouse scroll wheel, or the controls top right, and select a country.
        </div>
    </div>
    <div id="copub-map-container">
        <div id="copub-map" style="margin-bottom: 60px;">
        </div>
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
        bc_dept_setup("bc-dept-container", fetchMapData);
        bc_range_setup("bc-range-container", fetchMapData);
        $("#copub-map-info").hide();
        $("#bc-world-map-link").hide();
        $("#bc-world-map-link-anchor").click(function() {
            $("#bc-world-map-link").hide();
            $("#bc-world-map").show();
            $("#bc-country").hide();
            $("#copub-map-heading").show();
            $('#copub-map').show();
            $('#copub-map-info').hide();
            bc_dept_edit();
            bc_range_edit();
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
        fetchMapData();
    });

    function addZoom() {
        var html = "<div style=\"float: right; margin-right: 5px;\">" +
                   "    <button id=\"zoom-button-out\" class=\"zoom-button\" data-zoom=\"out\">-</button>" +
                   "    <div id=\"zoom-info\"></div>" +
                   "    <button id=\"zoom-button-in\" class=\"zoom-button\" data-zoom=\"in\">+</button>" +
                   "</div>";
        $("#copub-map").html(html);
    }

    function fetchMapData() {
        var mapData = "${urls.base}/vds/report/worldmap?dept=" + dept_val() + "&startYear=" + range_from_val() + "&endYear=" + range_to_val();
        console.log ("loading: " + mapData);
        $('#copub-map-container').addClass('spinner');
        addZoom();
        loadData(mapData, prepMapData);
    }

    function prepMapData(data) {
        var out = [];
        for (var i = 0, j = data.summary.length; i < j; i++) {
            var d = {};
            var count = data.summary[i].publications;
            if (count > 0) {
                d['centered'] = data.summary[i].code;
                d['publications'] = count;
                d['fillKey'] = 'default';
                d['radius'] = radius(count);
                out.push(d)
            };
        }
        makeMap(out);
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
        d3.selectAll(".datamaps-bubble").on('click', function(bubble) {
            $("#copub-filter").val("");
            $("#map-org-list tbody").html("<tr><td colspan=\"2\">Loading...</td></tr>");


            var countryDataURL = "${urls.base}/vds/report/country/" + bubble.centered + "?dept=" + dept_val() + "&startYear=" + range_from_val() +
                                 "&endYear=" + range_to_val();
            loadData(countryDataURL, orgList);
            var irec = $('#copub-map-info').get(0).getBoundingClientRect();
            var view = Math.max(document.documentElement.clientHeight, window.innerHeight);
            console.log (irec);
            console.log (view);

            $("#bc-world-map-link").show();
            $("#bc-world-map").hide();
            bc_dept_view();
            bc_range_view();

            if (countryKey[bubble.centered]) {
                $("#bc-country").html(countryKey[bubble.centered] + " &gt; ");
            } else {
                $("#bc-country").html(bubble.centered + " &gt; ");
            }
            $("#bc-country").show();
            $("#copub-map-heading").hide();
            $('#copub-map').hide();
            $('#copub-map-info').show();
        });
        map.instance.svg.selectAll('.datamaps-bubble')
            .each(function (d) {
                d3.select(this).attr('ro', d3.select(this).attr('r'))
        });
        $('#copub-map-container').removeClass('spinner');
    }

</script>

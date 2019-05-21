<#import "lib-home-page.ftl" as lh>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="${urls.base}/js/d3.min.js"></script>
<script src="${urls.theme}/js/topojson.min.js"></script>
<script src="${urls.theme}/js/datamaps.world.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/datamaps/0.5.8/datamaps.world.hires.min.js"></script>
<script src="${urls.theme}/js/copub.js"></script>
<script src="${urls.theme}/js/copub-util.js"></script>
<script src="${urls.theme}/js/jquery.sortElements.js"></script>

<section class="home-sections" id="copub">
    <div id="copub-breadcrumbs">
        <a href="../copub-choose">Ext. collaboration</a>
        &gt;
        <span id="bc-dept-container">
        </span>
        &gt;
        <span id="bc-copub-type">
            World map
        </span>
        <span id="bc-copub-type-link">
            <a id="bc-copub-type-link-anchor">World map</a>
        </span>
        &gt;
        <span id="bc-main">
        </span>
        <span id="bc-range-container">
        </span>
    </div>
    <div id="copub-map-heading">
        <div id="copub-map-sub-title" style="text-align: center;">
            Zoom using your mouse scroll wheel, or the controls top right, and select a country.
        </div>
    </div>
    <div id="copub-container">
        <div id="copub-map" style="margin-bottom: 60px;">
        </div>
        <table id="copub-org-list">
            <thead>
                <tr>
                    <th style="text-align: left; min-width: 600px;">
                        <div id="sort-org">
                            Collaboration partners
                            <div class="sort-dir"></div>
                        </div>
                        <form class="copub-filter-form" onSubmit="return (false);">
                            <input id="copub-org-filter" type="text" size="30" placeholder="Type here to shorten list"/>
                        </form>
                    </th>
                    <th id="sort-org-pub">
                        Co-publications
                        <div class="sort-dir"></div>
                    </th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
</section>
<script>
    const urls_base = "${urls.base}";
    // Create radius for bubbles (see: https://bost.ocks.org/mike/bubble-map/).
    var radius = d3.scale.sqrt()
        .domain([1, 5000])
        .range([5, 20]);

    $(document).ready(function() {
        bc_dept_setup("bc-dept-container", fetchMapData);
        bc_range_setup("bc-range-container", fetchMapData);
        $("#copub-org-list").hide();
        $("#bc-copub-type-link").hide();
        $("#bc-copub-type-link-anchor").click(function() {
            $("#bc-copub-type-link").hide();
            $("#bc-copub-type").show();
            $("#bc-main").hide();
            $("#copub-map-heading").show();
            $('#copub-map').show();
            $('#copub-org-list').hide();
            bc_dept_edit();
            bc_range_edit();
        });
        filter_setup("org");
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
        $('#copub-container').addClass('spinner');
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
            $("#copub-org-filter").val("");
            $("#copub-org-list tbody").html("<tr><td colspan=\"2\">Loading...</td></tr>");


            var name = '';
            if (countryKey[bubble.centered]) {
                name = countryKey[bubble.centered];
            } else {
                name = bubble.centered;
            }
            fetchOrgList("country", "", bubble.centered, name);
            $("#copub-map-heading").hide();
            $('#copub-map').hide();
        });
        map.instance.svg.selectAll('.datamaps-bubble')
            .each(function (d) {
                d3.select(this).attr('ro', d3.select(this).attr('r'))
        });
        $('#copub-container').removeClass('spinner');
    }
</script>

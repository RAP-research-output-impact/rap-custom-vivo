<#import "lib-home-page.ftl" as lh>

        <script src="${urls.base}/js/d3.min.js"></script>
        <script src="${urls.theme}/js/topojson.min.js"></script>
        <script src="${urls.theme}/js/datamaps.world.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/datamaps/0.5.8/datamaps.world.hires.min.js"></script>


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
               datamap.svg.call(d3.behavior.zoom()
                   .scaleExtent([1, 3])
                   .on("zoom", redraw));

               function redraw() {
                    datamap.svg.selectAll("g").attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
//                  datamap.svg.selectAll('.datamaps-bubble').attr('r', bubbleRadius / d3.event.scale);
                    map.svg.selectAll('.datamaps-bubble')
                      .each(function (d) {
                          if (d3.select(this).attr('ro') > 0) {
                              d3.select(this).attr('r', d3.select(this).attr('ro') / d3.event.scale);
                          } else {
                              d3.select(this).attr('ro', d3.select(this).attr('r'));
                              d3.select(this).attr('r', d3.select(this).attr('ro') / d3.event.scale);
                          }
                      });
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
      map.svg.selectAll('.datamaps-bubble')
          .each(function (d) {
              d3.select(this).attr('ro', d3.select(this).attr('r'))
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

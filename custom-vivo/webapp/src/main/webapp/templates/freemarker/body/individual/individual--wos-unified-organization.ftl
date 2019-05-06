<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Individual profile page template for foaf:Organization individuals (extends individual.ftl in vivo)-->

<#-- Do not show the link for temporal visualization unless it's enabled -->

<script src="${urls.theme}/js/jquery.corner.js"></script>
<script src="${urls.theme}/js/jquery.sortElements.js"></script>
<script src="${urls.theme}/js/d3-v5.min.js"></script>
<script src="${urls.theme}/js/copub-util.js"></script>

<#assign affiliatedResearchAreas>
    <#include "individual-affiliated-research-areas.ftl">
</#assign>

<#include "individual.ftl">
<div style="height: 60px;"></div>


<script>

var individualUri = "${individual.uri}";

var svgStr1;
var svgStr2;

//co-publication report
$("span.display-title").html('');
var uni = $("h1.fn").text();
$("h1.fn").html("DTU collaboration with " + uni.trim() +
                "<span id=\"collab-summary-country\" class=\"hidden\"></span>" +
                " - " +
                "<span id=\"range-container\"></span>");
range_bc_setup("range-container", range_change); 
if (individualLocalName != "org-technical-university-of-denmark") {
    var orgLocalName = individualLocalName;
    var base = "${urls.base}";
    var vds = base + '/vds/report/org/' + individualLocalName;
    var vdsOrgs = base + '/vds/report/org/' + individualLocalName + "/orgs";
    var byDeptUrl = base + '/vds/report/org/' + individualLocalName + "/by-dept";
    info_message_setup();
    info_message("Loading Co-publication report");
    var searchLink = base + "/search?classgroup=http%3A%2F%2Fvivoweb.org%2Fontology%23vitroClassGrouppublications&querytext=&facet_organization-enhanced_ss=" + encodeURIComponent(individualUri);

    var html = `
        <h2 id="collab-summary">
            <span id="collab-summary-total"></span> co-publications
            <span id="collab-summary-cat"></span>
            <form id='export-report_form' method='post', action=''>
              <input class='export-report export-report_local-name' name='orgLocalName' type='text' hidden value='' />
              <input class='export-report export-report_start-year' name='startYear' type='text' hidden value='' />
              <input class='export-report export-report_end-year' name='endYear' type='text' hidden value='' />
              <input class='export-report export-report_svg1' type='text' name='svgStr1' hidden value='' />
              <input class='export-report export-report_svg2' type='text' name='svgStr2' hidden value='' />
              <button type='submit' class="export-report_btn">Download Excel</button>
            </form>
        </h2>
        <p><a href="--link--">Show list</a> of all publications since 2007</p>
    `.replace('--link--', searchLink);
    $("#startYear").corner();
    $("#endYear").corner();
    $("#individual-info").append(html);
    setExportFormBase();
    setExportForm();

    loadPubInfoByStartYear(vds, range_from_val(), range_to_val(), collabSummary);
    document.addEventListener('click', function (e) {
        if (hasClass(e.target, 'view-dept')) {
            $(e.target).parents('tr').nextUntil('.copubdept-head').toggle();
            label = $(e.target);
            if(label.html()=="Expand to show details"){
                label.html('Collapse to hide details');
            }else{
                label.html('Expand to show details');
            }
            return false;
        }
    }, false);
}

function range_change() {
    setExportForm();
    info_message("Updating Co-publication report for year range " + range_from_val() + ' - ' + range_to_val());
    loadPubInfoByStartYear(vds, range_from_val(), range_to_val(), collabSummary);
}

function info_message_setup() {
    $("ul.propertyTabsList").hide();
    $("section.property-group").hide();
    $("#individual-info").append("<div id=\"info-message\"></div>");
}

function info_message(msg) {
    $("#info-message").html(msg + "<img src=\"${urls.theme}/images/loading.gif\"/>");
}

function info_message_reset() {
    $("div#info-message").html ("");
    $("td.rep-num").each(function() {
        if ($(this).find("a").length) {
            $(this).find("a").each(function() {
                $(this).html($(this).html().replace(/\B(?=(\d{3})+(?!\d))/g, " "));
            });
        } else {
            $(this).html($(this).html().replace(/\B(?=(\d{3})+(?!\d))/g, " "));
        }
    });
    $(".rep-row").hover(function() {
        var id = $(this).attr("id");
        $("#" + id.replace("tc-", "cc-")).css("background-color", "#dddddd");
        $("#" + id.replace("cc-", "tc-")).css("background-color", "#dddddd");
    }, function() {
        var id = $(this).attr("id");
        $("#" + id.replace("tc-", "cc-")).css("background-color", "white");
        $("#" + id.replace("cc-", "tc-")).css("background-color", "white");
    });
    $("ul.propertyTabsList").hide();
    $("section.property-group").hide();
}

function collabSummary(response, startYear, endYear) {
    $("#collab-summary-container").remove();
    $("section#individual-info").append("<div id=\"collab-summary-container\"></div>");
    if (startYear > endYear) {
        msg = "<h2>Please select an end year that is greater than or equal to the start year.</h2>";
        $("#collab-summary-container").append(msg);
        info_message_reset();
        $("#collab-summary-total").html(0);
        return;
    }
    if (response.summary.coPubTotal > 0) {
        $("#collab-summary-total").html(response.summary.coPubTotal);
        if (response.categories.length > 0) {
            $("#collab-summary-cat").html(" in " + response.categories.length + " subject categories");
        }
    } else {
        $("#collab-summary-total").html(0);
        $("#collab-summary-cat").html("");
    }
    doSummaryTable(response);
    if (response.org_totals.length != 0) {
        doPubCountTable(response.org_totals, response.dtu_totals, response.summary.coPubTotal, response.copub_totals);
    }
    var html = `
    <hr/>
    <h2 class="rep">Compare institutional top research subjects</h2>
    <div id="top-research">
    `;
    if (response.top_categories.length != 0) {
        html += doTopCategoryTable(response);
        html += '<hr/>';
    }
    if ((response.summary.coPubTotal > 0) && (response.categories.length > 0)) {
        html += doPubCategoryTable(response.categories, startYear, endYear);


        // generate barchart for response.categories
        // can't be created directly in a memory container if we want to render its elements positioned correctly
        // so we need to load it in a hidden (temporary) element in DOM, and then remove it to targeted place
        let tempChartHolder = document.createElement('div')
        tempChartHolder.className += 'chartDrawnButHidden'
        tempChartHolder.setAttribute("style", "position: absolute; top: -1000; left:-1000;")
        document.body.append(tempChartHolder)

        let myData = response.categories.slice(0, 20)
                    .map(x =>
                      ({ label: x.name,
                        value: x.number,
                        category: x.category
                     }))

        let pubsByResearchSubjChartOpt = {
          data: myData,
          width: 750,
          maxWidth: 750,
          height: myData.length < 10 ? 300 : 570,
          maxHeight: 600,
          minHeight: myData.length < 10 ? 300 : 450,
          margin: {
            top: 40,
            right: 20,
            bottom: 30,
            left: 370 // if labels must be in 1 line, should be depending on max label width (label of styled font-size)
          },
          insertAt: tempChartHolder,
          title: 'Number of publications by top research subjects',
          createFn: createHBarchart,
          styleFn: styleHBarchart,
          styleFnOpt: {
            barFillColor: '#030F4F',
            oyF: '15px',
            oxF: '14px'
          }
        }
        hBarchart(pubsByResearchSubjChartOpt)

        html += tempChartHolder.innerHTML

        // BJL: send client-side-generated SVG markup to the Excel download
        svgStr1 = tempChartHolder.getElementsByTagName('div')[0].innerHTML;
        setExportForm(svgStr1, svgStr2);

        tempChartHolder.remove()
    }


    html += "</div>";
    $("#collab-summary-container").append(html);

    $("#sort-org .sort-dir").html (sortArrow (0, 1));
    $("#sort-dtu .sort-dir").html (sortArrow (0, 0));
    var inverseORG = true;
    var inverseDTU = false;
    $('#sort-org').each(function() {
        $(this).click(function() {
            $("td.sort-org").sortElements(function(a, b) {
                return Number($.text([a])) > Number($.text([b])) ?
                       inverseORG ? -1 : 1
                     : inverseORG ? 1 : -1;
            }, function() {
                return this.parentNode;
            });
            if (inverseORG) {
                $("#sort-org .sort-dir").html (sortArrow (1, 1));
            } else {
                $("#sort-org .sort-dir").html (sortArrow (0, 1));
            }
            $("#sort-dtu .sort-dir").html (sortArrow (0, 0));
            inverseORG = !inverseORG;
            inverseDTU = false;
        });
    });
    $('#sort-dtu').each(function() {
        $(this).click(function() {
            $("td.sort-dtu").sortElements(function(a, b) {
                return Number($.text([a])) > Number($.text([b])) ?
                       inverseDTU ? -1 : 1
                     : inverseDTU ? 1 : -1;
            }, function() {
                return this.parentNode;
            });
            if (inverseDTU) {
                $("#sort-dtu .sort-dir").html (sortArrow (1, 1));
            } else {
                $("#sort-dtu .sort-dir").html (sortArrow (0, 1));
            }
            $("#sort-org .sort-dir").html (sortArrow (0, 0));
            inverseDTU = !inverseDTU;
            inverseORG = false;
        });
    });

    loadPubInfoByStartYear(byDeptUrl, startYear, endYear, byDeptReport)

    if ((response.summary.coPubTotal > 0) && (response.funders.length > 0)) {
        html = `
	  <hr/>
	  <div id="top-funders">
        `;
        html += doFunderTable(response.funders, startYear, endYear);
	html += "</div>";
        $("#collab-summary-container").append(html);
    }

    if ((response.summary.coPubTotal > 0) && (response.dtu_researchers.length > 0)) {
        html = `
	  <hr/>
	  <div id="top-dtu-researchers">
        `;
        html += doDtuResearchersTable(response.dtu_researchers, startYear, endYear);
	html += "</div>";
        $("#collab-summary-container").append(html);
    }

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


function byDeptReport(response, startYear, endYear) {
    if (response.departments.length > 0) {
        doDepartmentTable(response.departments, response.name, startYear, endYear);
    }
    info_message_reset();
}

function doCategories(response) {

    $("#collab-summary-container").append("<li>Co-publication categories: " + response.categories.length + "</li>");
}

function doSummaryTable(response) {
    $("summaryTable").remove();
    var html = `
    <div id="summaryTable">
    <hr/>
    <h2 class="rep">Compare key output and impact indicators</h2>
    <table id="rep1" class="pub-counts">
    `;
    if (response.summary.country) {
        $("#collab-summary-country").html(",&nbsp" + response.summary.country).removeClass("hidden");
    }
    html += "<tr><th class=\"col1\"></th><th class=\"col2\">Partner</th><th class=\"col3\">DTU</th></tr>";
    html += doSummaryTableRow (0, 0, "Number of publications",                             '',          response.summary.orgTotal,      response.summary.dtuTotal);
    html += doSummaryTableRow (0, 0, "Number of citations",                                '',          response.summary.orgCitesTotal, response.summary.dtuCitesTotal);
    html += doSummaryTableRow (1, 0, "Simple citation impact (citations / publication)",   '',          response.summary.orgImpact,     response.summary.dtuImpact);
    html += doSummaryTableRow (2, 0, "Normalised citation impact (global average 1.0)",    'ind-nci',   response.summary.orgimp,        response.summary.dtuimp);
    html += doSummaryTableRow (0, 1, "% of publications in top 10% most cited",            'ind-top10', response.summary.orgt10,        response.summary.dtut10);
    html += doSummaryTableRow (0, 1, "% of publications in top 1% most cited",             'ind-top1',  response.summary.orgt1,         response.summary.dtut1);
    html += doSummaryTableRow (0, 1, "% of publications with industry collaboration",      '',          response.summary.orgcind,       response.summary.dtucind);
    html += doSummaryTableRow (0, 1, "% of publications with international collaboration", '',          response.summary.orgcint,       response.summary.dtucint);
    html += `
        <div id="ind-nci-dialog" title="Normalised citation impact">
          <p>
              Citations per publication normalised for subject, year, and publication type.
              The world average is equal to 1.
              Example: A normalised citation impact of 1.23 means that the impact is 23% above the world average.
          </p>
        </div>
        <div id="ind-top10-dialog" title="% of publications in top 10% most cited">
          <p>
              Proportion of the publications belonging to the top 10% most cited in a given subject category, year, and publication type.
          </p>
        </div>
        <div id="ind-top1-dialog" title="% of publications in top 1% most cited">
          <p>
              Proportion of the publications belonging to the top 1% most cited in a given subject category, year, and publication type.
          </p>
        </div>
    `;
    $("#collab-summary-container").append(html);

    $("#ind-nci-dialog").hide();
    $("#ind-top10-dialog").hide();
    $("#ind-top1-dialog").hide();
    $("#ind-nci").click(function() {
        $("#ind-nci-dialog").dialog({
            resizable: false,
            height: "auto",
            width: 400,
            modal: true,
            buttons: {
                Close: function() {
                    $( this ).dialog( "close" );
                }
            },
            position: { my: "left top", at: "left bottom", of: "#ind-nci" }
        });
    });
    $("#ind-top10").click(function() {
        $("#ind-top10-dialog").dialog({
            resizable: false,
            height: "auto",
            width: 400,
            modal: true,
            buttons: {
                Close: function() {
                    $( this ).dialog( "close" );
                }
            },
            position: { my: "left top", at: "left bottom", of: "#ind-nci" }
        });
    });
    $("#ind-top1").click(function() {
        $("#ind-top1-dialog").dialog({
            resizable: false,
            height: "auto",
            width: 400,
            modal: true,
            buttons: {
                Close: function() {
                    $( this ).dialog( "close" );
                }
            },
            position: { my: "left top", at: "left bottom", of: "#ind-nci" }
        });
    });
}

function doSummaryTableRow(real, percent, label, info, org, dtu) {
    var vo = org;
    var vd = dtu;
    if (real) {
        if (vo) {
            vo = vo.toFixed(real);
        }
        if (vd) {
            vd = vd.toFixed(real);
        }
    } else if (percent) {
        if (vo) {
            vo = vo.toFixed(percent) + "%";
        }
        if (vd) {
            vd = vd.toFixed(percent) + "%";
        }
    }
    if (info) {
        info = ' <button id="' + info + '" class="info-button" style="border: 0px;"><span class="ui-icon ui-icon-info"></span></button>';
    }
    return "<tr><td class=\"rep-label\">" + label + info + "</td><td class=\"rep-num\">" + vo + "</td><td class=\"rep-num\">" + vd + "</td></tr>";
}

function doTopCategoryTable(response) {
    var html = `
    <table id="rep3" class="pub-counts">
        <tr>
            <th class="col1">Research publication subjects</th>
            <th class="col2" colspan="2">
                <div id="sort-org" style="color: white;">
                    Partner
                    <div class="sort-dir" style="height: 23px;"></div>
                </div>
            </th>
            <th class="col3" colspan="2">
                <div id="sort-dtu" style="color: white;">
                    DTU
                    <div class="sort-dir" style="height: 23px;"></div>
                </div>
            </th>
            <th class="col4" rowspan="2">
                <div style="color: white;">
                    Co-pubs
                </div>
            </th>
        </tr>
        <tr>
            <th class="col1">Compare partner and DTU</th>
            <th class="col21">Publ.</th>
            <th class="col22">Rank</th>
            <th class="col31">Publ.</th>
            <th class="col32">Rank</th>
        </tr>
    `;
    var n = 0;
    $.each( response.top_categories, function( key, value ) {
        if (n < 20) {
            var row = "<tr class=\"rep-row\" id=\"tc-" + idkey(value.name) + "\"><td class=\"rep-label\">" + value.name + "</td>" +
                      "<td class=\"rep-num\">" + value.number + "</td>" +
                      "<td class=\"rep-num sort-org\">" + value.rank + "</td>" +
                      "<td class=\"rep-num\">" + value.DTUnumber + "</td>" +
                      "<td class=\"rep-num sort-dtu\">" + value.DTUrank + "</td>" +
                      "<td class=\"rep-num\">" + value.copub + "</td>" +
                      "</tr>";
            html += row;
            n++;
        }
    });
    html += "</table>";
    return html;
}

function idkey(name) {
    name = name.toLowerCase();
    return name.replace(/[^0-9a-z]/g, '');
}

function doPubCategoryTable(totals, startYear, endYear) {
    var html = `
    <h2 class="rep">Compare collaboration top research subjects with institutional</h2>
    <table id="rep4" class="pub-counts">
      <tr>
        <th class="col1">Collaboration  publication subjects</th>
        <th class="col2">Co-pubs</th>
        <th class="col3">Partner rank</th>
        <th class="col4">DTU rank</th>
      </tr>
    `;
    $.each( totals.slice(0, 20), function( key, value ) {
        if (value.category != null) {
            var href = base + "/copubs-by-category/" + value.category.split("/")[4] + "?collab=" + individualLocalName;
            if(startYear > 0) {
                href += "&startYear=" + startYear;
            }
            if(endYear > 0) {
                href += "&endYear=" + endYear;
            }
            var coPubLink = "<a href=\"" + href + "\" target=\"_blank\">" +  value.number + "</a>";
            var row = "<tr class=\"rep-row\" id=\"cc-" + idkey(value.name) + "\"><td class=\"rep-label\">" + value.name + "</td><td class=\"rep-num\">" + coPubLink + "</td>" +
                      "<td class=\"rep-num\">" + value.rank + "</td><td class=\"rep-num\">" + value.DTUrank + "</td></tr>";
            html += row;
        }
    });
    html += "</table>";
    return html;
}

function doFunderTable(totals, startYear, endYear) {
    var html = `
    <h2 class="rep">Co-publications by funder (top 20)</h2>
    <table id="rep7" class="pub-counts">
      <tr>
        <th class="col1">Funder</th>
        <th class="col2">Publ.</th>
      </tr>
    `;
    $.each( totals.slice(0, 20), function( key, value ) {
        if (value.funder != null) {
            var href = base + "/copubs-by-funder/" + value.funder.split("/")[4] + "?collab=" + individualLocalName;
            if(startYear > 0) {
                href += "&startYear=" + startYear;
            }
            if(endYear > 0) {
                href += "&endYear=" + endYear;
            }
            var coPubLink = "<a href=\"" + href + "\" target=\"_blank\">" +  value.number + "</a>";
            var row = "<tr class=\"rep-row\" id=\"cc-" + idkey(value.name) + "\"><td class=\"rep-label\">" + value.name + "</td><td class=\"rep-num\">" + coPubLink + "</td></tr>";
            html += row;
        }
    });
    html += "</table>";
    return html;
}

function doDtuResearchersTable(totals, startYear, endYear) {
    var html = `
    <h2 class="rep">Co-publications by DTU researcher (top 20)</h2>
    <table id="rep6" class="pub-counts">
      <tr>
        <th class="col1">DTU Researcher</th>
        <th class="col2">Publ.</th>
      </tr>
    `;
    $.each( totals.slice(0, 20), function( key, value ) {
        if (value.funder != null) {
            var href = base + "/copubs-by-dtu-researcher/" + value.dtuResearcher.split("/")[4] + "?collab=" + individualLocalName;
            if(startYear > 0) {
                href += "&startYear=" + startYear;
            }
            if(endYear > 0) {
                href += "&endYear=" + endYear;
            }
            var coPubLink = "<a href=\"" + href + "\" target=\"_blank\">" +  value.number + "</a>";
            var row = "<tr class=\"rep-row\" id=\"cc-" + idkey(value.name) + "\"><td class=\"rep-label\">" + value.name + "</td><td class=\"rep-num\">" + coPubLink + "</td></tr>";
            html += row;
        }
    });
    html += "</table>";
    return html;
}

function doDepartmentTable(totals, name, startYear, endYear) {
    $("departmentTable").remove();
    var html = `
    <div id="departmentTable">
    <hr/>
    <h2 class="rep">Co-publications by department</h2>
    <table id="rep5" class="pub-counts">
      <tr>
        <th class="col1">DTU department</th>
        <th class="col2">Publications</th>
        <th class="col3">Partner departments</th>
      </tr>
    `;

    var yearParams = "";
    if(startYear > 0) {
        yearParams += "&startYear=" + startYear;
    }
    if(endYear > 0) {
        yearParams += "&endYear=" + endYear;
    }

    var closeHtml = "</table></div>";
    var last = null;
    $.each( totals, function( key, value ) {
        var orgKey = value.org.split("/")[4];
        if (value.name != last) {
            link = "<a href=\"" + base + "/individual?uri=" + value.org + "\">" + value.name + "</a>";
            var coPubLink = "<a href=\"" + base + "/copubs-by-dept/" + value.org.split("/")[4] + "?collab=" + individualLocalName + yearParams + "\" target=\"_blank\">" +  value.num + "</a>";
            //link = value.name;
            var row = "<tr class=\"copubdept-head\"><td class=\"rep-label\">";
            row += value.name + "</td><td class=\"dtu-dept-num\">" + coPubLink + "</td><td><a class=\"view-dept\">Expand to show details</a></td></tr>"
            html += row
        }
        $.each( value.sub_orgs, function( k2, subOrg ) {
            var subOrgKey = subOrg.uri.split("/")[4];
            var row = "<tr class=\"copubdept-child\"><td>";
            var clink = "<a href=\"" + base + "/copubs-by-dept/" + orgKey + "?collab=" + individualLocalName + "&collabSub=" + subOrgKey + "&collabSubName=" + encodeURIComponent(subOrg.name) + yearParams + "\" target=\"copubdept\">" +  subOrg.total + "</a>";
            row +=  "</td><td class=\"rep-num\">" + clink + "</td><td>" + subOrg.name + "</td></tr>";
            html += row;
        });
        last = value.name;
    });
    html += closeHtml;
    $("#collab-summary-container").append(html);

    // CALL RESPONSIVE CHARTS FN after elements are in dom
    let responsiveCharts = document.querySelectorAll('.chart-box[minH]')
    if (window.ResizeObserver) {
      responsiveCharts.forEach(x => resizeChart(x))
    }

}


/*** FN TO RESIZE CHARTS - at this moment available only in chrome and opera ***/
function resizeChart(target) {
  let minH = target.getAttribute('minH');
  let widthTrigger = 0

  var ro = new ResizeObserver( entries => {
    let entry = entries[0],
        cr = entry.contentRect,
        svg = entry.target.children[0],
        svgStyle = svg.style

    if (cr.height < minH) {

      if (!widthTrigger) widthTrigger = cr.width
      svgStyle.height = minH + 'px';
    }

    if (widthTrigger && svgStyle.height && cr.width > widthTrigger) {
      svgStyle.height = ''
    }
  });

  // Observe one or multiple elements
  ro.observe(target);
}


/*** START LINE CHART GENERAL FUNCTIONS ***/
// fn to create base input and elements and position them
function createLineChart({data, insertAt, title, svgId, width, maxWidth, height, maxHeight, minHeight, margin, label}) {

  // define xDomain and yDomain
  let xDomain = data.map(x => x.date)
  let yDomain = data.map(x => x.value)

  // define xScale and yScale with their domain and range
  let x = d3.scaleUtc()
    .domain(d3.extent(xDomain))
    .range([0, width - margin.left - margin.right])
  let y = d3.scaleLinear()
    .domain([0, Math.round(d3.max(yDomain)) + 1])
    .range([height - margin.top - margin.bottom, 0])


  // create x and y axis function
  let yTicksTotal = d3.max(yDomain) < 8 ? d3.max(yDomain) : null // avoid having intermittent values with 0.3, 0.6 etc
  let yAxis = (g) => g.call(d3.axisLeft(y).ticks(yTicksTotal))
  let xAxis = (g) => g.call(d3.axisBottom(x).ticks(xDomain.length))


  // fn to generate line
  let line = d3.line()
  .x(d => x(d.date))
  .y(d => y(d.value))


  // create svg in dom
  let svg = d3.select(insertAt)
    .append('div')
    .attr('class', 'chart-box')
    .attr('minH', minHeight)
    .style('max-width', maxWidth + 'px')
    .style('overflow', 'auto')
      .append('svg')
      .attr('id', svgId)
      .attr("preserveAspectRatio", "xMinYMin meet")
      .attr("viewBox", "0 0 " + width +' ' + height)
      .classed('svg-content', true)
      .style('max-height', maxHeight+'px')


  // positioning
  let position = (x, y) => 'translate(' + x + ',' + y + ')'

  let translate = {
    yAxis: position(margin.left, margin.top),
    xAxis: position(margin.left, (height-margin.bottom)),
    line: position(margin.left, margin.top),
  }


  // 1.create 2.position axes
  svg.append('g')
    .call(yAxis)
    .attr('class', 'oy')
    .attr('transform', translate.yAxis)

  svg.append('g')
    .call(xAxis)
    .attr('class', 'ox')
    .attr("transform", translate.xAxis)

  // svg append serie
  let serie = svg.append('g')
      .attr('class', 'serie')

  // serie append path
  serie.append("path")
    .datum(data)
    .attr('d', line)
    .attr('transform', translate.line)
    .attr('class', 'chart-line')

  // base styles - can be overwritten by styleFn
    .attr('stroke', 'black')
    .attr('stroke-width', 1)
    .attr('fill', 'none')


  // title
  svg.append('text')
    .attr('class', 'svg-title')
    .attr("x", width/2)
    .attr("y", 16)
    .attr("text-anchor", "middle")
    .attr('font-size', 16) // base style, to be overridden in styleFn
    .text(title)

  // LABELS if needed
  let hasLabels = label.type
  if (hasLabels) {


    // for now we deal only with case of rectanglar label
    let isRectLabel = label.type == 'rect'
    if (isRectLabel) {
      var labelPaddingH = label.paddingH,
          labelPaddingV = label.paddingV

      translate.labelsGr = position(margin.left-labelPaddingH/2, margin.top-labelPaddingV/2)
    }

    let isRoundLabel = label.type == 'round'
    if (isRoundLabel) {
       var r = label.r
      translate.labelsGr = position(margin.left, margin.top)
    }


    let labelGroup = svg.select('.serie')
      .append('g').attr('class', 'labels')
      .attr('font-size', 14)
      .attr("transform", translate.labelsGr)

    let labels = labelGroup.selectAll(".label")
      .data(data)
      .enter()
      .append("g")
      .attr("class", "label")
      .attr("transform", (d, i) =>  ("translate(" + x(d.date) + "," + y(d.value) + ")"))

    if (!isRoundLabel) {
      labels.append("text")
      .text( d => d.value)
      .attr("dx", function() { return -this.getBBox().width/2 + labelPaddingH/2+ 'px'})
      // .attr('text-anchor', "middle") this would center text in middle of container g - which is equal in width with rect

      .attr("dy", function() { return this.getBBox().height/2 + 'px'})
    }


    if (isRectLabel) {
      labels.insert("rect", 'text')
      .datum( function() { return this.nextSibling.getBBox() })
      .attr("x", (d) => (d.x - labelPaddingH))
      .attr("y", (d) => d.y - labelPaddingV-1)
      .attr("width", (d) => d.width + 2 * labelPaddingH)
      .attr("height", (d) => d.height + 2 * labelPaddingV)
      .attr('fill', '#fff')
    }

    if (isRoundLabel) {
      labels.append('circle')
        .attr('r', r)
    }

  } // close [if hasLabels]

}
// end createLineChart fn

/*** START HORIZONTAL BARCHART FN ***/
function styleHBarchart(opt) {
  let oxF = opt.oxF,
      oyF = opt.oyF,
      barColor = opt.barFillColor

  let svg = d3.select('#' + opt.svgId),
      // oyBox = svg.select('.oy-box'),
      oy = svg.select('.oy'),
      ox = svg.select('.ox'),
      title = svg.select('.svg-title')

  svg.style('color', '#5E6363')
     .style('fill', '#5E6363')

  oy.attr('transform', 'translate(-7, 0)')

  svg.selectAll('.bar')
    .attr('fill', barColor)
  oy.select('.domain').remove()
  oy.selectAll('text')
    .style('font-size', oyF )

  ox.style('font-size', oxF)
  oy.style('font-size', oxF)
  title.style('font-weight', 'bold')
      .style('font-size', '16px')

  let textElems = svg.selectAll('text')
}

function createHBarchart({data, insertAt, svgId, width, maxWidth, height, maxHeight, minHeight, margin, title}) {
  // define xDomain and yDomain
  let xDomain = data.map(x => x.value)
  let yDomain = data.map(x => x.label)

  // define xScale and yScale with their domain and range
  let y = d3.scaleBand()
      .domain(yDomain)
      .range([margin.top, height - margin.bottom])
      .padding(0.2) // should be customizable

  let x = d3.scaleLinear()
      .domain( [Math.round(d3.max(xDomain))+1, 0] ).nice()
      .range([width-margin.right-margin.left, 0])

  let ticksTotal = d3.max(xDomain) < 7 ? d3.max(xDomain) : null
  let xAxis = (g) => g.call(d3.axisBottom(x).ticks(ticksTotal))
  let yAxis = (g) => g.call(d3.axisLeft(y))


  // create svg in dom
  let svg = d3.select(insertAt)
    .append('div')
    .attr('class', 'chart-box')
    .attr('minH', minHeight)
    .style('max-width', maxWidth + 'px')
    .style('overflow', 'hidden')
      .append('svg')
      .attr('id', svgId)
      .attr("preserveAspectRatio", "xMinYMin meet")
      .attr("viewBox", "0 0 " + width + ' ' + height)
      .classed('svg-content', true)
      .style('max-height', maxHeight+'px')
      .append('g')
        .attr('class', 'svg-inner')

  let position = (x, y) => 'translate(' + x + ',' + y + ')'

  let translate = {
    yAxis: position(margin.left, 0),
    xAxis: position(margin.left, (height-margin.bottom))
  }

  // 1.create 2.position axes
  let q = svg.append('g')
    .attr('class', 'oy-box')
    .attr('transform', translate.yAxis)
    .append('g')
      .attr('class', 'oy')
      .call(yAxis)

  svg.append('g') //extra 'g' wrapper so we can position inner 'g' relative
    .attr('class', 'oy-box')
    .attr("transform", translate.xAxis)
    .append('g')
      .attr('class', 'ox')
      .call(xAxis)

  // horizontal bars
  svg.append('g')
    .attr('transform', translate.yAxis)
    .attr('class', 'bar-group-box')
    .append('g')
      .attr('class', 'bar-group')
      .selectAll('rect')
      .data(data)
      .enter()
      .append('rect')
      .attr('class', 'bar')
      .attr('y', d => y(d.label))
      .attr('width', d => x(d.value))
      .attr('height', d => (y.bandwidth()))


  // title
  svg.append('text')
    .attr('class', 'svg-title')
    .attr("x", width/2)
    .attr("y", 18)  // 12 being half of font-size
    .attr("text-anchor", "middle")
    .attr('font-size', 18) // base style, to be overridden in styleFn
    .text(title)

}

function hBarchart(options) {
  let width = options.width,
    maxWidth = options.maxWidth,
    height = options.height,
    maxHeight = options.maxHeight,
    minHeight = options.minHeight,
    margin = options.margin,
    data = options.data,
    insertAt = options.insertAt,
    svgTitle = options.title,
    create = options.createFn,
    style = options.styleFn,
    styleFnOpt = options.styleFnOpt

  let svgId = svgTitle.toLowerCase().split(' ').join('-')


  create({data, insertAt, svgId, width, maxWidth, height, maxHeight, minHeight, margin, title:svgTitle})

  styleFnOpt.svgId = svgId
  style(styleFnOpt)
}
// END HORIZONTAL BARCHART FN //


// fn for customizing look - can be custom made each time/ for each chart
function styleLineChart(opt){
   let svg = d3.select('#' + opt.svgId)
   svg.style('color', '#5E6363')
      .style('fill', '#5E6363')

   let textElems = svg.selectAll('text')
   textElems.style('color', "#5E6363")

   let serie = svg.select('.serie')
   serie.select('path')
      .attr('stroke', '#030F4F') // maybe color should be passed as option
      .attr('stroke-width', 2)

  let labels = svg.selectAll('.label')
  labels.select('circle').attr('fill', '#030F4F')

  let ox = svg.select('.ox')
  let oy = svg.select('.oy')
  ox.style('font-size', '14px')
  oy.style('font-size', '14px')

  let title = svg.select('.svg-title')
      .style('font-weight', 'bold')
      .style('font-size', '16px')

} // end styleLineChart

function lineChart(opt) {
  let create = opt.createFn
  let style = opt.styleFn

  let data = opt.data,
      maxHeight = opt.maxHeight,
      minHeight = opt.minHeight,
      startHeight = opt.startHeight,
      maxWidth = opt.maxWidth,
      startWidth = opt.startWidth,

      insertAt = opt.insertAt,
      margin = opt.margin,
      title = opt.svgTitle,
      label = opt.label

  let height = startHeight
  let width = insertAt.clientWidth || startWidth
  let svgId = title.toLowerCase().split(' ').join('-')

  let styleOpt = opt.styleFnOpt || {}
  styleOpt.svgId = svgId

  create({
    data,
    title,
    svgId,
    insertAt,
    width, maxWidth,
    height, maxHeight, minHeight,
    margin,
    label
  })

  style(styleOpt)
}
/*** END LINE CHART GENERAL FUNCTIONS ***/


function doPubCountTable(totals, DTUtotals, copubsTotal, copubs) {
    var years = {};
    $.each(totals, function(key, value) {
        var year = value.year.toString();
        if (year in years) {
            years[year]["org"] = value.number;
            years[year]["copubs"] = 0;
        } else {
            years[year] = [];
            years[year]["org"] = value.number;
            years[year]["copubs"] = 0;
        }
    });
    $.each(DTUtotals, function(key, value) {
        var year = value.year.toString();
        if (year in years) {
            years[year]["dtu"] = value.number;
            years[year]["copubs"] = 0;
        } else {
            years[year] = [];
            years[year]["dtu"] = value.number;
            years[year]["copubs"] = 0;
        }
    });
    if (copubsTotal > 0) {
        $.each(copubs, function(key, value) {
            var year = value.year.toString();
            if (year in years) {
                years[year]["copubs"] = value.number;
            } else {
                years[year] = [];
                years[year]["copubs"] = value.number;
            }
        });
    }
    var html = `
    <div id="pubCountTable">
    <hr/>
    <h2 class="rep">Compare the annual publication output</h2>
    <table id="rep2" class="pub-counts">
      <tr>
        <th class="col1">Year</th>
        <th class="col2">Partner</th>
        <th class="col3">DTU</th>
        <th class="col4">Co-pubs</th>
      </tr>
    `;
    $.each(years, function(key, value) {
        html += "<tr><td class=\"rep-label\">" + key;
        if (key > 2017) {
            html += "<sup>*</sup>";
        }
        html += "</td><td class=\"rep-num\">";
        if ("org" in years[key]) {
            html += years[key]["org"];
        } else {
            html += "0";
        }
        html += "</td><td class=\"rep-num\">";
        if ("dtu" in years[key]) {
            html += years[key]["dtu"];
        } else {
            html += "0";
        }
        html += "</td><td class=\"rep-num\">";
        if ("copubs" in years[key]) {
            html += years[key]["copubs"];
        } else {
            html += "0";
        }
        html += "</td></tr>";
    });
    html += "</table><sup>*</sup> <span class=\"footnote\">The number of publications for a year will not be complete until the middle of the following year due to latency of indexing publications in Web of Science.</span></div>";

    if (copubsTotal == 0) {
        $("#collab-summary-container").append(html);
        return;
    }
    // generate linechart for response.categories
    // can't be created directly in a memory container if we want to render its elements positioned correctly
    // so we need to load it in a hidden (temporary) element in DOM, and then replace it to targeted place
    let tempChartHolder = document.createElement('div')
    tempChartHolder.className += 'chartDrawnButHidden'
    tempChartHolder.setAttribute("style", "position: absolute; top: -1000; left:-1000;")
    document.body.append(tempChartHolder)
    
    let myData = []
    for(let year in years) {
        myData.push({
            value: years[year].copubs,
            date: new Date(year.toString())
  	})
    }
    myData = myData.sort( (a,b) => (a.date - b.date) )
    
    let copubsChartOpt = {
      data: myData,
      maxHeight: 500,
      minHeight: 270,
      startHeight: 300,
      startWidth: 800,
      maxWidth: 900,  // should be width of #collab-summary-container

      svgTitle: 'Number of co-publications per year',
      insertAt: tempChartHolder,
      margin: {
        top: 70,
        left: 40,
        bottom: 30,
        right: 40
      },
      createFn: createLineChart,
      // draw options
      styleFnOpt: {},
      label: {
        type: 'round',
        r: 4,
      },
      styleFn: styleLineChart
    }

    lineChart(copubsChartOpt)

    html += tempChartHolder.innerHTML

    // BJL: send client-side-generated SVG markup to the Excel download
    svgStr2 = tempChartHolder.getElementsByTagName('div')[0].innerHTML;
    setExportForm(svgStr1, svgStr2);

    tempChartHolder.remove()

    $("#collab-summary-container").append(html);
}

function loadPubInfo(url, callback) {
    loadPubInfoByStartYear(url, -1, -1, callback);
}

function loadPubInfoByStartYear(url, startYear, endYear, callback) {
    if(startYear > endYear) {
        callback("", startYear, endYear);
    }
    var xhr = new XMLHttpRequest();
    if(startYear > 0)  {
        url += "/" + startYear;
    }
    if(endYear > 0)  {
        url += "/" + endYear;
    }
    xhr.open('GET', url);
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response);
            callback(response, startYear, endYear);
        }
        else {
            alert('Request ' + url + ' failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}


//Bind events to dynamically added elements.
//http://jsfiddle.net/ramswaroop/Nrxp5/28/
function hasClass(elem, className) {
    return elem.className.split(' ').indexOf(className) > -1;
}

//https://jsfiddle.net/gengns/j1jm2tjx/
function downloadCsv(csv, filename) {
    var csvFile;
    var downloadLink;

    // CSV FILE
    csvFile = new Blob([csv], {type: "text/csv"});

    // Download link
    downloadLink = document.createElement("a");

    // File name
    downloadLink.download = filename;

    // We have to create a link to the file
    downloadLink.href = window.URL.createObjectURL(csvFile);

    // Make sure that the link is not displayed
    downloadLink.style.display = "none";

    // Add the link to your DOM
    document.body.appendChild(downloadLink);

    downloadLink.click();
}

function setExportFormBase() {
  let form = document.querySelector('#export-report_form')
  form.action = "${urls.base}/excelExport/" + individualLocalName + ".xlsx"

  let inputLocalName = document.querySelector('.export-report_local-name')
  inputLocalName.value = individualLocalName
}

function setExportForm(svgStr1, svgStr2) {

  let startYear = range_from_val();
  let input1 = document.querySelector('.export-report_start-year')
  input1.value = startYear

  let endYear = range_to_val();
  let input2 = document.querySelector('.export-report_end-year')
  input2.value = endYear

  if(svgStr1) {
    let input3 = document.querySelector('.export-report_svg1')
    input3.value = svgStr1
  }

  if (svgStr2) {
    let input4 = document.querySelector('.export-report_svg2')
    input4.value = svgStr2
  }
}



function exportTable(html, filename) {
    var csv = [];
    var sections = document.querySelectorAll("table")
    for (var sec =0; sec < sections.length; sec++) {
        var rows = sections[sec].querySelectorAll("tr");
        for (var i = 0; i < rows.length; i++) {
            var row = [], cols = rows[i].querySelectorAll("td, th");
            for (var j = 0; j < cols.length; j++) {
                row.push("\"" + cols[j].innerText.replace(/(\d) (?=\d)/g, "$1") + "\"");
            }
            csv.push(row.join("\t"));
        }
        csv.push("\n");
    }

    // Download CSV
    downloadCsv(csv.join("\n"), filename);
}



</script>

<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Individual profile page template for foaf:Organization individuals (extends individual.ftl in vivo)-->

<#-- Do not show the link for temporal visualization unless it's enabled -->

<script src="${urls.theme}/js/jquery.corner.js"></script>
<script src="${urls.theme}/js/d3-v5.min.js"></script>


<#assign affiliatedResearchAreas>
    <#include "individual-affiliated-research-areas.ftl">
</#assign>


<#include "individual.ftl">
<div style="height: 60px;"></div>


<script>

let module = {
  db: {
    foo: '123'
  },
  controllers: [{
      name: 'foo',
      args: {
        init: '/'
      },
      fn: () => {
        console.log('foo')
      }
    }]
}
var individualUri = "${individual.uri}";

//co-publication report
$("span.display-title").html('');
var uni = $("h1.fn").text();
$("h1.fn").html("DTU collaboration with " + uni.trim() +
                "<span id=\"collab-summary-country\" class=\"hidden\"></span>" +
                " from " + "<select id=\"startYear\" name=\"startYear\"></select>" +
                " to " + "<select id=\"endYear\" name=\"endYear\"></select>");
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
	    <a id="report-export" class="report-export" href="#">Export</a>
            <#-- original Javascript export
	    <a class="report-export" href="#">Export</a>
	    -->
        </h2>
        <p><a href="--link--">Show list</a> of all publications since 2007</p>
    `.replace('--link--', searchLink);
    $("#startYear").corner();
    $("#endYear").corner();
    $("#individual-info").append(html);
    $("#startYear").change(function() {
        setExportLink(individualLocalName, $("#startYear").val(), $("#endYear").val());
        info_message("Updating Co-publication report for start year " + $("#startYear").val());
        loadPubInfoByStartYear(vds, $("#startYear").val(), $("#endYear").val(), collabSummary);
    });
    $("#endYear").change(function() {
        setExportLink(individualLocalName, $("#startYear").val(), $("#endYear").val());
        info_message("Updating Co-publication report for end year " + $("#endYear").val());
        loadPubInfoByStartYear(vds, $("#startYear").val(), $("#endYear").val(), collabSummary);
    });
    setExportLink(individualLocalName, $("#startYear").val(), $("#endYear").val());
    loadPubInfo(vds, collabSummary);
    document.addEventListener('click', function (e) {
        if (hasClass(e.target, 'report-export')) {
	    // Original Javascript export
            // var html = document.querySelector("table").outerHTML
            // exportTable(html, "co-publication-" + individualLocalName + ".tsv");
        } else if (hasClass(e.target, 'view-dept')) {
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

function addDateSelector() {
    $("section#individual-info").append("<p>Start year <select id=\"startYear\" name=\"startYear\"></select></p>");
    $("#startYear").change(function() {
        info_message("Updating Co-publication report for start year " + $("#startYear").val());
        loadPubInfoByStartYear(vds, $("#startYear").val(), $("#endYear").val(), collabSummary);
    });
    $("section#individual-info").append("<p>End year <select id=\"endYear\" name=\"endYear\"></select></p>");
    $("#endYear").change(function() {
        info_message("Updating Co-publication report for end year " + $("#endYear").val());
        loadPubInfoByStartYear(vds, $("#startYear").val(), $("#endYear").val(), collabSummary);
    });
    $("#startYear").corner();
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
        $("#" + id.replace("tc-", "cc-")).css("background-color", "#f2f2f2");
        $("#" + id.replace("cc-", "tc-")).css("background-color", "#f2f2f2");
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
    var yearRange = [];
    if (response.org_totals != []) {
        for(var i in response.org_totals) {
	    if(response.org_totals[i].year >= startYear) {
	        yearRange.push(response.org_totals[i].year)
	    }
	};
        yearRange.sort()
    } else {
        yearRange = [startYear, 2016];
    }
    if(startYear < 0) {
        for(var i in yearRange) {
            $("#startYear").append("<option value=\"" + yearRange[i] + "\">" + yearRange[i] + "</option>");
            $("#endYear").append("<option value=\"" + yearRange[i] + "\">" + yearRange[i] + "</option>");
        }
	$("#endYear").val($("#endYear option:last-child").val());
    }
    $("#collab-summary-total").html(response.summary.coPubTotal);
    if (response.categories.length > 0) {
        $("#collab-summary-cat").html(" in " + response.categories.length + " subject categories");
    }
    doSummaryTable(response);
    if (response.org_totals.length != 0) {
        doPubCountTable(response.org_totals, response.dtu_totals, response.copub_totals);
    }
    var html = `
    <hr/>
    <h2>Top research subjects</h2>
    <div id="top-research">
    `;
    if (response.top_categories.length != 0) {
        html += doTopCategoryTable(response);
    }
    if (response.categories.length > 0) {
        html += doPubCategoryTable(response.categories, startYear, endYear);


        // generate barchart for response.categories
        // can't be created directly in a memory container if we want to render its elements positioned correctly
        // so we need to load it in a hidden (temporary) element in DOM, and then remove it to targeted place
        let tempChartHolder = document.createElement('div')
        tempChartHolder.className += 'chartDrawnButHidden'
        tempChartHolder.setAttribute("style", "position: absolute; top: -1000; left:-1000;")
        document.body.append(tempChartHolder)

        let myData = response.categories.map(x =>
                    ({ label: x.name,
                      value: x.number,
                      category: x.category
                     }))

        let pubsByResearchSubjChartOpt = {
          data: myData,
          width: 600,
          maxWidth: 700,
          height: 750,
          maxHeight: 800,
          minHeight: 450,
          margin: {
            top: 40,
            right: 20,
            bottom: 30,
            left: 300 // if labels must be in 1 line, should be depending on max label width (label of styled font-size)
          },
          insertAt: tempChartHolder,
          title: 'Number of Publications by Top Research Subjects',
          createFn: createHBarchart,
          styleFn: styleHBarchart,
          styleFnOpt: {
            barFillColor: '#030F4F',
            oyF: '13px',
            oxF: '13px'
          }
        }
        hBarchart(pubsByResearchSubjChartOpt)

        html += tempChartHolder.innerHTML
        tempChartHolder.remove()
    }


    html += "</div>";
    $("#collab-summary-container").append(html);
    loadPubInfoByStartYear(byDeptUrl, startYear, endYear, byDeptReport)
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
    <h2>Overview</h2>
    <table id="rep1" class="pub-counts">
    `;
    if (response.summary.country) {
        $("#collab-summary-country").html(",&nbsp" + response.summary.country).removeClass("hidden");
    }
    var orgTotal = response.summary.orgTotal;
    //for(var i in response.org_totals) { orgTotal += response.org_totals[i].count; };
    var orgTotalCites = response.summary.orgCitesTotal;
    var dtuTotal = response.summary.dtuTotal;
    var dtuTotalCites = response.summary.dtuCitesTotal;
    if (response.summary.orgImpact) {
        var orgImpact = response.summary.orgImpact.toFixed(1);
    } else {
        var orgImpact = null;
    }
    if (response.summary.dtuImpact) {
        var dtuImpact = response.summary.dtuImpact.toFixed(1);
    } else {
        var dtuImpact = null ;
    }
    html += "<tr><th class=\"col1\"></th><th class=\"col2\">" + response.summary.name + "</th><th class=\"col3\">Technical University of Denmark</th></tr>";
    html += "<tr><td class=\"rep-label\">Publications</td><td class=\"rep-num\">" + orgTotal + "</td><td class=\"rep-num\">" + dtuTotal + "</td>";
    html += "<tr><td class=\"rep-label\">Citations</td><td class=\"rep-num\">" + orgTotalCites + "</td><td class=\"rep-num\">" + dtuTotalCites + "</td>";
    //Impact
    html += "<tr><td class=\"rep-label\">Impact<sup>*</sup></td><td class=\"rep-num\">" + orgImpact + "</td><td class=\"rep-num\">" + dtuImpact + "</td>";

    html += "</table><sup>*</sup> <span class=\"footnote\">Citations per publication for the timespan selected.</span></div>";
    $("#collab-summary-container").append(html);
}

function doTopCategoryTable(response) {
    var html = `
    <table id="rep3" class="pub-counts" style="display: inline-block; vertical-align: top;">
        <tr>
            <th class="col1">Partner&apos;s top research subjects</th>
            <th class="col2">Publications</th>
        </tr>
    `;
    var n = 0;
    $.each( response.top_categories, function( key, value ) {
        if (n < 20) {
            var row = "<tr class=\"rep-row\" id=\"tc-" + idkey(value.name) + "\"><td class=\"rep-label\">" + value.name + "</td><td class=\"rep-num\">" + value.number + "</td></tr>";
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
    <table id="rep4" class="pub-counts" style="display: inline-block; vertical-align: top;">
      <tr>
        <th class="col1">Collaboration top research subjects</th>
        <th class="col2">Publications</th>
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
    <h2>Co-publications by department</h2>
    <table id="rep5" class="pub-counts">
      <tr>
        <th class="col1">DTU department</th>
        <th class="col2">Publications</th>
        <th class="col3">`;
    html += name + ' department';
    html += `</th>
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
    responsiveCharts.forEach(x => resizeChart(x))

}


/*** FN TO RESIZE CHARTS ***/
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
    .domain([0, d3.max(yDomain)])
    .range([height - margin.top - margin.bottom, 0])


  // create x and y axis function
  let yAxis = (g) => g.call(d3.axisLeft(y))
  let xAxis = (g) => g.call(d3.axisBottom(x))


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
    .style('margin', 'auto')
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
    .attr('transform', translate.yAxis)

  svg.append('g')
    .call(xAxis)
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


    labels.append("text")
      .text( d => d.value)
      .attr("dx", function() { return -this.getBBox().width/2 + labelPaddingH/2+ 'px'})
      .attr("dy", function() { return this.getBBox().height/2 + 'px'})

    if (isRectLabel) {
      labels.insert("rect", 'text')
      .datum( function() { return this.nextSibling.getBBox() })
      .attr("x", (d) => (d.x - labelPaddingH))
      .attr("y", (d) => d.y - labelPaddingV-1)
      .attr("width", (d) => d.width + 2 * labelPaddingH)
      .attr("height", (d) => d.height + 2 * labelPaddingV)
      .attr('fill', '#fff')

      // if want to set border and radius
      // .attr('rx', '3')
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
      ox = svg.select('.ox')

  oy.attr('transform', 'translate(-7, 0)')

  svg.selectAll('.bar')
    .attr('fill', barColor)
  oy.select('.domain').remove()
  oy.selectAll('text')
    .style('font-size', oyF )

  ox.selectAll('text').style('font-size', oxF)

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
      .domain( [d3.max(xDomain), 0] ).nice()
      .range([width-margin.right-margin.left, 0])


  let xAxis = (g) => g.call(d3.axisBottom(x))
  let yAxis = (g) => g.call(d3.axisLeft(y))


  // create svg in dom
  let svg = d3.select(insertAt)
    .append('div')
    .attr('class', 'chart-box')
    .attr('minH', minHeight)
    .style('max-width', maxWidth + 'px')
    .style('overflow', 'hidden')
    .style('margin', 'auto')
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
   let serie = svg.select('.serie')

   serie.select('path')
      .attr('stroke', '#030F4F') // maybe color should be passed as option
      .attr('stroke-width', 2)

   let labels = svg.selectAll('.label')
    labels.select('rect').attr('fill', '#f3f3f0')

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


function doPubCountTable(totals, DTUtotals, copubs) {
    var years = {};
    $.each(totals, function(key, value) {
        var year = value.year.toString();
        if (year in years) {
            years[year]["org"] = value.number;
        } else {
            years[year] = [];
            years[year]["org"] = value.number;
        }
    });
    $.each(DTUtotals, function(key, value) {
        var year = value.year.toString();
        if (year in years) {
            years[year]["dtu"] = value.number;
        } else {
            years[year] = [];
            years[year]["dtu"] = value.number;
        }
    });
    $.each(copubs, function(key, value) {
        var year = value.year.toString();
        if (year in years) {
            years[year]["copubs"] = value.number;
        } else {
            years[year] = [];
            years[year]["copubs"] = value.number;
        }
    });
    var html = `
    <div id="pubCountTable">
    <hr/>
    <h2>Number of publications per year</h2>
    <table id="rep2" class="pub-counts">
      <tr>
        <th class="col1">Year</th>
        <th class="col2">
    `;
    html += uni.trim() + "</th>";
    html += "<th class=\"col3\">Technical University of Denmark</th>";
    html += "<th class=\"col4\">Co-publications</th></tr>";
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


    // generate linechart for response.categories
    // can't be created directly in a memory container if we want to render its elements positioned correctly
    // so we need to load it in a hidden (temporary) element in DOM, and then replace it to targeted place
    let tempChartHolder = document.createElement('div')
    tempChartHolder.className += 'chartDrawnButHidden'
    tempChartHolder.setAttribute("style", "position: absolute; top: -1000; left:-1000;")
    document.body.append(tempChartHolder)

    let myData = copubs.map(x => (
                              {
                                value: x.number,
                                date: new Date(x.year.toString())
                              }
                            ))
                      .sort( (a,b) => (a.date - b.date) )

    let copubsChartOpt = {
      data: myData,
      maxHeight: 500,
      minHeight: 270,
      startHeight: 300,
      startWidth: 800,
      maxWidth: 900,  // should be width of #collab-summary-container

      svgTitle: 'Number of Co-Publications Per Year',
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
        type: 'rect',
        paddingV: 2,
        paddingH: 8
      },
      styleFn: styleLineChart
    }

    lineChart(copubsChartOpt)

    html += tempChartHolder.innerHTML
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

function setExportLink(individualLocalName, startYear, endYear) {
    var href = "${urls.base}/excelExport/" + individualLocalName + ".xlsx?orgLocalName=" + individualLocalName;
    if(startYear != null) {
        href += "&startYear=" + startYear;
    }
    if(endYear != null) {
        href += "&endYear=" + endYear;
    }
    $("#report-export").attr("href", href);
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

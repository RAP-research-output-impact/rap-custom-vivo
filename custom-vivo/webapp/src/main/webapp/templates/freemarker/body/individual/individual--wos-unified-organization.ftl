<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Individual profile page template for foaf:Organization individuals (extends individual.ftl in vivo)-->

<#-- Do not show the link for temporal visualization unless it's enabled -->

<script src="${urls.theme}/js/jquery.corner.js"></script>
<script src="${urls.theme}/js/jquery.sortElements.js"></script>

<#assign affiliatedResearchAreas>
    <#include "individual-affiliated-research-areas.ftl">
</#assign>


<#include "individual.ftl">
<div style="height: 60px;"></div>

<script>
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
        doPubCountTable(response.org_totals, response.dtu_totals);
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
    if (response.categories.length > 0) {
        html += doPubCategoryTable(response.categories, startYear, endYear);
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
    console.log(response.categories);
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
        info = ' <button id="' + info + '" class="" style="border: 0px;"><span class="ui-icon ui-icon-info"></span></button>';
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

}

function doPubCountTable(totals, DTUtotals) {
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
    var html = `
    <div id="pubCountTable">
    <hr/>
    <h2 class="rep">Compare the annual publication output</h2>
    <table id="rep2" class="pub-counts">
      <tr>
        <th class="col1">Year</th>
        <th class="col2">Partner</th>
        <th class="col3">DTU</th>
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
        html += "</td></tr>";
    });
    html += "</table><sup>*</sup> <span class=\"footnote\">The number of publications for a year will not be complete until the middle of the following year due to latency of indexing publications in Web of Science.</span></div>";
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

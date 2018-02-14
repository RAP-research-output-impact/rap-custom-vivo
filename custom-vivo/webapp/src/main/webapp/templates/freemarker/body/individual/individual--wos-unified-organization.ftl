<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Individual profile page template for foaf:Organization individuals (extends individual.ftl in vivo)-->

<#-- Do not show the link for temporal visualization unless it's enabled -->

<script src="${urls.theme}/js/jquery.corner.js"></script>

<#assign affiliatedResearchAreas>
    <#include "individual-affiliated-research-areas.ftl">
</#assign>


<#include "individual.ftl">

<script>
//co-publication report
$("span.display-title").html('');
var uni = $("h1.fn").text();
$("h1.fn").html("DTU collaboration with " + uni.trim() +
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
    var html = `
        <h2 id="collab-summary">
            <span id="collab-summary-total"></span> co-publications
            <span id="collab-summary-cat"></span>
            <a class="report-export" href="#">Export</a>
        </h2>
    `;
    $("#startYear").corner();
    $("#endYear").corner();
    $("#individual-info").append(html);
    $("#startYear").change(function() {
        info_message("Updating Co-publication report for start year " + $("#startYear").val());
        loadPubInfoByStartYear(vds, $("#startYear").val(), $("#endYear").val(), collabSummary);
    });
    $("#endYear").change(function() {
        info_message("Updating Co-publication report for end year " + $("#endYear").val());
        loadPubInfoByStartYear(vds, $("#startYear").val(), $("#endYear").val(), collabSummary);
    });
    loadPubInfo(vds, collabSummary);
    document.addEventListener('click', function (e) {
        if (hasClass(e.target, 'report-export')) {
            var html = document.querySelector("table").outerHTML
            exportTable(html, "co-publication-" + individualLocalName + ".tsv");
        } else if (hasClass(e.target, 'view-dept')) {
            $(e.target).parents('tr').nextUntil('.copubdept-head').toggle();
            label = $(e.target);
            if(label.html()=="Show details"){
                label.html('Hide details');
            }else{
                label.html('Show details');
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
    $("#individual-info").append("<div id=\"info-message\"></div><div id=\"info-message-spacer\"></div>");
    $("ul.propertyTabsList").before("<div id=\"group-tab-spacer\"></div>");
}

function info_message(msg) {
    $("#info-message").html(msg + "<img src=\"${urls.theme}/images/loading.gif\"/>");
    $("#info-message-spacer").show();
}

function info_message_reset() {
    $("div#info-message").html ("");
    $("#info-message-spacer").hide();
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
        doPubCountTable(response.org_totals);
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
    console.log(response.categories);
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
    html += "<tr><td class=\"rep-label\">Impact</td><td class=\"rep-num\">" + orgImpact + "</td><td class=\"rep-num\">" + dtuImpact + "</td>";

    var closeHtml = "</table></div>";
    html += closeHtml;
    $("#collab-summary-container").append(html);
}

function doTopCategoryTable(response) {
    var html = `
    <table id="rep3" class="pub-counts" style="display: inline-block; vertical-align: top;">
        <tr>
            <th class="col1">Partner&apos;s top research subjects</th>
            <th class="col2">Number</th>
        </tr>
    `;
    $.each( response.top_categories, function( key, value ) {
        //console.log(value);
        var row = "<tr class=\"rep-row\" id=\"tc-" + idkey(value.name) + "\"><td class=\"rep-label\">" + value.name + "</td><td class=\"rep-num\">" + value.number + "</td></tr>";
        html += row;
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
        <th class="col2">Number</th>
      </tr>
    `;
    $.each( totals.slice(0, 20), function( key, value ) {
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
        <th class="col2">Number</th>
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
            row += value.name + "</td><td class=\"dtu-dept-num\">" + coPubLink + "</td><td><a class=\"view-dept\">Show details</a></td></tr>"
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

function doPubCountTable(totals, startYear) {
    var html = `
    <div id="pubCountTable">
    <hr/>
    <h2>Number of publications per year</h2>
    <table id="rep2" class="pub-counts">
      <tr>
        <th class="col1">Year</th>
        <th class="col2">
    `;
    html += uni.trim() + "</th></tr>";
    var closeHtml = "</table></div>";
    $.each( totals, function( key, value ) {
        //console.log(value);
        var row = "<tr><td class=\"rep-label\">" + value.year + "</td><td class=\"rep-num\">" + value.number + "</td></tr>";
        html += row;
    });
    html += closeHtml;
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

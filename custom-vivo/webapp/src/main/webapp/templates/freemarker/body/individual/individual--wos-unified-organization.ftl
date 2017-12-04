<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Individual profile page template for foaf:Organization individuals (extends individual.ftl in vivo)-->

<#-- Do not show the link for temporal visualization unless it's enabled -->

<#assign affiliatedResearchAreas>
    <#include "individual-affiliated-research-areas.ftl">
</#assign>


<#include "individual.ftl">

<script>
//co-publication report
var orgLocalName = individualLocalName;
var base = "${urls.base}";
var vds = base + '/vds/report/org/' + individualLocalName;
var vdsOrgs = base + '/vds/report/org/' + individualLocalName + "/orgs";
var byDeptUrl = base + '/vds/report/org/' + individualLocalName + "/by-dept";
info_message("Loading Co-publication report");
loadPubInfo(vds, collabSummary);
$('#overview').addClass('spinner');
addDateSelector();

function addDateSelector() {
    var startYearSelectorHtml = "<p>Start year <select id=\"startYear\" name=\"startYear\"></select></p>";
    $("section#individual-info").append(startYearSelectorHtml);
    document.getElementById("startYear").addEventListener('change', function() {
           alert(this.value);
           loadPubInfoByStartYear(vds, this.value, collabSummary);
    }, false);

}

function info_message(msg) {
    $("section#individual-info").append("<div id=\"info-message\"><div>" + msg + "<img src=\"${urls.theme}/images/loading.gif\"/></div>" +
                                        "<div id=\"info-message-spacer\"></div><div id=\"info-message-spacer\"></div></div>");
}

function info_message_reset() {
    $("div#info-message").html ("");
    $("section#individual-info").append("<div id=\"info-message-spacer\"></div>");
}

function collabSummary(response, startYear) {
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
            $("#startYear").append("<option value=\"" + yearRange[i] + "\");\">" + yearRange[i] + "</option>");
        }
    }
    $("collab-summary").remove();

    if (individualLocalName != "org-technical-university-of-denmark") {
        var msg = "<h2 id=\"collab-summary\">Co-publications: " + response.summary.coPubTotal + " total ";
        if (response.categories.length > 0) {
            msg += "in " + response.categories.length + " categories ";
        }
        msg += "from " + yearRange[0] + " to " + yearRange.slice(-1)[0] + ".<a class=\"report-export\" href=\"#\">Export</a></h2>";
        $("section#individual-info").append(msg);
        doSummaryTable(response)
        if (response.org_totals.length != 0) {
            doPubCountTable(response.org_totals);
        }
        if (response.top_categories.length != 0) {
            doTopCategoryTable(response);
        }
        if (response.categories.length > 0) {
            doPubCategoryTable(response.categories);
        }
        loadPubInfo(byDeptUrl, byDeptReport)
    };
}


function byDeptReport(response) {
    if (response.departments.length > 0) {
        doDepartmentTable(response.departments, response.name);
    }
    info_message_reset();
}


function doCategories(response) {
	console.log(response.categories);
	$("ul#collab-summary").append("<li>Co-publication categories: " + response.categories.length + "</li>");
}


function doSummaryTable(response) {
    $("summaryTable").remove();
    var html = `
    <div id="summaryTable">
    <hr/>
    <h2>Overview</h2>
    <table class="pub-counts">
      <tr>
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
    html += "<tr><th></th><th>" + response.summary.name + "</th><th>Technical University of Denmark</th></tr>";
    html += "<tr><td class=\"rep-label\">Publications</td><td class=\"rep-num\">" + orgTotal + "</td><td class=\"rep-num\">" + dtuTotal + "</td>";
    html += "<tr><td class=\"rep-label\">Citations</td><td class=\"rep-num\">" + orgTotalCites + "</td><td class=\"rep-num\">" + dtuTotalCites + "</td>";
    //Impact
    html += "<tr><td class=\"rep-label\">Impact</td><td class=\"rep-num\">" + orgImpact + "</td><td class=\"rep-num\">" + dtuImpact + "</td>";

    var closeHtml = "</table></div>";
    html += closeHtml;
    $("#individual-info").append(html);
    $('#overview').removeClass('spinner');
}

function doTopCategoryTable(response) {
    $("topCategoryTable").remove();
    var html = `
    <div id="topCategoryTable">
    <hr/>
    <h2>Top research subjects</h2>
    <table class="pub-counts">
      <tr>
    `;
    html += "<tr><th>Category</th><th>Number</th></tr>";

    $.each( response.top_categories, function( key, value ) {
        //console.log(value);
        var row = "<tr><td class=\"rep-label\">" + value.name + "</td><td class=\"rep-num\">" + value.number + "</td></tr>";
        html += row;
    });
    var closeHtml = "</table></div>";
    html += closeHtml;
    $("#individual-info").append(html);
}

function doPubCategoryTable(totals) {
    $("pubCategoryTable").remove();
    var html = `
    <div id="pubCategoryTable">
    <hr/>
    <h2>Co-publications by category. Top 10</h2>
    <table class="pub-counts">
      <tr>
        <th>Category</th>
        <th>Number</th>
      </tr>
    `;

    var closeHtml = "</table></div>";
    $.each( totals.slice(0, 10), function( key, value ) {
        //console.log(value);
        var coPubLink = "<a href=\"" + base + "/copubs-by-category/" + value.category.split("/")[4] + "?collab=" + individualLocalName + "\" target=\"copubcategory\">" +  value.number + "</a>";
        var row = "<tr><td class=\"rep-label\">" + value.name + "</td><td class=\"rep-num\">" + coPubLink + "</td></tr>";
        html += row;
    });
    html += closeHtml;
    $("#individual-info").append(html);
}


function doDepartmentTable(totals, name) {
    $("departmentTable").remove();
    var html = `
    <div id="departmentTable">
    <hr/>
    <h2>Co-publications by department</h2>
    <table class="pub-counts">
      <tr>
        <th>DTU department</th>
        <th>Number</th>
        <th>`;
    html += name + ' department';
    html += `</th>
      </tr>
    `;

    var closeHtml = "</table></div>";
    var last = null;
    $.each( totals, function( key, value ) {
        if (value.name != last) {
            link = "<a href=\"" + base + "/individual?uri=" + value.org + "\">" + value.name + "</a>";
            var coPubLink = "<a href=\"" + base + "/copubs-by-dept/" + value.org.split("/")[4] + "?collab=" + individualLocalName + "\" target=\"copubdept\">" +  value.num + "</a>";
            //link = value.name;
            var row = "<tr class=\"copubdept-head\"><td class=\"rep-label\">";
            row += value.name + "</td><td class=\"dtu-dept-num\">" + coPubLink + "</td><td><a class=\"view-dept\">Show details</a></td></tr>"
            html += row
        }
        $.each( value.sub_orgs, function( k2, subOrg ) {
            var row = "<tr class=\"copubdept-child\"><td>";
            var clink = "<a href=\"" + base + "/copubs-by-dept/" + value.org.split("/")[4] + "?collab=" + individualLocalName + "&collabSub=" + subOrg.uri.split("/")[4] + "&collabSubName=" + encodeURIComponent(subOrg.name) + "\" target=\"copubdept\">" +  subOrg.total + "</a>";
            row +=  "</td><td class=\"rep-num\">" + clink + "</td><td>" + subOrg.name + "</td></tr>";
            html += row;
        });
        last = value.name;
    });
    html += closeHtml;
    $("#individual-info").append(html);
}

function doPubCountTable(totals) {
    $("pubCountTable").remove();
    var html = `
    <div id="pubCountTable">
    <hr/>
    <h2>Total publications per year</h2>
    <table class="pub-counts">
      <tr>
        <th>Year</th>
        <th>Number</th>
      </tr>
    `;

    var closeHtml = "</table></div>";
    $.each( totals, function( key, value ) {
        //console.log(value);
        var row = "<tr><td class=\"rep-label\">" + value.year + "</td><td class=\"rep-num\">" + value.number + "</td></tr>";
        html += row;
    });
    html += closeHtml;
    $("#individual-info").append(html);
}

function loadPubInfo(url, callback) {
    loadPubInfoByStartYear(url, -1, callback);
}

function loadPubInfoByStartYear(url, startYear, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', url );
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response);
            callback(response, startYear);
        }
        else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}


//Bind events to dynamically added elements.
//http://jsfiddle.net/ramswaroop/Nrxp5/28/
function hasClass(elem, className) {
    return elem.className.split(' ').indexOf(className) > -1;
}

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
            for (var j = 0; j < cols.length; j++)
                row.push("\"" + cols[j].innerText + "\"");
                csv.push(row.join("\t"));
        }
        csv.push("\n");
    }

    // Download CSV
    downloadCsv(csv.join("\n"), filename);
}

</script>

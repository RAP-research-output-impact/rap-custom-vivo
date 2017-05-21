<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Individual profile page template for foaf:Organization individuals (extends individual.ftl in vivo)-->

<#-- Do not show the link for temporal visualization unless it's enabled -->

<#assign affiliatedResearchAreas>
    <#include "individual-affiliated-research-areas.ftl">
</#assign>


<#include "individual.ftl">

<script>
//co-publication report
var base = "${urls.base}";
var vds = base + '/vds/report/org/' + individualLocalName;
var vdsOrgs = base + '/vds/report/org/' + individualLocalName + "/orgs";
loadPubInfo(vds, collabSummary);

function collabSummary(response) {
    var yearRange = [];
    if (response.org_totals != []) {
        for(var i in response.org_totals) { yearRange.push(response.org_totals[i].year) };
        yearRange.sort()
    } else {
        yearRange = [2015, 2016];
    }
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
    };
}

function doCategories(response) {
	console.log(response.categories);
	$("ul#collab-summary").append("<li>Co-publication categories: " + response.categories.length + "</li>");
}


function doSummaryTable(response) {
    var html = `
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
    var orgImpact = response.summary.orgImpact.toFixed(1);
    var dtuImpact = response.summary.dtuImpact.toFixed(1);
    html += "<tr><th></th><th>" + response.summary.name + "</th><th>Technical University of Denmark</th></tr>";
    html += "<tr><td>Publications</td><td>" + orgTotal + "</td><td>" + dtuTotal + "</td>";
    html += "<tr><td>Citations</td><td>" + orgTotalCites + "</td><td>" + dtuTotalCites + "</td>";
    //Impact
    html += "<tr><td>Impact</td><td>" + orgImpact + "</td><td>" + dtuImpact + "</td>";

    var closeHtml = "</table>";
    html += closeHtml;
    $("#individual-info").append(html);
}

function doTopCategoryTable(response) {
    var html = `
    <hr/>
    <h2>Top research subjects</h2>
    <table class="pub-counts">
      <tr>
    `;
    html += "<tr><th>Category</th><th>Number</th></tr>";

    $.each( response.top_categories, function( key, value ) {
        //console.log(value);
        var row = "<tr><td>" + value.name + "</td><td>" + value.number + "</td></tr>";
        html += row;
    });
    var closeHtml = "</table>";
    html += closeHtml;
    $("#individual-info").append(html);
}

function doPubCategoryTable(totals) {
    var html = `
    <hr/>
    <h2>Co-publications by category. Top 10</h2>
    <table class="pub-counts">
      <tr>
        <th>Category</th>
        <th>Number</th>
      </tr>
    `;

    var closeHtml = "</table>";
    $.each( totals.slice(0, 10), function( key, value ) {
        //console.log(value);
        var row = "<tr><td>" + value.name + "</td><td>" + value.number + "</td></tr>";
        html += row;
    });
    html += closeHtml;
    $("#individual-info").append(html);
}

function doPubCountTable(totals) {
    var html = `
    <hr/>
    <h2>Total publications per year</h2>
    <table class="pub-counts">
      <tr>
        <th>Year</th>
        <th>Number</th>
      </tr>
    `;

    var closeHtml = "</table>";
    $.each( totals, function( key, value ) {
        //console.log(value);
        var row = "<tr><td>" + value.year + "</td><td>" + value.number + "</td></tr>";
        html += row;
    });
    html += closeHtml;
    $("#individual-info").append(html);
}

function loadPubInfo(url, callback) {

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


//Bind events to dynamically added elements.
//http://jsfiddle.net/ramswaroop/Nrxp5/28/
function hasClass(elem, className) {
    return elem.className.split(' ').indexOf(className) > -1;
}

document.addEventListener('click', function (e) {
    if (hasClass(e.target, 'report-export')) {
        var html = document.querySelector("table").outerHTML
        exportTable(html, "co-publication-" + individualLocalName + ".tsv");
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

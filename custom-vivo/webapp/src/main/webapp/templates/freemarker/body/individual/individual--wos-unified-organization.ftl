<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#-- Individual profile page template for foaf:Organization individuals (extends individual.ftl in vivo)-->
<#-- Do not show the link for temporal visualization unless it's enabled -->

<script src="${urls.theme}/js/jquery.corner.js"></script>
<script src="${urls.theme}/js/jquery.sortElements.js"></script>
<script src="${urls.theme}/js/d3-v5.min.js"></script>
<script src="${urls.theme}/js/copub-util.js?ver=CACHEVERSION"></script>
<script src="${urls.theme}/js/copub-report.js?ver=CACHEVERSION"></script>

<#assign affiliatedResearchAreas>
    <#include "individual-affiliated-research-areas.ftl">
</#assign>
<#include "individual.ftl">

<div id="copubs-rep">
    <div id="collab-title">
        DTU collaboration report for the timespan
        <span id="range-container"></span>
        <form id="export-report_form" method="post" action="" style="display: inline-block; float: right;">
            <input class="export-report export-report_local-name" name="orgLocalName" type="text" hidden value="" />
            <input class="export-report export-report_start-year" name="startYear" type="text" hidden value="" />
            <input class="export-report export-report_end-year" name="endYear" type="text" hidden value="" />
            <input class="export-report export-report_svg1" type="text" name="svgStr1" hidden value="" />
            <input class="export-report export-report_svg2" type="text" name="svgStr2" hidden value="" />
            <button type="submit" class="export-report_btn">Download Excel</button>
        </form>
    </div>
    <div id="collab-uni">
        <span id="collab-summary-uni"></span><span id="collab-summary-country" class="hidden"></span>
    </div>
    <div id="collab-sub">
        Collaboration reports cover all DTU departments – for a breakdown by department see section 6
    </div>
    <h2>Contents:</h2>
    <ol id="copubs-rep-toc">
        <li><a onClick="scrollToID ('rep0-top');">Collaboration overview</a></li>
        <li><a onClick="scrollToID ('rep1-top');">Compare key output and impact indicators</a></li>
        <li><a onClick="scrollToID ('rep2-top');">Compare annual publication and co-publication output</a></li>
        <li><a onClick="scrollToID ('rep3-top');">Compare partner’s top subjects with DTU and co-publications</a></li>
        <li><a onClick="scrollToID ('rep4-top');">Compare top collaboration subjects with partner and DTU subjects</a></li>
        <li><a onClick="scrollToID ('rep5-top');">Collaboration by DTU department</a></li>
        <li><a onClick="scrollToID ('rep6-top');">Collaboration by DTU researcher (top 20)</a></li>
        <li><a onClick="scrollToID ('rep7-top');">Collaboration by funder (top 20)</a></li>
        <li><a onClick="scrollToID ('rep8-top');">Notes and hints</a></li>
    </ol>
    <!-- Report 0 -->
    <hr id="rep0-top"/>
    <h2 class="rep">1. Collaboration overview</h2>
    <div>
        In total: <span id="collab-summary-total"></span> co-publications <span id="collab-summary-cat"></span> during timespan
    </div>
    <h3>Number of co-publications per year</h3>
    <h4 class="rep">Note: In Web of Science, the data for a particular publication year is not complete until the middle of the following year</h4>
    <div id="co-pub-per-year-chart">
    </div>
    <h3>Number of co-publications by top research subjects</h3>
    <div id="co-pub-per-subject-chart">
    </div>
    <!-- Report 1 -->
    <a name="rep1"></a>
    <hr id="rep1-top"/>
    <h2 class="rep">2. Compare key output and impact indicators</h2>
    <table id="rep1" class="pub-counts">
        <thead>
            <tr>
                <th class="col1">Indicator</th>
                <th class="col2">Partner</th>
                <th class="col3">DTU</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <!-- Report 2 -->
    <hr id="rep2-top"/>
    <h2 class="rep">3. Compare the annual publication and co-publication output</h2>
    <table id="rep2" class="pub-counts">
        <thead>
            <tr>
                <th class="col1">Year</th>
                <th class="col2">Partner pubs</th>
                <th class="col3">DTU pubs</th>
                <th class="col4">Co-pubs</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <!-- Report 3 -->
    <hr id="rep3-top"/>
    <h2 class="rep">4. Compare partner’s top subjects with DTU and co-publications</h2>
    <table id="rep3" class="pub-counts">
        <thead>
            <tr>
                <th class="col1" rowspan="2">Partner’s top 20 subjects</th>
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
                <th class="col21">Pubs</th>
                <th class="col22">Rank</th>
                <th class="col31">Pubs</th>
                <th class="col32">Rank</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <!-- Report 4 -->
    <hr id="rep4-top"/>
    <h2 class="rep">5. Compare top collaboration subjects with partner and DTU subjects</h2>
    <table id="rep4" class="pub-counts">
        <thead>
            <tr>
                <th class="col1">Collaboration top 20 subjects</th>
                <th class="col2">Co-pubs</th>
                <th class="col3">Partner rank</th>
                <th class="col4">DTU rank</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <!-- Report 5 -->
    <hr id="rep5-top"/>
    <h2 class="rep">6. Collaboration by DTU department</h2>
    <table id="rep5" class="pub-counts">
        <thead>
            <tr>
                <th class="col1">DTU department</th>
                <th class="col2">Co-pubs</th>
                <th class="col3">Partner departments</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <!-- Report 6 -->
    <hr id="rep6-top"/>
    <h2 class="rep">7. Collaboration by DTU researcher (top 20)</h2>
    <table id="rep6" class="pub-counts">
        <thead>
            <tr>
                <th class="col1">DTU researcher</th>
                <th class="col2">Co-pubs</th>
                <th class="col3">Partner researcher</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <!-- Report 7 -->
    <hr id="rep7-top"/>
    <h2 class="rep">8. Collaboration by funder (top 20)</h2>
    <table id="rep7" class="pub-counts">
        <thead>
            <tr>
                <th class="col1">Funder</th>
                <th class="col2">Co-pubs</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <!-- Report 8 -->
    <hr id="rep8-top"/>
    <h2 class="rep">9. Notes and hints</h2>
    <div>
        <strong>Source:</strong> All data is retrieved from Web of Science and InCites of Clarivate Analytics.
    </div>
    <div>
        <strong>Hints:</strong> How to use the eight sections of the collaboration report.
    </div>
    <h3 class="notes">1. Collaboration overview</h3>
    <div style="margin-left: 40px;">
        <div>Quick overview of the collaboration:</div>
        <ul style="margin-left: 40px; list-style: disc;">
            <li>How many co-publications (in the selected timespan)?</li>
            <li>How many subject categories (out of 250 in total)?</li>
            <li>What are the most popular subject categories?</li>
        </ul>
        <div><strong>Remember</strong> that you may change the timespan and generate a new report.</div>
    </div>
    <h3 class="notes">2. Compare key output and impact indicators</h3>
    <div style="margin-left: 40px;">
        <div>Compare DTU and the chosen partner in the chosen timespan:</div>
        <ul style="margin-left: 40px; list-style: disc;">
            <li>How many publications and citations?</li>
            <li>How are they doing wrt. citation impact – simple and normalized?</li>
            <li>How are they doing wrt. excellence – proportion of publications in top 10% and top 1% most cited?</li>
            <li>How much are they collaborating – internationally and with industry?</li>
        </ul>
    </div>
    <h3 class="notes">3. Compare annual publication and co-publication output</h3>
    <div style="margin-left: 40px;">
        Year by year: How many publications from the two universities and how many co-publications?
    </div>
    <h3 class="notes">4. Compare partner’s top subjects with DTU and co-publications</h3>
    <div style="margin-left: 40px;">
        <div>Top subjects of the partner, of DTU and of the resulting co-publications:</div>
        <ul style="margin-left: 40px; list-style: disc;">
            <li>Sort by partner to see the partner’s top 20 subjects.</li>
            <li>And how they rank on the DTU side?</li>
            <li>Are we collaborating in the partner’s top 20 subjects, or outside?</li>
        </ul>
    </div>
    <h3 class="notes">5. Compare top collaboration subjects with partner and DTU subjects</h3>
    <div style="margin-left: 40px;">
        <div>Looking at the top 20 subjects of the co-publications:</div>
        <ul style="margin-left: 40px; list-style: disc;">
            <li>How do they match the top 20 of the partner?</li>
            <li>How do they match the top 20 of DTU?</li>
        </ul>
    </div>
    <h3 class="notes">6. Collaboration by DTU department</h3>
    <div style="margin-left: 40px;">
        <div>Listing all the DTU departments involved in the collaboration:</div>
        <ul style="margin-left: 40px; list-style: disc;">
            <li>How many co-publications for each department?</li>
            <li>Follow link to see a list of a particular department’s co-publications:</li>
            <ul style="margin-left: 40px; list-style: circle;">
                <li>Title of publications, involved researchers on DTU side as well as partner side.</li>
                <li>Link to all details about a single publication and its citations.</li>
            </ul>
            <li>Expand to see the departments involved on the partner side.</li>
        </ul>
    </div>
    <h3 class="notes">7. Collaboration by DTU researcher (top 20)</h3>
    <div style="margin-left: 40px;">
        <div>Listing the 20 most active DTU researchers in this collaboration in this timespan:</div>
        <ul style="margin-left: 40px; list-style: disc;">
            <li>Follow link to all the co-publications of a particular researcher.</li>
        </ul>
    </div>
    <h3 class="notes">8. Collaboration by funder (top 20)</h3>
    <div style="margin-left: 40px;">
        <div>Listing the 20 most used funders in this collaboration in this timespan. NB:</div>
        <ul style="margin-left: 40px; list-style: disc;">
            <li>Not all publications provide funding details.</li>
            <li>Funder names are not (yet) normalized, but Clarivate is working to achieve this soon.</li>
        </ul>
    </div>
</div>
<hr />

<div style="height: 60px;"></div>
<div style="display: none;">
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
</div>

<style>
    #co-pub-per-subject-chart .oy .tick text {
        font-size: 14px;
    }
    h3 {
        font-size: 18px;
        font-weight: bold;
    }
    h3.notes {
        color: #b50404;
    }
    h2.rep {
        margin-top: 20px;
    }
    h4.rep {
        font-style: italic;
        font-size: 1.0em;
    }
    #copubs-rep-toc {
        margin-left: 80px;
        list-style: decimal;
        margin-bottom: 24px;
    }
    #copubs-rep-toc a {
        cursor: pointer;
    }
    #rep3 th.col1 {
        vertical-align: middle;
    }
    #copubs-rep {
        margin-left: 20px;
    }
    #collab-title {
        margin-top: 25px;
        font-size: 24px;
        font-weight: bold;
        color: #595B5B;
    }
    #collab-title span {
        font-size: 24px;
        font-weight: bold;
        color: #595B5B;
    }
    #collab-title select {
        font-size: 20px;
        font-weight: bold;
        color: #595B5B;
    }
    #collab-sub {
        margin-top: 10px;
        font-style: italic;
    }
    #export-report_form {
        float: right;
    }
    #collab-uni span {
        font-size: 20px;
        font-weight: bold;
        color: #595B5B;
    }
    #year-from {
        margin-bottom: 3px;
    }
    #year-to {
        margin-bottom: 3px;
    }
    h1.fn {
        display: none;
    }
</style>

<script>
    var individualUri = "${individual.uri}";
    var svgStr1;
    var svgStr2;

//  co-publication report
    $("span.display-title").html('');
    var uni = $("h1.fn").text();
    $("h1.fn").html('');
    $("#collab-summary-uni").html(uni.trim());
    bc_range_setup("range-container", range_change); 
    if (individualLocalName != "org-technical-university-of-denmark") {
        var orgLocalName = individualLocalName;
        var base = "${urls.base}";
        var vds = base + '/vds/report/org/' + individualLocalName;
        var vdsOrgs = base + '/vds/report/org/' + individualLocalName + "/orgs";
        var byDeptUrl = base + '/vds/report/org/' + individualLocalName + "/by-dept";
        info_message_setup();
        info_message("Loading Co-publication report");
//      var searchLink = base + "/search?classgroup=http%3A%2F%2Fvivoweb.org%2Fontology%23vitroClassGrouppublications&querytext=&facet_organization-enhanced_ss=" + encodeURIComponent(individualUri);
//      var html = `
//          <p><a href="--link--">Show list</a> of all publications since 2007</p>
//      `.replace('--link--', searchLink);
//      $("#individual-info").append(html);
        $("#startYear").corner();
        $("#endYear").corner();
        setExportFormBase("${urls.base}");
        setExportForm();

        loadPubInfoByStartYear(vds, range_from_val(), range_to_val(), collabSummary);
        document.addEventListener('click', function (e) {
            if (hasClass(e.target, 'view-dept')) {
                $(e.target).parents('tr').nextUntil('.copubdept-head').toggle();
                label = $(e.target);
                if (label.html()=="Expand to show details"){
                    label.html('Collapse to hide details');
                } else {
                    label.html('Expand to show details');
                }
                return false;
            }
            if (hasClass(e.target, 'view-resea')) {
                $(e.target).parents('tr').nextUntil('.copubresea-head').toggle();
                label = $(e.target);
                if (label.html()=="Expand to show details"){
                    label.html('Collapse to hide details');
                } else {
                    label.html('Expand to show details');
                }
                return false;
            }
        }, false);
    }
</script>

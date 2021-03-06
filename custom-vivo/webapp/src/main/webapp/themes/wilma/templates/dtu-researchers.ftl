<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"></script>
<link rel="stylesheet" href="https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css">
<script src="${urls.theme}/js/assessment.js?ver=CACHEVERSION"></script>
<script type="text/javascript" src="https://d3js.org/d3.v5.min.js"></script>
<link rel="stylesheet" href="${urls.theme}/css/assessment.css">
<div id="crumb" style="margin: 12px 0px 6px 6px;"></div>
<div id="researchers-section">
    <table id="researchers-content">
        <thead>
            <tr>
                <th>Name</th>
                <th>ORCID</th>
                <th>Email</th>
                <th>Department</th>
                <th>Job title</th>
            </tr>
        </thead>
    </table>
</div>
<div id="researcher-section" style="display: none;">
    <table id="researcher-info">
        <tr>
            <td id="researcher-info-photo" rowspan="4" style="display: none;"></td>
            <td id="researcher-info-name" colspan="5"></td>
        </tr>
        <tr>
            <td>ORCID:</td>
            <td id="researcher-info-orcid"></td>
            <td class="researcher-info-spacer"></td>
            <td>Earliest publication:</td>
            <td id="researcher-info-first-pub"></td>
        </tr>
        <tr>
            <td>ResearcherID:</td>
            <td id="researcher-info-rid"></td>
            <td class="researcher-info-spacer"></td>
            <td>Latest publication:</td>
            <td id="researcher-info-last-pub"></td>
        </tr>
        <tr>
            <td>Email:</td>
            <td id="researcher-info-email"></td>
            <td class="researcher-info-spacer"></td>
            <td>DTU affiliation:</td>
            <td id="researcher-info-start"></td>
        </tr>
        <tr>
            <td></td>
            <td></td>
            <td class="researcher-info-spacer"></td>
            <td>PhD year:</td>
            <td id="researcher-info-phd"></td>
        </tr>
    </table>
    <h2>DTU affiliations and positions since 2020 – as reported by the departments annually</h2>
    <table id="researcher-affiliation">
        <thead>
            <tr>
                <th>Year</th>
                <th>Department</th>
                <th>Section</th>
                <th>Job title</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <div id="researcher-pubs">
        <h2><b>Publications</b> - found in Web of Science using ORCID/ResearcherID – a publication may belong to more than one type</h2>
        <table id="researcher-summary">
        </table>
        <button id="researcher-publications">View all publications</button>
        <div id='pubCite-researcher'>
        </div>
        <table id="researcher-indicator">
            <thead>
                <tr>
                    <td colspan="3">
                        <h2 style="margin-bottom: 0;">Metrics based on these publications </h2>
                    </td>
                    <td colspan="4" align="right">
                        From
                        <select id="researcher-year-start" name="year-start" style="margin-bottom: 0;">
                            <option value="1900">1900</option>
                            <option value="2015">2015</option>
                            <option value="2016">2016</option>
                            <option value="2017">2017</option>
                            <option value="2018">2018</option>
                            <option value="2019">2019</option>
                            <option value="2020">2020</option>
                        </select>
                        -
                        <select id="researcher-year-end" name="year-end" style="margin-bottom: 0;">
                            <option value="2015">2015</option>
                            <option value="2016">2016</option>
                            <option value="2017">2017</option>
                            <option value="2018">2018</option>
                            <option value="2019">2019</option>
                            <option value="2020">2020</option>
                            <option value="2030" selected="1">2030</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="7" align="right">
                        <table id="researcher-doctype" style="width: 1048px">
                            <tr>
                                <td>
                                    Include publication types:
                                    <input type="checkbox" class="researcher-doctype" name="doctype" value="article" checked="checked">
                                    Article
                                </td>
                                <td>
                                    <input type="checkbox" class="researcher-doctype" name="doctype" value="review" checked="checked">
                                    Review
                                </td>
                                <td>
                                    <input type="checkbox" class="researcher-doctype" name="doctype" value="proceedings paper" checked="checked">
                                    Proceedings paper
                                </td>
                                <td>
                                    <input type="checkbox" class="researcher-doctype" name="doctype" value="abstract" checked="checked">
                                    Abstracts
                                </td>
                                <td>
                                    <input type="checkbox" class="researcher-doctype" name="doctype" value="correction" checked="checked">
                                    Corrections
                                </td>
                                <td>
                                    <input type="checkbox" class="researcher-doctype" name="doctype" value="other" checked="checked">
                                    Other
                                </td>
                                <td>
                                    <button class="doctype-button" onClick="flip_doctype('researcher-doctype', researcher_fetch);">All/None</button>
                                </td>
                            </tr>
                        <input type="hidden" id="researcher-orcid" value=""/>
                        </table>
                    </td>
                </tr>
                <tr>
                    <th>Publications</th>
                    <th>Citations</th>
                    <th>Cit./Publ.</th>
                    <th>Cit./Year</th>
                    <th title="H-index of the set of publications. Is calculated by counting the number of publications that been cited at least that same number of times.">H-index</th>
                    <th title="Percentage of publications that have international co-authors">International Collab.</th>
                    <th title="Percentage of publications that are identified as Open Access">Open Access</th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
    <div id="researcher-no-pubs" style="margin-top: 30px; width: 1032px; border: 2px solid black; padding: 6px; display: none;">
        <div style="font-size: 24px; text-align: center;">
            We’re sorry
        </div>
        <div style="font-size: 18px; text-align: center;">
            but we cannot retrieve any data for this researcher
        </div>
        <div style="font-size: 18px; text-align: center;">
            whether we use ORCID or ResearcherID
        </div>
    </div>
</div>
<div id="records-section" style="display: none;">
    <table id="records-filters">
        <tr>
            <td>
                <input type="hidden" id="records-id" value="">
                <input type="hidden" id="records-page" value="">
                Year:
                <select id="records-year-start">
                    <option value="" selected="1">All</option>
                    <option value="2020">2020</option>
                    <option value="2019">2019</option>
                    <option value="2018">2018</option>
                    <option value="2017">2017</option>
                    <option value="2016">2016</option>
                    <option value="2015">2015</option>
                </select>
                -
                <select id="records-year-end">
                    <option value="" selected="1">All</option>
                    <option value="2020">2020</option>
                    <option value="2019">2019</option>
                    <option value="2018">2018</option>
                    <option value="2017">2017</option>
                    <option value="2016">2016</option>
                    <option value="2015">2015</option>
                </select>
            </td>
            <td>
                Type:
                <select id="records-doctype">
                    <option value="" selected="1">All</option>
                    <option value="article">Article</option>
                    <option value="review">Review</option>
                    <option value="proceedings paper">Proceedings paper</option>
                    <option value="abstract">Abstracts</option>
                    <option value="correction">Corrections</option>
                    <option value="other">Other</option>
                </select>
            </td>
            <td>
                Affiliation:
                <select id="records-dtu">
                    <option value="" selected="1">All</option>
                    <option value="1">DTU</option>
                    <option value="0">Not-DTU</option>
                </select>
            </td>
            <td>
                Impact:
                <select id="records-impact">
                    <option value="" selected="1">All</option>
                </select>
            </td>
            <td>
                Access:
                <select id="records-access">
                    <option value="" selected="1">All</option>
                </select>
            </td>
            <td width="30%" align="right">
                <input id="records-search" type="text" size="30" onChange="records_fetch(null, 1);" placeholder="">
                <button class="records-button" onClick="records_fetch(null, 1);">Go</button>
            </td>
        </tr>
        <tr style="border: 0px;">
            <td colspan="3">
                Showing <span class="records-from"></span> to <span class="records-to"></span> of <span class="records-total"></span> publications
            </td>
            <td colspan="3" align="right">
                <span class="records-paging"></span>
                <button class="blue-button" onClick="records_fetch_excel();">Download Excel</button>
            </td>
        </tr>
    </table>
    <ul id="records-content">
    </ul>
    <table id="records-filters-footer">
        <tr style="border: 0px;">
            <td>
                Showing <span class="records-from"></span> to <span class="records-to"></span> of <span class="records-total"></span> publications
            </td>
            <td class="records-paging" align="right">
            </td>
        </tr>
    </table>
</div>
<div id="record-section" style="display: none;">
    <div id="record-title">
    </div>
    <div id="record-authors">
    </div>
    <div id="record-source">
    </div>
    <div id="record-pub">
    </div>
    <div id="record-abstract">
    </div>
    <div id="record-keywords">
    </div>
    <div id="record-class">
    </div>
    <div id="record-address">
    </div>
    <div id="record-funding">
    </div>
    <div id="record-doctype">
    </div>
    <div id="record-indicators">
    </div>
</div>
<script>
    $(document).ready(function() {
        state_set("researchers");
        fetch_department_options(update_crumb, "researchers");
        researchers_setup();
        researcher_setup();
        records_setup();
        record_setup();
        if (window.history && window.history.pushState) {
            console.log ('info: init history');
            $(window).on('popstate', function() {
                state_set();
            });
        } else {
            console.log ('error: no history');
        }
        console.log('info: URL: ' + window.location.href);
        $(function() {
            $(document).tooltip();
        });
    });
</script>


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
        <a href="copub-choose">Ext. collaboration</a>
        &gt;
        <span id="bc-dept-container">
        </span>
        &gt;
        <span id="bc-copub-type">
            Subjects
        </span>
        <span id="bc-copub-type-link">
            <a id="bc-copub-type-link-anchor">Subjects</a>
        </span>
        &gt;
        <span id="bc-main">
        </span>
        <span id="bc-range-container">
        </span>
    </div>
    <div id="copub-container">
        <table id="copub-main-list">
            <thead>
                <tr>
                    <th style="text-align: left; width: 600px;">
                        <div id="sort-main">
                            Subjects
                            <div class="sort-dir"></div>
                        </div>
                        <form class="copub-filter-form" onSubmit="return (false);">
                            <input id="copub-main-filter" type="text" size="30" placeholder="Type here to shorten list"/>
                        </form>
                    </th>
                    <th id="sort-main-pub">
                        Co-publications
                        <div class="sort-dir"></div>
                    </th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
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

    $(document).ready(function() {
        $('#copub-container').addClass('tab-spinner');
        bc_dept_setup("bc-dept-container", fetchSubjects);
        bc_range_setup("bc-range-container", fetchSubjects);
        $("#copub-org-list").hide();
        $("#bc-copub-type-link").hide();
        $("#bc-copub-type-link-anchor").click(function() {
            $("#bc-copub-type-link").hide();
            $("#bc-copub-type").show();
            $("#bc-main").hide();
            $('#copub-main-list').show();
            $('#copub-org-list').hide();
            bc_dept_edit();
            bc_range_edit();
        });
        filter_setup("main");
        filter_setup("org");
        fetchSubjects();
    });

    function fetchSubjects() {
        $("#copub-main-filter").val("");
        var url = "${urls.base}/vds/report/copub-subjects?dept=" + dept_val() + "&startYear=" + range_from_val() + "&endYear=" + range_to_val();
        console.log ("loading: " + url);
        $("#copub-main-list tbody").html("");
        loadData(url, subjectList);
    }
    function subjectList(data) {
        var tbody = "";
        for (var i = 0, j = data.subjects.length; i < j; i++){
            tbody += "<tr><td class=\"sort-main copub-main-row\"><a href=\"javascript:fetchOrgList('partners-by-subject', 'subject', '" +
                     data.subjects[i].subject + "', '" + data.subjects[i].name + "');\">" + data.subjects[i].name +
                     "</a></td><td class=\"sort-main-pub\" style=\"text-align: right;\">" + data.subjects[i].publications + "</td></tr>";
        }
        $("#copub-main-list tbody").html(tbody);
        setSort("sort-main");
        $('#copub-container').removeClass('tab-spinner');
    }
</script>

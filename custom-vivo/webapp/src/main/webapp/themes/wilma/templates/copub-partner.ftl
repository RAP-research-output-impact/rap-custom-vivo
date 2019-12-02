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
        <a href="../copub-choose">Ext. collaboration</a>
        &gt;
        <span id="bc-dept-container">
        </span>
        &gt;
        <span id="bc-copub-type">
            Partners
        </span>
        <span id="bc-copub-type-link">
            <a id="bc-copub-type-link-anchor">Partners</a>
        </span>
        &gt;
        <span id="bc-main">
        </span>
        <span id="bc-range-container">
        </span>
    </div>
    <div id="copub-container" style="min-height: 600px;">
        <table id="copub-org-list" style="width: 800px;">
            <thead>
                <tr>
                    <th style="text-align: left; width: 600px;">
                        <div id="sort-org" style="line-height: 30%;">
                            Collaboration partners
                            <div class="sort-dir"></div>
                            <br/>
                            <font size="-1" style="margin-top: -5px;">with at least 3 co-publications</font>
                        </div>
                        <form class="copub-filter-form" onSubmit="return (false);">
                            <input id="copub-org-filter" type="text" size="24" placeholder="Type here to shorten list"/>
                        </form>
                    </th>
                    <th id="sort-org-imp">
                        Normalised citation impact
                        <div class="sort-dir"></div>
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
        bc_dept_setup("bc-dept-container", fetchPartnerData);
        bc_range_setup("bc-range-container", fetchPartnerData);
        $("#bc-copub-type-link").hide();
        $("#bc-copub-type-link-anchor").click(function() {
            $("#bc-copub-type-link").hide();
            $("#bc-copub-type").show();
            $("#bc-main").hide();
            $('#copub-main-list').show();
            bc_dept_edit();
            bc_range_edit();
        });
        filter_setup("org");
        fetchPartnerData();
    });
    function fetchPartnerData() {
        $("#copub-main-filter").val("");
        var url = "${urls.base}/vds/report/partners?dept=" + dept_val() + "&startYear=" + range_from_val() + "&endYear=" + range_to_val();
        console.log ("loading: " + url);
        loadData(url, orgList);
    }
</script>

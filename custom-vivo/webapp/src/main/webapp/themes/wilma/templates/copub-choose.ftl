<#import "lib-home-page.ftl" as lh>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="${urls.theme}/js/copub-util.js"></script>
<section class="home-sections" id="copub-choose">
    <div id="copub-choose-heading">
        <h1 id="copub-choose-title">Explore our collaboration – by country, partner or subject</h1>
        <div style="font-weight: bold; margin-bottom: 12px;">
            Last DTU RAP Collaboration update: May 18<sup>th</sup> (based on InCites dataset updated April 28<sup>th</sup>)
        </div>
        <div style="font-weight: bold;">
            Note: this site does not work with older versions of <i>Microsoft</i> browsers.
            <br/>
            Please use <i>Chrome</i>, <i>Firefox</i> or <i>Edge version 44.18362.449.0</i> or higher.
        </div>
    </div>
    <div id="copub-choose-body">
        <table id="copub-choose-table" cellpadding="6" cellspacing="6" style="margin-left: auto; margin-right: auto; margin-top: 30px; margin-bottom: 60px;">
            <tr>
                <td style="width: 360px; padding-right: 20px; font-size: 120%;">
                    Start your exploration at university or department level.
                    Then choose one of the four paths of exploration.
                    At the end of all paths, you may request a full collaboration report for a given partner -
                    with details at the university level as well as at the department level.
                </td>
                <td style="width: 260px; padding-left: 20px;">
                    <select id="DTUdepartment" name="DTUdepartment">
                    </select>
                    <ul id="copub-choose-list">
                        <li><a href="javascript:copub_link('copub');">World map</a></li>
                        <li><a href="javascript:copub_link('copub-country');">Country - browse or search</a></li>
                        <li><a href="javascript:copub_link('copub-partner');">Partner - browse or search</a></li>
                        <!--
                        <li><a href="javascript:copub_link('copub-funder');">Funder - browse or search</a></li>
                        -->
                        <li><a href="javascript:copub_link('copub-subject');">Subject - browse or search</a></li>
                    </ul>
                </td>
            </tr>
        <table>
    </div>
    <div id="copub-choose-graphic" style="margin-top: 60px;">
        <img src="${urls.theme}/images/copub-wordmap.png" style="opacity: 80%;">
    </div>
</section>

<script>
    const urls_base = "${urls.base}";
    dept_options("DTUdepartment");
    function copub_link(base) {
        window.location.href = urls_base + '/' + base + '?dept=' + $("#DTUdepartment").val();
        return (false);
    }
</script>

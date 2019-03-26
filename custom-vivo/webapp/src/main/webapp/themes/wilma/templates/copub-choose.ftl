<#import "lib-home-page.ftl" as lh>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="${urls.theme}/js/copub-util.js"></script>
<section class="home-sections" id="copub-choose">
    <div id="copub-choose-heading">
        <h1 id="copub-choose-title">Explore the university’s external collaboration</h1>
        <div id="copub-choose-desc" style="text-align: center;">
            Collaboration as reflected in co-publications with external partners.<br/>
            All paths below lead to a full co-publication report for a given collaboration partner.<br/>
            Explore the university&apos;s collaboration at university level or department level.<br/>
        </div>
    </div>
    <div id="copub-choose-body">
        <table id="copub-choose-table" cellpadding="6" cellspacing="6" style="margin-left: auto; margin-right: auto; margin-top: 30px; margin-bottom: 60px;">
            <tr>
                <td>
                    <select id="DTUdepartment" name="DTUdepartment">
                    </select>
                </td>
                <td>
                    <ul id="copub-choose-list">
                        <li><a href="javascript:copub_link('copub');">World map</a></li>
                        <li><a href="javascript:copub_link('copub-country');">Country - browse or search</a></li>
                        <li><!--a href="javascript:copub_link('copub-partner');"-->Partner - browse or search<!--/a--></li>
                        <li><!--a href="javascript:copub_link('copub-funder');"-->Funder - browse or search<!--/a--></li>
                        <li><!--a href="javascript:copub_link('copub-subject');"-->Subject - browse or search<!--/a--></li>
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
    dept_options("DTUdepartment");
    function copub_link(base) {
        window.location.href = '../' + base + '?dept=' + $("#DTUdepartment").val();
        return (false);
    }
</script>

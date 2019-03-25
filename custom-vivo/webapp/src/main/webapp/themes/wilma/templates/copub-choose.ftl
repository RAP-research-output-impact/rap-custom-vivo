<#import "lib-home-page.ftl" as lh>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<section class="home-sections" id="copub-choose">
    <div id="copub-choose-heading">
        <h1 id="copub-choose-title">Explore the university’s external collaboration</h1>
        <div id="copub-choose-desc" style="text-align: center;">
            Collaboration as reflected in co-publications with external partners.<br/>
            All paths below lead to a full co-publication report for a given collaboration partner.<br/>
            Explore the university’s collaboration at university level or department level.<br/>
        </div>
    </div>
    <div id="copub-choose-body">
        <table id="copub-choose-table" cellpadding="6" cellspacing="6" style="margin-left: auto; margin-right: auto; margin-top: 30px; margin-bottom: 60px;">
            <tr>
                <td>
                    <select id="DTUdepartment" name="DTUdepartment">
                        <option value="">Entire university</option>
                        <option value="dtusuborg-centre-for-oil-and-gas-dtu">Centre for Oil and Gas - DTU</option>
                        <option value="dtusuborg-dtu-aqua">DTU Aqua</option>
                        <option value="dtusuborg-dtu-bioengineering">DTU Bioengineering</option>
                        <option value="dtusuborg-dtu-bioinformatics">DTU Bioinformatics</option>
                        <option value="dtusuborg-dtu-biosustain">DTU Biosustain</option>
                        <option value="dtusuborg-dtu-business">DTU Business</option>
                        <option value="dtusuborg-dtu-chemical-engineering">DTU Chemical Engineering</option>
                        <option value="dtusuborg-dtu-chemistry">DTU Chemistry</option>
                        <option value="dtusuborg-dtu-civil-engineering">DTU Civil Engineering</option>
                        <option value="dtusuborg-dtu-compute">DTU Compute</option>
                        <option value="dtusuborg-dtu-danchip">DTU Danchip</option>
                        <option value="dtusuborg-dtu-diplom">DTU Diplom</option>
                        <option value="dtusuborg-dtu-electrical-engineering">DTU Electrical Engineering</option>
                        <option value="dtusuborg-dtu-energy">DTU Energy</option>
                        <option value="dtusuborg-dtu-environment">DTU Environment</option>
                        <option value="dtusuborg-dtu-food">DTU Food</option>
                        <option value="dtusuborg-dtu-fotonik">DTU Fotonik</option>
                        <option value="dtusuborg-dtu-health-tech">DTU Health Tech</option>
                        <option value="dtusuborg-dtu-management-engineering">DTU Management Engineering</option>
                        <option value="dtusuborg-dtu-mechanical-engineering">DTU Mechanical Engineering</option>
                        <option value="dtusuborg-dtu-nanotech">DTU Nanotech</option>
                        <option value="dtusuborg-dtu-nutech">DTU Nutech</option>
                        <option value="dtusuborg-dtu-physics">DTU Physics</option>
                        <option value="dtusuborg-dtu-space">DTU Space</option>
                        <option value="dtusuborg-dtu-systems-biology">DTU Systems Biology</option>
                        <option value="dtusuborg-dtu-vet">DTU Vet</option>
                        <option value="dtusuborg-dtu-wind-energy">DTU Wind Energy</option>
                        <option value="dtusuborg-dtu-department-unknown">DTU department unknown</option>
                        <option value="dtusuborg-ris-dtu">Risø DTU</option>
                    </select>
                </td>
                <td>
                    <ul id="copub-choose-list">
                        <li><a href="javascript:copub_link('copub');">World map</a></li>
                        <li><a href="javascript:copub_link('copub-country');">Country - browse or search</a></li>
                        <li><a href="javascript:copub_link('copub-partner');">Partner - browse or search</a></li>
                        <li><a href="javascript:copub_link('copub-funder');">Funder - browse or search</a></li>
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
    function copub_link(base) {
        var dep = $("#DTUdepartment").val();
        var lab = $("#DTUdepartment option:selected").text();
        window.location.href = '../' + base + '?dept=' + dep + '&name=' + lab;
        return (false);
    }
</script>

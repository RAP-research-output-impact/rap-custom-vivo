<style>
    div#about {
        margin-left: auto;
        margin-right: auto;
        max-width: 1000px;
    }
    h2 div {
        display: inline-block;
        min-width: 40px;
    }
    h3 div {
        display: inline-block;
        min-width: 40px;
    }
    h2 {
        margin-block-end: 0;
    }
    h2.toc {
        margin-block-end: 0;
    }
    #toc {
        display: grid;
        grid-template-columns: 1fr minmax(260px, 300px);
    }
    #toc ol {
        list-style-type: none;
        padding-inline-start: 20px;
        line-height: 1.1em;
        margin-top: 6px;
    }
    #toc li {
        font-weight: bold;
        margin-bottom: 6px;
    }
    #toc ol ol li {
        font-weight: normal;
    }
    #toc a {
        text-decoration: none;
    }
    hr {
        margin-right: 100%;
    }
    ul {
        list-style-type: disc;
        padding-inline-start: 20px;
        margin-bottom: 20px;
    }
    ol {
        list-style-type: decimal;
        padding-inline-start: 20px;
        margin-bottom: 12px;
    }
    p {
        font-size: 100%;
    }
    p.ack {
        margin-bottom: 0;
        font-weight: bold;
    }
    p.list-head {
        margin-bottom: 6px;
    }
</style>
<div id="about">
    <h1>About the University Research Analytics Platform</h1>
    <h2 class="toc">Table of Contents</h2>
    <div id="toc">
        <div>
            <ol>
                <li><a href="#ch-1">1. Introduction</a></li>
                <ol>
                    <li><a href="#ch-1.1">1.1. Motivation and aims</a></li>
                    <li><a href="#ch-1.2">1.2. Overall data and technology choices</a></li>
                    <li><a href="#ch-1.3">1.3. Acknowledgement of contributors and funders</a></li>
                </ol>
                <li><a href="#ch-2">2. External Collaboration Analytics Module</a></li>
                <ol>
                    <li><a href="#ch-2.1">2.1. Purpose of module</a></li>
                    <li><a href="#ch-2.2">2.2. Data flow</a></li>
                </ol>
                <li><a href="#ch-3">3. Publication Search Module</a></li>
                <ol>
                    <li><a href="#ch-3.1">3.1. Purpose of module</a></li>
                    <li><a href="#ch-3.2">3.2. Data flow</a></li>
                </ol>
                <li><a href="#ch-4">4. Researcher Profile Module (DTU Researches)</a></li>
                <ol>
                    <li><a href="#ch-4.1">4.1. Purpose of module</a></li>
                    <li><a href="#ch-4.2">4.2. Data flow</a></li>
                    <li><a href="#ch-4.3">4.3. All DTU Researchers</a></li>
                    <li><a href="#ch-4.4">4.4. Single Researcher profile</a></li>
                    <li><a href="#ch-4.5">4.5. List of publications</a></li>
                    <li><a href="#ch-4.6">4.6. Full view of single publication</a></li>
                </ol>
                <li><a href="#ch-5">5. Organizational Unit Profile Module (DTU Units)</a></li>
                <ol>
                    <li><a href="#ch-5.1">5.1. Purpose of module</a></li>
                    <li><a href="#ch-5.2">5.2. Data flow</a></li>
                    <li><a href="#ch-5.3">5.3. All DTU Units</a></li>
                    <li><a href="#ch-5.4">5.4. Single Unit Profile</a></li>
                    <li><a href="#ch-5.5">5.5. List of publications</a></li>
                    <li><a href="#ch-5.6">5.6. Full view of single publication</a></li>
                </ol>
            </ol>
        </div>
        <div>
            <a href="/PDF/Manual-RAP-Researchers-and-Units-2021-03-05.pdf">
                <img src="${urls.theme}/images/download-manual.png"/></a>
        </div>
    </div>
    <a name="ch-1"></a>
    <h2><div>1.</div>Introduction</h2>
    <hr width="100%"/>
    <p>
        The University Research Analytics Platform (RAP) is an online service offering popular and standardized research analytics reports to the university’s researchers, research leadership, and administration.
        RAP makes these insights available online, with short response times, and in some cases, with richer information than would be possible with a manual approach.
    </p>
    <a name="ch-1.1"></a>
    <h3><div>1.1.</div>Motivation and aims</h3>
    <p>
        Many universities – especially in the areas of science, technology, medicine, and data-driven social sciences – use quantitative data as one of the inputs to their evaluation and planning processes, which also draw on qualitative assessments.
        Such bibliometrics/scientometrics traditionally deal with publications and the citations they receive from peers – as indicators of research output and impact. 
    </p>
    <p class="list-head">
        The RAP aims at making such bibliometric/scientometric analysis:
    </p>
    <ul>
        <li>As relevant as possible</li>
        <li>As clear as possible</li>
        <li>As precise as possible</li>
        <li>As open as possible</li>
        <li>As transparent as possible</li>
        <li>As reproducible as possible</li>
        <li>As reusable as possible</li>
    </ul>
    <p>
        Thus the RAP data, analytical concepts, software, and algorithms must - at the same time and in a balanced way - address these requirements for quality as well as transparency/openness.
    </p>
    <a name="ch-1.2"></a>
    <h3><div>1.2.</div>Overall data and technology choices</h3>
    <p>
        The Technical University of Denmark (DTU) instance of RAP (DTU RAP) uses data from two commercially available databases: Web of Science and InCites – both are licensed by the university and accessible to all with a campus login – just like the DTU RAP itself.
    </p>
    <p>
        The choice of data reflects DTU’s long experience with Web of Science data and its coverage and quality when it comes to reflecting the university’s research output.
        While the data is far from Open Access, it is commercially available to anyone who would like to reproduce the DTU RAP analytics – in order to check the validity or to set up a RAP for their own university.
    </p>
    <p>
        While the “behind the firewall” nature of the DTU RAP services align well with the access rights of its data, other implementors may opt for a RAP with a greater degree open access and open data.
        This is an implementor’s choice and the RAP system may be adapted to accommodate such a choice.
    </p>
    <p>
        The central software component is VIVO “a member-supported, open source software and an ontology for representing scholarship.
        VIVO supports recording, editing, searching, browsing, and visualizing scholarly activity.
        VIVO encourages showcasing the scholarly record, research discovery, expert finding, network analysis, and assessment of research impact.
        VIVO is easily extended to support additional domains of scholarly activity.” See more at <a target="_blank" href="https://duraspace.org/vivo/">https://duraspace.org/vivo/</a>
    </p>
    <p>
        The software used and developed for the various RAP modules is entirely Open Source and available for all to download, apply, or adapt at
        <a target="_blank" href="https://github.com/RAP-research-output-impact/rap-custom-vivo">https://github.com/RAP-research-output-impact/rap-custom-vivo</a>.
        There are some variations in how the DTU RAP modules import and process data, these are outlined below, module for module. 
    </p>
    <a name="ch-1.3"></a>
    <h3><div>1.3.</div>Acknowledgement of contributors and funders</h3>
    <p>
        Many people and organizations have contributed (and still contribute) to DTU RAP:
    </p>
    <p class="ack">
        Financial support
    </p>
    <ul>
        <li>
            DEFF – Denmark’s Electronic Research Library – has financially supported two projects encompassing VIVO-development activities.
            After DEFF was discontinued in 2019, the <a target="_blank" href="https://ufm.dk/en/the-ministry/organisation/danish-agency-for-science-and-higher-education">Danish Agency for Science and Higher Education</a> took over.
        </li>
        <ul>
            <li>
                <a target="_blank" href="http://libguides.sdu.dk/roi-av">ROI-AV</a> – Research Output &amp; Impact – Analyzed &amp; Visualized – 2016-2018 – with Work Package 3: VIVO as a Research Analytics Platform
            </li>
            <li>
                <a target="_blank" href="https://deffopera.dk/">OPERA</a> – Open Research Analytics – with Work Package 1: Open university research analytics system – Research collaboration and Work Package 4: Open university research analytics system – Research assessment
            </li>
        </ul>
    </ul>
    <p class="ack">
        Data and API services
    </p>
    <ul>
        <li><a href="mailto:Benjamin.Gross@Clarivate.com">Benjamin Gross</a>, Clarivate Analytics</li>
    </ul>
    <p class="ack">
        Software development
    </p>
    <ul>
        <li><a href="mailto:fkybus@gmail.com">Franck Falcoz</a>, Vox Novitas</li>
        <li><a href="mailto:lawlesst@gmail.com">Ted Lawless</a>, formerly Clarivate Analytics, now Brown University</li>
        <li><a target="_blank" href="https://ontocale.com/">Brian Lowe</a>, Ontocale SRL</li>
    </ul>
    <p class="ack">
        IT service and hardware
    </p>
    <ul>
        <li>
            <a target="_blank" href="https://www.dtu.dk/english/service/phonebook/person?id=43881&cpid=99405&tab=2&qt=dtupublicationquery">Michael Rasmussen</a> and <a target="_blank" href="https://www.dtu.dk/english/service/phonebook/person?id=45361&cpid=99406&tab=0">Martin Holmquist Schimmel</a>
            of the DTU IT Service provide and support the server environment for DTU RAP
        </li>
    </ul>
    <p class="ack">
        Bibliometric development and support
    </p>
    <ul>
        <li>
            <a href="mailto:chste@dtu.dk">Christina Steensboe</a>,
            <a href="mailto:kshi@dtu.dk">Karen Hytteballe Ibanez</a>,
            Mette Fjeldhagen, Nikoline Dohm Lauridsen, and 
            <a href="mailto:mosa@dtu.dk">Mogens Sandfær</a> -
            of the DTU Research Analytics Office.
        </li>
    </ul>
    <a name="ch-2"></a>
    <h2><div>2.</div>External Collaboration Analytics Module</h2>
    <hr width="100%"/>
    <a name="ch-2.1"></a>
    <h3><div>2.1.</div>Purpose of module</h3>
    <p>
        The Collaboration Analytics Module part of DTU RAP analyzes the collaboration between DTU and other organizations.
        It is possible to explore the collaboration at either the university or the department level by following one of four paths of exploration – world map, country, partner, or subject.
    </p>
    <p class="list-head">
        The final product of the Collaboration Analytics Module is a detailed collaboration report of the collaboration between DTU and a chosen partner organization to:
    </p>
    <ol>
        <li>view online and exploit the many hyperlinks going deep into certain aspects</li>
        <li>or to download as an offline spreadsheet.</li>
    </ol>
    <p class="list-head">
        The collaboration report provides an overview as well as details of the collaboration between DTU and the chosen partner organization. The report includes the following eight sections:
    </p>
    <ol>
        <li>Collaboration overview</li>
        <li>Compare key output and impact indicators</li>
        <li>Compare annual publication and co-publication output</li>
        <li>Compare partner’s top subjects with DTU and co-publications</li>
        <li>Compare top collaboration subjects with partner and DTU subjects</li>
        <li>Collaboration by DTU department</li>
        <li>Collaboration by DTU researcher (top 20)</li>
        <li>Collaboration by funder (top 20)</li>
    </ol>
    <p>
        A comprehensive introduction to the Collaboration Analytics Module can be found in the <a target="_blank" href="http://rap.adm.dtu.dk/vivo/PDF/DTU_RAP_Collaboration_24-Sep-2019.pdf">DTU RAP Collaboration Module Presentation</a><sup>1</sup>.
    </p>
    <a name="ch-2.2"></a>
    <h3><div>2.2</div>Data flow</h3>
    <p class="list-head">
        There are two data sources for the Collaboration Analytics Module:
    </p>
    <ol>
        <li>
            Web of Science for publication data pertaining to DTU’s Organization-Enhanced in Web of Science
        </li>
        <li>
            InCites for bibliometric indicator data pertaining to DTU’s Organization-Enhanced in InCites and all its collaboration partners with an Organization-Enhanced in Web of Science/InCites
        </li>
    </ol>
    <p>
        DTU RAP imports all the DTU-affiliated data in Web of Science since 2007.
        The “DTU-affiliated” data is isolated using the Web of Science-unified organization name “Technical University of Denmark” (Organization-Enhanced).
    </p>
    <p>
        Data from Web of Science for DTU are stored in VIVO as RDF (Research Description Framework) triples in a triple store.
        The triples compose a graph of connections between discrete entities such as persons, publications, organizations, departments, and countries.
        These entity types and their relationships are defined in the VIVO Ontology; some minor extensions have been added to this ontology to represent details of the Web of Science data.
    </p>
    <p>
        In order to produce a useful overview of publication and collaboration at the department level, the DTU Research Analytics Office maintains mapping tables unifying the many name variants found in publications.
        While this effort successfully unifies more than two thousand departmental name variants into a short list of current DTU departments, a number of departmental affiliations remain undeclared or unclear and are thus mapped to “DTU department unknown”.
    </p>
    <p>
        In addition to the data retrieved from Web of Science for DTU, bibliometric indicators for entire organizations are retrieved from InCites for DTU and the collaborating organizations.
    </p>
    <p>
        A detailed walk-through of the data flow including data loading, enhancing, and storing can be found in the report
        <a target="_blank" href="https://figshare.com/articles/Web_of_Science_InCites_Data_for_VIVO_Research_Analytics_Platform_VIVO_RAP_/11743341">Web of Science &amp; InCites Data for VIVO Research Analytics Platform (VIVO RAP)</a><sup>2</sup>.
    </p>
    <a name="ch-3"></a>
    <h2><div>3.</div>Publication Search Module</h2>
    <hr width="100%"/>
    <a name="ch-3.1"></a>
    <h3><div>3.1.</div>Purpose of module</h3>
    <p class="list-head">
        The Publication Search Module is a search interface for DTU’s Web of Science publications. It includes nine different facets:
    </p>
    <ol>
        <li>Subject categories</li>
        <li>Document types</li>
        <li>Publication years</li>
        <li>Org.-Enhanced</li>
        <li>Journals</li>
        <li>Conferences</li>
        <li>Countries</li>
        <li>Funding agencies</li>
        <li>DTU departments</li>
    </ol>
    <p>
        The facets enable the user to refine the search result to e.g. a certain year, subject category, and/or DTU department.
    </p>
    <a name="ch-3.2"></a>
    <h3><div>3.2.</div>Data flow</h3>
    <p>
        The Publication Search Module includes DTU’s Web of Science publications since 2007.
        The publications are used as basis for the DTU RAP Collaboration Analytics Module. 
    </p>
    <a name="ch-4"></a>
    <h2><div>4.</div>Researcher Profile Module (DTU Researches)</h2>
    <hr width="100%"/>
    <a name="ch-4.1"></a>
    <h3><div>4.1.</div>Purpose of module</h3>
    <p>
        The Researcher Profile Module part of DTU RAP includes researcher profiles for researchers affiliated to DTU based on publications found via their ORCID and ResearcherID in Web of Science.
    </p>
    <p>
        The main purpose of the module is to give DTU’s researchers an overview of their research output and impact based on all research publications and citations found in Web of Science.
        Besides giving the researchers a more structured, sustainable, and open view into their publishing performance the information in DTU Researcher can be used when creating a CV, funding applications, or in other carreer promoting situations.
    </p>
    <a name="ch-4.2"></a>
    <h3><div>4.2.</div>Data flow</h3>
    <p class="list-head">
        The DTU RAP Researcher Module is based on data retrieved from two Clarivate platforms:
    </p>
    <ol>
        <li>
            Web of Science for publication data pertaining to each DTU researcher’s ORCID and/or ResearcherID for all active publication years
        </li>
        <li>
            InCites for bibliometric indicator data pertaining to the publications fetched from Web of Science and matched by the Web of Science ID (UT) of each publication
        </li>
    </ol>
    <p>
        To make the Researcher Profiles as open as possible – and at the same time being able to automate the data load process – the Research Profile Module is only based on publication data that can be found through the researcher-PIDs ORCID and ResearcherID.
        For the researcher to be accurately represented in the DTU RAP Researcher Profile Module, the researcher is required to actively make sure his/her public publication list in ORCID.org is up-to date at all times.
        A detailed data flow is described in the following figure:
    </p>
    <img src="${urls.theme}/images/about.jpg"/>
    <p>
        The ORCIDs and/or ResearcherIDs used to fetch publication and citation data from Web of Science/InCites come from a locally defined affiliation CSV file for each DTU department, which is updated annually starting in 2020 by the departments themselves.
    </p>
    <a name="ch-4.3"></a>
    <h3><div>4.3.</div>All DTU Researchers</h3>
    <p class="list-head">
        Contains a list of all DTU researchers available from the locally defined affiliation CSV department sheets.
        The list includes the following information, which in addition serve as both sortable and searchable facets:
    </p>
    <ol>
        <li>Name</li>
        <li>ORCID</li>
        <li>Email</li>
        <li>Department</li>
        <li>Job title</li>
    </ol>
    <a name="ch-4.4"></a>
    <h3><div>4.4.</div>Single Researcher profile</h3>
    <p class="list-head">
        Each Single Researcher Profile displays:
    </p>
    <ol>
        <li>
            <b>Master data</b>: Name, ORCID, ResearcherID, Email, Year of earliest/latest publication retrieved from Web of Science, Start year of DTU affiliation.
        </li>
        <li>
            <b>DTU affiliations and positions</b>: List of positions at/affiliations to the university’s departments and sections since 2020.
            The information is updated annually by the departments.
        </li>
        <li>
            <b>Statistics on Web of Science publications</b>: retrieved using ORCID and/or ResearcherID.
            The numbers are given per publication type.
            Please note that a publication may be assigned to more than one publication type, which may imply that the sum of these numbers is greater than the total number of publications retrieved.
        </li>
        <ol style="list-style-type: lower-alpha;">
            <li>
                The publication type “Other” can include: Editorials, Notes, Letters, Discussions, Bibliographies, Book reviews, Software reviews, News items, Reprints, and Retractions
            </li>
        </ol>
        <li>
            <b>Publications and citations per year</b>: Graph of the annual number of publications and citations.
        </li>
        <li>
            <b>Metrics based on these publications</b>: You may set the publication timespan and decide which publication types to include in the analysis.
            For a more detailed understanding of the metrics incorporated in DTU RAP please visit
            <a target="_blank" href="https://spiral.imperial.ac.uk/handle/10044/1/75946">Using InCites responsibly: a guide to interpretation and good practice</a><sup>3</sup>.
            <br/>
            The table displays:
        </li>
        <ol style="list-style-type: lower-alpha;">
            <li>Total number of publications retrieved from Web of Science</li>
            <li>Total number of citations retrieved from Web of Science</li>
            <li>The average number of citations per publication</li>
            <li>The average number of citations per active publication year</li>
            <li>The H-index for the set of publications, which is calculated by counting the number of publications that have been cited at least that same number of times.</li>
            <li>International collaboration – i.e. the percentage of publications with international co-authors.</li>
            <li>Open Access – i.e. the percentage of publications with Open Access to the full text.</li>
        </ol>
        <li>
            <b>Link to full list of researcher’s Web of Science publications</b>: as retrieved by ORCID and/or ResearcherID.
        </li>
    </ol>
    <a name="ch-4.5"></a>
    <h3><div>4.5.</div>List of publications</h3>
    <p>
        When going to the full list of a researcher’s Web of Science publications, a full list of the publications retrieved by ORCID and/or ResearcherID for the particular researcher is displayed.
        The publications appear year by year, from the newest publication to the oldest.
    </p>
    <p class="list-head">
        The full list of publications offers four filters:
    </p>
    <ol>
        <li>
            <b>Year</b>: refines the publication list to a particular publication year
        </li>
        <li>
            <b>Type</b>: refines the publication list to the document types selected
        </li>
        <li>
            <b>Affiliation</b>: refines the publication list to DTU or non-DTU affiliated publications (based on the Web of Science DTU Organization-Enhanced)
        </li>
        <li>
            <b>Citation impact</b>: refines the publication list according to four citation impact indicators: Top 10%, Top 1%, above or below world average (according to the InCites Category Normalized Citation Impact (CNCI))
        </li>
    </ol>
    <a name="ch-4.6"></a>
    <h3><div>4.6.</div>Full view of single publication</h3>
    <p>
        From the list of publications view it is possible to go to a single publication.
        The Single Publications Page displays all bibliographic metadata retrieved for the publication from Web of Science.
    </p>
    <p class="list-head">
        The page also includes the heading ‘InCites Indicators’ that can be expanded.
        This includes fifteen indicators from InCites that is the basis for most of the aggregated metrics shown in DTU RAP Researcher and Unit Profile Modules:
    </p>
    <ol>
        <li>
            <b>Times Cited</b>: number of citations in InCites
        </li>
        <li>
            <b>Category Normalized Citation Impact (CNCI)</b>: Citation impact (citations per publication) normalized for subject, year, and document type.
            Values above 1.0 are considered above average, and values below 1.0 are considered below average
        </li>
        <li>
            <b>In top 10%</b>: percentage of publications in the top 10% most cited, based on citations by subject, year, and document type (Y/N indicator).
            Based on Percentile in Subject Area
        </li>
        <li>
            <b>In top 1%</b>: percentage of publications in the top 1% most cited, based on citations by subject (Web of Science Category), year, and document type (Y/N indicator).
            Based on Percentile in Subject Area
        </li>
        <li>
            <b>Percentile in Subject Area</b>: ranking of a publication against all other publications in equivalent subjects, year, and document type.
            If a Percentile is &lt; or =1.0 the publications belongs in Top 1%.
            If a Percentile is &lt; or = 10 the publications belongs in Top 10%
        </li>
        <li>
            <b>Industry Collaboration</b>: if at least one co-author is industry-affiliated (Y/N indicator)
        </li>
        <li>
            <b>Institution Collaboration</b>: if at least one co-author is institution-affiliated (Y/N indicator)
        </li>
        <li>
            <b>International Collaboration</b>: if at least one co-author is international (Y/N indicator)
        </li>
        <li>
            <b>Open Access</b>: if the publication is considered OA of any type (Y/N indicator)
        </li>
        <li>
            <b>Highly Cited Paper</b>: if the publication is classified as ‘highly cited’ by the Essential Science Indicators database (Y/N indicator)
        </li>
        <li>
            <b>Hot Paper</b>: if the publication is classified as a "hot paper" by the Essential Science Indicators (ESI) database (Y/N indicator).
            Hot papers are the top 0.1% of papers published in past two years and cited in the past two months.
        </li>
        <li>
            <b>Category Expected Citations</b>: the expected number of citations calculated from all other publications in equivalent subjects, year, and document type.
            Used when calculating CNCI
        </li>
        <li>
            <b>Journal Impact Factor</b>: counts publications in a journal that is indexed in Journal Citation Reports and given a Journal Impact Factor (JIF) in the particular publication year (only includes data from 1996-)
        </li>
        <li>
            <b>Journal Normalized Citation Impact (JNCI)</b>: calculated similar to CNCI, but based on a comparison to other items in the same journal rather than in the same subject
        </li>
        <li>
            <b>Journal Expected Citations</b>: the expected number of citations calculated from all other publications in the same journal, year, and document type
        </li>
    </ol>
    <p>
        All indicators in DTU RAP Researcher Profile Module are based on calculations from InCites including the Emerging Sources Citation Index (ESCI).
    </p>
    <p>
        For a more detailed understanding of the indicators and metrics incorporated in DTU RAP please visit <a target="_blank" href="https://spiral.imperial.ac.uk/handle/10044/1/75946">Using InCites responsibly: a guide to interpretation and good practice</a><sup>3</sup>.
    </p>
    <a name="ch-5"></a>
    <h2><div>5.</div>Organizational Unit Profile Module (DTU Units)</h2>
    <hr width="100%"/>
    <a name="ch-5.1"></a>
    <h3><div>5.1.</div>Purpose of module</h3>
    <p>
        The DTU RAP Organizational Unit Profile Module contains the different units within DTU distributed by university, department, and section/group level.
        The primary aim of the module is to support evaluation and assessment of the university’s departments and sections.
    </p>
    <a name="ch-5.2"></a>
    <h3><div>5.2.</div>Data flow</h3>
    <p>
        The Organizational Unit Profile Module is based on the same publication, citations, and metrics data as the DTU RAP Researcher Profile Module.
        The publication and indicator data are aggregated according to the CSV affiliation department files filled out by the departments.
        For a more detailed view of the data flow go to <a href="#ch-3.2">chapter 3.2</a>.
    </p>
    <a name="ch-5.3"></a>
    <h3><div>5.3.</div>All DTU Units</h3>
    <p>
        Is an overview of the different DTU units – going from university level to more detailed department structures.
        Select the university, a department, or a section to see metrics and publications.
    </p>
    <a name="ch-5.4"></a>
    <h3><div>5.4.</div>Single Unit Profile</h3>
    <p>
        Each Single Unit Profile displays:
    </p>
    <ol>
        <li>
            <b>‘Head of’ data</b>: Name, ORCID, ResearcherID, Email of the Head/leader of the unit.
        </li>
        <li>
            <b>Researchers</b>: Link to list of researchers within the unit
        </li>
        <li>
            <b>Statistics on Web of Science publications</b>: retrieved using ORCID and/or ResearcherID for all of the researches connected to the particular unit.
            The numbers are given per publication type.
            Please note that a publication may be assigned to more than one publication type, which may imply that the sum of these numbers is greater than the total number of publications retrieved.
        </li>
        <ol style="list-style-type: lower-alpha;">
            <li>
                The publication type “Other” can include: Editorials, Notes, Letters, Discussions, Bibliographies, Book reviews, Software reviews, News items, Reprints, and Retractions
            </li>
        </ol>
        <li>
            <b>Publications and citations per year</b>: Graph of the annual number of publications and citations.
        </li>
        <li>
            <b>Metrics based on these publications</b>.
            You may set the publication timespan and decide which publication types to include in the analysis.
            For a more detailed understanding of the metrics incorporated in DTU RAP please visit <a target="_blank" href="https://spiral.imperial.ac.uk/handle/10044/1/75946">Using InCites responsibly: a guide to interpretation and good practice</a><sup>3</sup>.
            <br/>
            The table displays:
        </li>
        <ol style="list-style-type: lower-alpha;">
            <li>Total number of publications retrieved from Web of Science</li>
            <li>Total number of citations retrieved from Web of Science</li>
            <li>The average number of citations per publication</li>
            <li>The average number of citations per active publication year</li>
            <li>The H-index for the set of publications, which is calculated by counting the number of publications that have been cited at least that same number of times.</li>
            <li>Percentage of the publications that has at least one citation</li>
            <li>The Category Normalized Citation Impact (CNCI) number for the set of publications (an output with a CNCI value of 1.0 can be interpreted as having 'world average impact')</li>
            <li>Percentage of publications found in top 10% of comparable publications (same field, same document type, same publication year)</li>
            <li>Percentage of publications found in top 1% of comparable publications (same field, same document type, same publication year)</li>
            <li>
                International collaboration – i.e. the percentage of publications with international co-authors.
                Share of publications is calculated held up against the Web of Science publications
            </li>
            <li>Open Access – i.e. the percentage of publications with Open Access to the full text.</li>
        </ol>
        <li>
            <b>Link to full list of units’s Web of Science publications</b> as retrieved by ORCID and/or ResearcherID for all of the researches connected to the particular unit.
        </li>
    </ol>
    <a name="ch-5.5"></a>
    <h3><div>5.5.</div>List of publications</h3>
    <p>
        When going to the full list of a unit’s Web of Science publications, a full list of the publications fetched as retrieved by ORCID and/or ResearcherID for the particular unit is displayed.
        The publications appears year by year ordered from the newest publication descending to the oldest.
    </p>
    <p class="list-head">
        The full list of publications offers four filters:
    </p>
    <ol>
        <li><b>Year</b>: refines the publication list to a particular publication year</li>
        <li><b>Type</b>: refines the publication list to the document types selected</li>
        <li><b>Affiliation</b>: refines the publications according to DTU or non-DTU affiliated publications (based on the Web of Science DTU Organization-Enhanced)</li>
        <li><b>Citation impact</b>: refines the publications according to four citation impact indicators – publications in the list that are in: Top 10%, Top 1%, above or below world average (according to the InCites Category Normalized Citation Impact (CNCI))</li>
    </ol>
    <a name="ch-5.6"></a>
    <h3><div>5.6.</div>Full view of single publication</h3>
    <p>
        From the list of publications view it is possible to go to a single publication.
        The Single Publications Page displays all bibliographic metadata retrieved for the publication from Web of Science.
    </p>
    <p class="list-head">
        The page also includes the heading ‘InCites Indicators’ that can be expanded.
        This includes fifteen indicators from InCites that is the basis for most of the aggregated metrics shown in DTU RAP Researcher and Unit Profile Modules:
    </p>
    <ol>
        <li>
            <b>Times Cited</b>: number of citations in InCites
        </li>
        <li>
            <b>Category Normalized Citation Impact (CNCI)</b>: Citation impact (citations per publication) normalized for subject, year, and document type.
            Values above 1.0 are considered above average, and values below 1.0 are considered below average
        </li>
        <li>
            <b>In top 10%</b>: percentage of publications in the top 10% most cited, based on citations by subject, year, and document type (Y/N indicator).
            Based on Percentile in Subject Area
        </li>
        <li>
            <b>In top 1%</b>: percentage of publications in the top 1% most cited, based on citations by subject (Web of Science Category), year, and document type (Y/N indicator). Based on Percentile in Subject Area
        </li>
        <li>
            <b>Percentile in Subject Area</b>: ranking of a publication against all other publications in equivalent subjects, year, and document type.
            If a Percentile is &lt; or =1.0 the publications belongs in Top 1%.
            If a Percentile is &lt; or = 10 the publications belongs in Top 10%
        </li>
        <li>
            <b>Industry Collaboration</b>: if at least one co-author is industry-affiliated (Y/N indicator)
        </li>
        <li>
            <b>Institution Collaboration</b>: if at least one co-author is institution-affiliated (Y/N indicator)
        </li>
        <li>
            <b>International Collaboration</b>: if at least one co-author is international (Y/N indicator)
        </li>
        <li>
            <b>Open Access</b>: if the publication considered OA of any type (Y/N indicator)
        </li>
        <li>
            <b>Highly Cited Paper</b>: if the publication is classified as ‘highly cited’ by the Essential Science Indicators database (Y/N indicator)
        </li>
        <li>
            <b>Hot Paper</b>: if the publication is classified as a "hot paper" by the Essential Science Indicators (ESI) database (Y/N indicator).
            Hot papers are the top 0.1% of papers published in past two years and cited in the past two months.
        </li>
        <li>
            <b>Category Expected Citations</b>: the expected number of citations calculated from all other publications in equivalent subjects, year, and document type.
            Used when calculating CNCI
        </li>
        <li>
            <b>Journal Impact Factor</b>: counts publications in a journal that is indexed in Journal Citation Reports and given a Journal Impact Factor (JIF) in the particular publication year (only includes data from 1996-)
        </li>
        <li>
            <b>Journal Normalized Citation Impact (JNCI)</b>: calculated similar to CNCI, but based on a comparison to other items in the same journal rather than in the same subject
        </li>
        <li>
            <b>Journal Expected Citations</b>: the expected number of citations calculated from all other publications in the same journal, year, and document type
        </li>
    </ol>
    <p>
        All indicators in DTU RAP Unit Profile Module are based on calculations from InCites including the Emerging Sources Citation Index (ESCI).
    </p>
    <p>
        For a more detailed understanding of the indicators and metrics incorporated in DTU RAP please visit Using <a target="_blank" href="https://spiral.imperial.ac.uk/handle/10044/1/75946">InCites responsibly: a guide to interpretation and good practice</a><sup>3</sup>.
    </p>
    <hr width="30%"/>
    <ul>
        <li>
            <sup>1</sup><a target="_blank" href="http://rap.adm.dtu.dk/vivo/PDF/DTU_RAP_Collaboration_24-Sep-2019.pdf">http://rap.adm.dtu.dk/vivo/PDF/DTU_RAP_Collaboration_24-Sep-2019.pdf</a>
        </li>
        <li>
            <sup>2</sup><a target="_blank" href="https://figshare.com/articles/Web_of_Science_InCites_Data_for_VIVO_Research_Analytics_Platform_VIVO_RAP_/11743341">https://figshare.com/articles/Web_of_Science_InCites_Data_for_VIVO_Research_Analytics_Platform_VIVO_RAP_/11743341</a>
        </li>
        <li>
            <sup>3</sup><a target="_blank" href="https://spiral.imperial.ac.uk/handle/10044/1/75946">https://spiral.imperial.ac.uk/handle/10044/1/75946</a>
        </li>
    </ul>
</div>

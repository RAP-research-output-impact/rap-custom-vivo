/*
    DTU Departments
*/
function dept_bc_dropdown(id) {
    var html = "<span id=\"bc-dept\">" + 
               "    <select id=\"dtu-dept\" name=\"dtu-dept\">" +
               "    </select>" +
               "    <select id=\"dtu-dept-tmp\" name=\"dtu-dept-tmp\" style=\"display: none;\">" +
               "        <option id=\"dtu-dept-tmp-opt\"></option>" +
               "    </select>" +
               "</span>" +
               "<span id=\"bc-dept-view\">" +
               "</span>";
    $("#" + id).html(html);
}

function dept_bc_width() {
    $("#dtu-dept-tmp-opt").text($("#dtu-dept option:selected").text());
    $("#dtu-dept").width($("#dtu-dept-tmp").width() * 1.02);
}

function dept_bc_setup(id, callback) {
    dept_bc_dropdown(id);
    dept_options("dtu-dept");
    var urlParams = new URLSearchParams(window.location.search);
    $("#dtu-dept").val(urlParams.get('dept'));
    dept_bc_width();
    $("#dtu-dept").change(function() {
        dept_bc_width();
        callback();
    });
}

function dept_bc_edit() {
    $("#bc-dept-view").hide();
    $("#bc-dept").show();
}

function dept_bc_view() {
    $("#bc-dept").hide();
    $("#bc-dept-view").html($("#dtu-dept option:selected").text());
    $("#bc-dept-view").show();
}

function dept_val() {
    return $("#dtu-dept").val();
}

function dept_options(id) {
    var dept = [
        {"uri":"",                                     "name":"Entire university"},
        {"uri":"dtusuborg-centre-for-oil-and-gas-dtu", "name":"Centre for Oil and Gas - DTU"},
        {"uri":"dtusuborg-dtu-aqua",                   "name":"DTU Aqua"},
        {"uri":"dtusuborg-dtu-bioengineering",         "name":"DTU Bioengineering"},
        {"uri":"dtusuborg-dtu-bioinformatics",         "name":"DTU Bioinformatics"},
        {"uri":"dtusuborg-dtu-biosustain",             "name":"DTU Biosustain"},
        {"uri":"dtusuborg-dtu-business",               "name":"DTU Business"},
        {"uri":"dtusuborg-dtu-chemical-engineering",   "name":"DTU Chemical Engineering"},
        {"uri":"dtusuborg-dtu-chemistry",              "name":"DTU Chemistry"},
        {"uri":"dtusuborg-dtu-civil-engineering",      "name":"DTU Civil Engineering"},
        {"uri":"dtusuborg-dtu-compute",                "name":"DTU Compute"},
        {"uri":"dtusuborg-dtu-danchip",                "name":"DTU Danchip"},
        {"uri":"dtusuborg-dtu-diplom",                 "name":"DTU Diplom"},
        {"uri":"dtusuborg-dtu-electrical-engineering", "name":"DTU Electrical Engineering"},
        {"uri":"dtusuborg-dtu-energy",                 "name":"DTU Energy"},
        {"uri":"dtusuborg-dtu-environment",            "name":"DTU Environment"},
        {"uri":"dtusuborg-dtu-food",                   "name":"DTU Food"},
        {"uri":"dtusuborg-dtu-fotonik",                "name":"DTU Fotonik"},
        {"uri":"dtusuborg-dtu-health-tech",            "name":"DTU Health Tech"},
        {"uri":"dtusuborg-dtu-management-engineering", "name":"DTU Management Engineering"},
        {"uri":"dtusuborg-dtu-mechanical-engineering", "name":"DTU Mechanical Engineering"},
        {"uri":"dtusuborg-dtu-nanotech",               "name":"DTU Nanotech"},
        {"uri":"dtusuborg-dtu-nutech",                 "name":"DTU Nutech"},
        {"uri":"dtusuborg-dtu-physics",                "name":"DTU Physics"},
        {"uri":"dtusuborg-dtu-space",                  "name":"DTU Space"},
        {"uri":"dtusuborg-dtu-systems-biology",        "name":"DTU Systems Biology"},
        {"uri":"dtusuborg-dtu-vet",                    "name":"DTU Vet"},
        {"uri":"dtusuborg-dtu-wind-energy",            "name":"DTU Wind Energy"},
        {"uri":"dtusuborg-dtu-department-unknown",     "name":"DTU department unknown"},
        {"uri":"dtusuborg-ris-dtu",                    "name":"Risø DTU"}
    ];
    $("#" + id).val(null);
    dept.forEach(function(e) {
        $("#" + id).append(new Option(e.name, e.uri));
    });
}

/*
    Year ranges
*/
function range_bc_dropdown(id) {
    var html = "<span id=\"bc-range\">" + 
               "    From" +
               "    <select id=\"year-from\" name=\"year-from\">" +
               "    </select>" +
               "    -" +
               "    <select id=\"year-to\" name=\"year-to\">" +
               "    </select>" +
               "</span>" +
               "<span id=\"bc-range-view\">" +
               "</span>";
    $("#" + id).html(html);
}

function range_bc_options() {
    var last = new Date().getFullYear();
    var year;
    for(year = 2007; year <= last; year++) {
        $("#year-from").append(new Option(year, year));
        $("#year-to").append(new Option(year, year));
    }
}

function range_bc_setup(id, callback) {
    range_bc_dropdown(id);
    range_bc_options();
    var urlParams = new URLSearchParams(window.location.search);
    var last = new Date().getFullYear();
    var year;
    year = urlParams.get('year-from');
    if (year) {
        $("#year-from").val(year);
    } else {
        $("#year-from").val(last - 5);
    }
    year = urlParams.get('year-to');
    if (year) {
        $("#year-to").val(year);
    } else {
        $("#year-to").val(last);
    }
    $("#year-from").change(function() {
        if ($("#year-from").val() > $("#year-to").val()) {
            $("#year-to").val($("#year-from").val());
        } 
        callback();
    });
    $("#year-to").change(function() {
        if ($("#year-to").val() < $("#year-from").val()) {
            $("#year-from").val($("#year-to").val());
        } 
        callback();
    });
}

function range_bc_edit() {
    $("#bc-range-view").hide();
    $("#bc-range").show();
}

function range_bc_view() {
    $("#bc-range").hide();
    $("#bc-range-view").html("From " + $('#year-from').val() + " - " + $('#year-to').val());
    $("#bc-range-view").show();
}

function range_from_val() {
    return $("#year-from").val();
}

function range_to_val() {
    return $("#year-to").val();
}

/*
    UI functions
*/
function sortArrow(up, used) {
    var svg = '<svg height="14" width="24">';
    if (up) {
        if (used) {
            svg += '<polygon points="12,2 22,12 2,12 12,2" style="fill:red;stroke:white;stroke-width:2" />';
        } else {
            svg += '<polygon points="12,2 22,12 2,12 12,2" style="fill:none;stroke:white;stroke-width:2" />';
        }
    } else {
        if (used) {
            svg += '<polygon points="12,12 2,2 22,2 12,12" style="fill:red;stroke:white;stroke-width:2" />';
        } else {
            svg += '<polygon points="12,12 2,2 22,2 12,12" style="fill:none;stroke:white;stroke-width:2" />';
        }
    }
    svg += '</svg>';
    return svg;
}

/*
    Data functions
*/
function loadData(url, callback) {
    var xhr = new XMLHttpRequest();

    xhr.open('GET', url);
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response)
            callback(response);
        } else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}

function orgList(data) {
    var tbody = "";
    var range = "&year-from=" + range_from_val() + "&year-to=" + range_to_val();
    for (var i = 0, j = data.orgs.length; i < j; i++){
        tbody += "<tr><td class=\"map-org-org sort-org\"><a href=\"" + urls_base + "/individual?uri=" + data.orgs[i].org + range + "\">" + data.orgs[i].name +
                 "</a></td><td class=\"sort-pub\" style=\"text-align: right;\">" + data.orgs[i].publications + "</td></tr>";
    }
    $("#map-org-list tbody").html(tbody);
    $("#sort-pub .sort-dir").html (sortArrow (1, 1));
    $("#sort-org .sort-dir").html (sortArrow (0, 0));
    $('#sort-org').each(function() {
        var inverse = false;
        $(this).click(function() {
            $("td.sort-org").sortElements(function(a, b) {
                return $.text([a]) > $.text([b]) ?
                       inverse ? -1 : 1
                     : inverse ? 1 : -1;
            }, function() {
                return this.parentNode;
            });
            if (inverse) {
                $("#sort-org .sort-dir").html (sortArrow (1, 1));
            } else {
                $("#sort-org .sort-dir").html (sortArrow (0, 1));
            }
            $("#sort-pub .sort-dir").html (sortArrow (0, 0));
            inverse = !inverse;
        });
    });
    $('#sort-pub').each(function() {
        var inverse = false;
        $(this).click(function() {
            $("td.sort-pub").sortElements(function(a, b) {
                return Number($.text([a])) > Number($.text([b])) ?
                       inverse ? -1 : 1
                     : inverse ? 1 : -1;
            }, function() {
                return this.parentNode;
            });
            if (inverse) {
                $("#sort-pub .sort-dir").html (sortArrow (1, 1));
            } else {
                $("#sort-pub .sort-dir").html (sortArrow (0, 1));
            }
            $("#sort-org .sort-dir").html (sortArrow (0, 0));
            inverse = !inverse;
        });
    });
}

